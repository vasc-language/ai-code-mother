package com.spring.aicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.spring.aicodemother.exception.BusinessException;
import com.spring.aicodemother.exception.ErrorCode;
import com.spring.aicodemother.mapper.AppMapper;
import com.spring.aicodemother.model.dto.user.UserQueryRequest;
import com.spring.aicodemother.model.entity.App;
import com.spring.aicodemother.model.vo.UserProfileVO;
import com.spring.aicodemother.model.vo.UserStatisticsVO;
import com.spring.aicodemother.model.vo.UserVO;
import com.spring.aicodemother.model.entity.User;
import com.spring.aicodemother.mapper.UserMapper;
import com.spring.aicodemother.model.enums.UserRoleEnum;
import com.spring.aicodemother.model.vo.LoginUserVO;
import com.spring.aicodemother.service.UserService;
import com.spring.aicodemother.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.spring.aicodemother.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private AppMapper appMapper;

    @Autowired
    private com.spring.aicodemother.service.UserPointsService userPointsService;

    @Autowired
    private com.spring.aicodemother.service.InviteRecordService inviteRecordService;

    @Autowired
    private EmailService emailService;

    @Override
    public long userRegister(String userEmail, String emailCode, String userPassword, String checkPassword, String inviteCode) {
        // 1. 校验
        if (StrUtil.hasBlank(userEmail, emailCode, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        // 2. 校验邮箱格式
        if (!cn.hutool.core.util.ReUtil.isMatch("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", userEmail)) {
            throw new BusinessException(ErrorCode.EMAIL_FORMAT_ERROR);
        }

        // 3. 校验密码长度
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能少于8位");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 4. 验证邮箱验证码
        boolean codeValid = emailService.verifyCode(userEmail, emailCode, "REGISTER");
        if (!codeValid) {
            throw new BusinessException(ErrorCode.EMAIL_CODE_INVALID);
        }

        // 5. 数据库是否已存在该邮箱
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userEmail", userEmail);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTS);
        }

        // 6. 加密
        String encryptPassword = getEncryptPassword(userPassword);

        // 7. 插入数据库
        User user = new User();
        user.setUserAccount(userEmail);
        user.setUserEmail(userEmail);
        user.setEmailVerified(1); // 已验证邮箱
        user.setUserPassword(encryptPassword);
        user.setUserName(userEmail.substring(0, userEmail.indexOf('@'))); // 默认用户名为邮箱前缀
        user.setUserRole(UserRoleEnum.USER.getValue());
        // 保存用户到数据库
        boolean result = save(user);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }

        // 5. 发放新用户注册奖励积分
        Long userId = user.getId();
        try {
            userPointsService.addPoints(
                    userId,
                    com.spring.aicodemother.constants.PointsConstants.REGISTER_REWARD,
                    com.spring.aicodemother.model.enums.PointsTypeEnum.REGISTER.getValue(),
                    "新用户注册奖励",
                    null
            );
            log.info("新用户 {} 注册成功，发放{}积分奖励", userId,
                    com.spring.aicodemother.constants.PointsConstants.REGISTER_REWARD);
        } catch (Exception e) {
            log.error("发放新用户注册奖励失败: {}", e.getMessage(), e);
            // 注册奖励失败不影响注册流程
        }

        // 6. 处理邀请码逻辑
        if (StrUtil.isNotBlank(inviteCode)) {
            try {
                // 绑定邀请关系
                boolean bound = inviteRecordService.bindInvite(inviteCode, userId, null, null);
                if (bound) {
                    log.info("用户 {} 通过邀请码 {} 注册，绑定邀请关系成功", userId, inviteCode);

                    // 发放邀请注册奖励
                    boolean inviteRewarded = inviteRecordService.rewardInviteRegister(userId);
                    if (inviteRewarded) {
                        log.info("用户 {} 邀请注册奖励发放成功", userId);
                    } else {
                        log.warn("用户 {} 邀请注册奖励发放失败", userId);
                    }
                } else {
                    log.warn("用户 {} 绑定邀请关系失败，邀请码: {}", userId, inviteCode);
                }
            } catch (Exception e) {
                log.error("处理邀请码失败: {}", e.getMessage(), e);
                // 邀请码处理失败不影响注册流程
            }
        }

        return userId;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public LoginUserVO userLogin(String userEmail, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StrUtil.hasBlank(userEmail, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        // 2. 校验邮箱格式
        if (!cn.hutool.core.util.ReUtil.isMatch("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", userEmail)) {
            throw new BusinessException(ErrorCode.EMAIL_FORMAT_ERROR);
        }

        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        // 3. 加密
        String encryptPassword = getEncryptPassword(userPassword);

        // 4. 查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userEmail", userEmail);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);

        // 5. 用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        // 6. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);

        // 7. 获得脱敏后的用户信息
        return this.getLoginUserVO(user);
    }

    /**
     * 获取当前登陆对象
     *
     * @param request 根据用户ID查询
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 用户注销，将 isDelete 置为1
     *
     * @param request
     * @return
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 用户管理相关操作
     * 个人用户信息脱敏
     *
     * @param user
     * @return
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 用户列表脱敏
     *
     * @param userList
     * @return
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .eq("userRole", userRole)
                .like("userAccount", userAccount)
                .like("userName", userName)
                .like("userProfile", userProfile)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }


    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆视听
        final String SALT = "Join2049";

        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public UserProfileVO getUserProfile(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID无效");
        }

        // 1. 查询用户基本信息
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        // 2. 获取用户统计数据
        UserStatisticsVO statistics = this.getUserStatistics(userId);

        // 3. 组装返回结果
        UserProfileVO profileVO = new UserProfileVO();
        profileVO.setUser(this.getUserVO(user));
        profileVO.setStatistics(statistics);

        return profileVO;
    }

    @Override
    public UserStatisticsVO getUserStatistics(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID无效");
        }

        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        UserStatisticsVO statistics = new UserStatisticsVO();

        // 1. 统计应用数量
        QueryWrapper appCountWrapper = QueryWrapper.create()
                .eq("userId", userId)
                .eq("isDelete", 0);
        long appCount = appMapper.selectCountByQuery(appCountWrapper);
        statistics.setAppCount(appCount);

        // 2. 生成次数（暂时使用应用数量作为代理指标，未来可以从chat表统计）
        statistics.setGenerateCount(appCount);

        // 3. 累计使用天数
        LocalDateTime createTime = user.getCreateTime();
        if (createTime != null) {
            long joinDays = ChronoUnit.DAYS.between(createTime, LocalDateTime.now());
            statistics.setJoinDays(joinDays);
        } else {
            statistics.setJoinDays(0L);
        }

        // 4. 最近活跃时间（从用户应用的最新更新时间获取）
        QueryWrapper lastActiveWrapper = QueryWrapper.create()
                .select("updateTime")
                .from("app")
                .where("userId = ? and isDelete = 0", userId)
                .orderBy("updateTime", false)
                .limit(1);
        App lastApp = appMapper.selectOneByQuery(lastActiveWrapper);
        if (lastApp != null && lastApp.getUpdateTime() != null) {
            statistics.setLastActiveTime(lastApp.getUpdateTime());
        } else {
            statistics.setLastActiveTime(createTime);
        }

        return statistics;
    }

    @Override
    public boolean resetPassword(String userEmail, String emailCode, String newPassword, String checkPassword) {
        // 1. 校验参数
        if (StrUtil.hasBlank(userEmail, emailCode, newPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        // 2. 校验邮箱格式
        if (!cn.hutool.core.util.ReUtil.isMatch("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", userEmail)) {
            throw new BusinessException(ErrorCode.EMAIL_FORMAT_ERROR);
        }

        // 3. 验证邮箱验证码
        boolean codeValid = emailService.verifyCode(userEmail, emailCode, "RESET_PASSWORD");
        if (!codeValid) {
            throw new BusinessException(ErrorCode.EMAIL_CODE_INVALID);
        }

        // 4. 验证两次密码是否一致
        if (!newPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 5. 验证密码长度
        if (newPassword.length() < 8 || newPassword.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度必须在8-20位之间");
        }

        // 6. 查询用户是否存在
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq("userEmail", userEmail);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "该邮箱未注册");
        }

        // 7. 加密新密码
        String encryptPassword = getEncryptPassword(newPassword);

        // 8. 更新用户密码
        user.setUserPassword(encryptPassword);
        boolean result = this.updateById(user);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "密码重置失败");
        }

        log.info("用户 {} 密码重置成功", userEmail);
        return true;
    }
}
