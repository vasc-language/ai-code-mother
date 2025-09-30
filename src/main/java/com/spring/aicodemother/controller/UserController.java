package com.spring.aicodemother.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.spring.aicodemother.annotation.AuthCheck;
import com.spring.aicodemother.common.BaseResponse;
import com.spring.aicodemother.common.DeleteRequest;
import com.spring.aicodemother.common.ResultUtils;
import com.spring.aicodemother.constant.UserConstant;
import com.spring.aicodemother.exception.BusinessException;
import com.spring.aicodemother.exception.ErrorCode;
import com.spring.aicodemother.exception.ThrowUtils;
import com.spring.aicodemother.model.dto.user.*;
import com.spring.aicodemother.model.entity.App;
import com.spring.aicodemother.model.vo.AppVO;
import com.spring.aicodemother.model.vo.LoginUserVO;
import com.spring.aicodemother.model.vo.UserVO;
import com.spring.aicodemother.service.AppService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.spring.aicodemother.model.entity.User;
import com.spring.aicodemother.service.UserService;

import java.util.List;

/**
 * 用户 控制层。
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AppService appService;

    /**
     * 用户注册
     *
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取登陆用户信息
     *
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getUserLogin(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        // 返回脱敏后的用户信息
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }



    /**
     * 创建用户
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        // 默认密码 12345678
        final String DEFAULT_PASSWORD = "12345678";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserVO> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        // 返回脱敏用户信息
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        // 权限交由 getUserById 方法校验
        return getUserById(id);
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 增加业务逻辑，禁止管理员删除自己
        User loginUser = userService.getLoginUser(request);
        if (deleteRequest.getId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "不能删除自己");
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 增加业务逻辑，禁止管理员修改自己的角色
        User loginUser = userService.getLoginUser(request);
        if (userUpdateRequest.getId().equals(loginUser.getId()) &&
                userUpdateRequest.getUserRole() != null &&
                !userUpdateRequest.getUserRole().equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "不能修改自己的角色");
        }

        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        // 防止密码被意外更新
        user.setUserPassword(null);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 分页获取用户封装列表（仅管理员）
     *
     * @param userQueryRequest 查询请求参数
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = userQueryRequest.getPageNum();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(Page.of(pageNum, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        // 数据脱敏
        Page<UserVO> userVOPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

    /**
     * 获取用户主页信息
     *
     * @param userId 用户ID，如果不传则获取当前登录用户的主页
     * @param request HttpServletRequest
     * @return 用户主页信息
     */
    @GetMapping("/profile")
    public BaseResponse<com.spring.aicodemother.model.vo.UserProfileVO> getUserProfile(
            Long userId,
            HttpServletRequest request) {
        // 如果没有传userId，则获取当前登录用户的ID
        if (userId == null || userId <= 0) {
            User loginUser = userService.getLoginUser(request);
            userId = loginUser.getId();
        }

        com.spring.aicodemother.model.vo.UserProfileVO profileVO = userService.getUserProfile(userId);
        return ResultUtils.success(profileVO);
    }

    /**
     * 更新当前用户信息（个人主页编辑）
     *
     * @param updateRequest 用户信息更新请求
     * @param request HttpServletRequest
     * @return 是否更新成功
     */
    @PostMapping("/profile/update")
    public BaseResponse<Boolean> updateUserProfile(
            @RequestBody com.spring.aicodemother.model.dto.user.UserProfileUpdateRequest updateRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(updateRequest == null, ErrorCode.PARAMS_ERROR);

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 参数校验
        String userName = updateRequest.getUserName();
        String userProfile = updateRequest.getUserProfile();

        if (StrUtil.isNotBlank(userName)) {
            if (userName.length() < 2 || userName.length() > 20) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称长度必须在2-20字符之间");
            }
        }

        if (StrUtil.isNotBlank(userProfile) && userProfile.length() > 200) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "个人简介不能超过200字符");
        }

        // 更新用户信息
        User user = new User();
        user.setId(loginUser.getId());
        user.setUserName(updateRequest.getUserName());
        user.setUserAvatar(updateRequest.getUserAvatar());
        user.setUserProfile(updateRequest.getUserProfile());

        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新失败");

        return ResultUtils.success(true);
    }

    /**
     * 获取用户应用列表（分页+排序+筛选）
     *
     * @param queryRequest 查询请求参数
     * @param request HttpServletRequest
     * @return 应用列表
     */
    @PostMapping("/apps")
    public BaseResponse<Page<AppVO>> getUserApps(
            @RequestBody UserAppsQueryRequest queryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);

        Long userId = queryRequest.getUserId();
        // 如果没有传userId，则查询当前登录用户的应用
        if (userId == null || userId <= 0) {
            User loginUser = userService.getLoginUser(request);
            userId = loginUser.getId();
        }

        // 构建查询条件
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userId", userId)
                .eq("isDelete", 0);

        // 生成类型筛选
        String genType = queryRequest.getGenType();
        if (StrUtil.isNotBlank(genType) && !"all".equals(genType)) {
            queryWrapper.eq("codeGenType", genType);
        }

        // 排序处理
        String sortBy = queryRequest.getSortBy();
        String sortOrder = queryRequest.getSortOrder();
        boolean isAsc = "ascend".equals(sortOrder);

        if ("appName".equals(sortBy)) {
            queryWrapper.orderBy("appName", isAsc);
        } else if ("updateTime".equals(sortBy)) {
            queryWrapper.orderBy("updateTime", isAsc);
        } else {
            // 默认按创建时间排序
            queryWrapper.orderBy("createTime", isAsc);
        }

        // 分页查询
        long pageNum = queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize();
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);

        // 数据转换
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);

        return ResultUtils.success(appVOPage);
    }

}
