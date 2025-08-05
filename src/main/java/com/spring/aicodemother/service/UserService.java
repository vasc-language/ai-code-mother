package com.spring.aicodemother.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.spring.aicodemother.model.dto.UserQueryRequest;
import com.spring.aicodemother.model.dto.UserVO;
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
     * @param userAccount 用户账号
     * @param userPassword 密码
     * @param checkPassword 校验密码
     * @return
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

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

}
