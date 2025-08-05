package com.spring.aicodemother.controller;

import com.spring.aicodemother.model.dto.UserLoginRequest;
import com.spring.aicodemother.service.UserService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户接口测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 确保每个测试方法都在事务中运行，并在结束后回滚，避免污染数据库
class UserControllerTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 定义一个可复用的测试用户信息
    private static final String TEST_USER_ACCOUNT = "testUser123";
    private static final String TEST_USER_PASSWORD = "password123";

    /**
     * 在每个测试执行前，先注册一个用于测试的用户
     */
    @BeforeEach
    void setUp() {
        // 为保证幂等性，可以先尝试删除，再创建
        // 但由于 @Transactional 的存在，每次测试都是干净的，所以直接注册即可
        userService.userRegister(TEST_USER_ACCOUNT, TEST_USER_PASSWORD, TEST_USER_PASSWORD);
    }

    @Test
    @DisplayName("获取登录用户信息 - 成功场景")
    void getUserLogin_Success() throws Exception {
        // 1. 准备：先模拟登录，获取有效的 Session
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setUserAccount(TEST_USER_ACCOUNT);
        loginRequest.setUserPassword(TEST_USER_PASSWORD);

        MvcResult loginResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0)) // 登录成功
                .andReturn();

        // 从登录结果中提取 Session
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();
        assert session != null;

        // 2. 执行：使用获取到的 Session 发起获取用户信息的请求
        mockMvc.perform(get("/user/get/login")
                        .session(session)) // 关键：带上 session
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0)) // 期望响应成功
                .andExpect(jsonPath("$.data.userAccount").value(TEST_USER_ACCOUNT)) // 验证返回的账号是否正确
                .andExpect(jsonPath("$.data.userRole").value("user")) // 验证返回的角色是否正确
                .andExpect(jsonPath("$.data.userPassword").doesNotExist()); // 关键安全验证：确保密码字段不存在
    }

    @Test
    @DisplayName("获取登录用户信息 - 失败场景（未登录）")
    void getUserLogin_NotLoggedIn() throws Exception {
        // 1. 准备：无需登录，直接请求

        // 2. 执行：发起获取用户信息的请求，不带任何 Session
        mockMvc.perform(get("/user/get/login"))
                .andExpect(status().isOk()) // 全局异常处理器会捕获异常并返回 200 OK
                .andExpect(jsonPath("$.code").value(40100)) // 期望返回“未登录”的错误码
                .andExpect(jsonPath("$.message").value("未登录")) // 期望返回“未登录”的错误信息
                .andExpect(jsonPath("$.data").isEmpty()); // 期望 data 字段为空
    }
}
