package com.spring.aicodemother.service.impl;

import com.spring.aicodemother.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 姚东名
 * Date: 2025-08-06
 * Time: 10:56
 */
@SpringBootTest
class UserServiceImplTest {
    @Autowired
    private UserService userService;

    @Test
    void getEncryptPassword() {
        System.out.println(userService.getEncryptPassword("12345678"));
        // 45682a7088aad280cec116864abbdc93
    }
}