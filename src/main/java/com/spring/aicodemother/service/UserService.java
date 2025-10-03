package com.spring.aicodemother.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.spring.aicodemother.model.dto.user.UserQueryRequest;
import com.spring.aicodemother.model.vo.UserProfileVO;
import com.spring.aicodemother.model.vo.UserStatisticsVO;
import com.spring.aicodemother.model.vo.UserVO;
import com.spring.aicodemother.model.entity.User;
import com.spring.aicodemother.model.vo.LoginUserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userEmail 用户邮箱
     * @param emailCode 邮箱验证码
     * @param userPassword 密码
     * @param checkPassword 校验密码
     * @param inviteCode 邀请码(可选)
     * @return 新用户ID
     */
    long userRegister(String userEmail, String emailCode, String userPassword, String checkPassword, String inviteCode);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 用户登录
     *
     * @param userEmail 用户邮箱
     * @param userPassword 用户密码
     * @param request HttpServletRequest
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userEmail, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 用户管理：单个用户信息进行脱敏操作
     */
    UserVO getUserVO(User user);

    /**
     * 用户管理：用户列表信息进行脱敏
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 专门用于将查询请求转为 QueryWrapper 对象
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 加密
     */
    String getEncryptPassword(String userPassword);

    /**
     * 获取用户主页信息（包含用户信息和统计数据）
     *
     * @param userId 用户ID
     * @return 用户主页视图对象
     */
    UserProfileVO getUserProfile(Long userId);

    /**
     * 获取用户统计数据
     *
     * @param userId 用户ID
     * @return 用户统计数据
     */
    UserStatisticsVO getUserStatistics(Long userId);

    /**
     * 重置密码
     *
     * @param userEmail 用户邮箱
     * @param emailCode 邮箱验证码
     * @param newPassword 新密码
     * @param checkPassword 确认密码
     * @return 是否重置成功
     */
    boolean resetPassword(String userEmail, String emailCode, String newPassword, String checkPassword);

}
