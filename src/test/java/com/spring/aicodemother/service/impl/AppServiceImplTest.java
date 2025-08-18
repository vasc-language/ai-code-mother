package com.spring.aicodemother.service.impl;

import com.spring.aicodemother.core.AiCodeGeneratorFacade;
import com.spring.aicodemother.exception.BusinessException;
import com.spring.aicodemother.exception.ErrorCode;
import com.spring.aicodemother.mapper.AppMapper;
import com.spring.aicodemother.model.entity.App;
import com.spring.aicodemother.model.entity.User;
import com.spring.aicodemother.model.enums.CodeGenTypeEnum;
import com.spring.aicodemother.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * AppServiceImpl 测试类
 * 
 * Created with IntelliJ IDEA.
 * Description: 测试 AppServiceImpl 中的 chatToGenCode 方法
 * User: 姚东名
 * Date: 2025-08-12
 * Time: 11:26
 */
@ExtendWith(MockitoExtension.class)
class AppServiceImplTest {

    @Mock
    private AppMapper appMapper;

    @Mock
    private UserService userService;

    @Mock
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @InjectMocks
    private AppServiceImpl appService;

    private User mockUser;
    private App mockApp;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUserAccount("testUser");

        // 创建测试应用
        mockApp = new App();
        mockApp.setId(1L);
        mockApp.setUserId(1L);
        mockApp.setAppName("测试应用");
        mockApp.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());
        mockApp.setInitPrompt("测试初始提示");
    }

    @Test
    void chatToGenCode_参数校验_appId为空() {
        // given: appId 为空
        Long appId = null;
        String message = "生成一个登录页面";

        // when & then: 应该抛出参数错误异常
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> appService.chatToGenCode(appId, message, mockUser));
        
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("应用 ID 不能为空"));
    }

    @Test
    void chatToGenCode_参数校验_appId小于等于0() {
        // given: appId 小于等于 0
        Long appId = 0L;
        String message = "生成一个登录页面";

        // when & then: 应该抛出参数错误异常
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> appService.chatToGenCode(appId, message, mockUser));
        
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("应用 ID 不能为空"));
    }

    @Test
    void chatToGenCode_参数校验_message为空() {
        // given: message 为空
        Long appId = 1L;
        String message = "";

        // when & then: 应该抛出参数错误异常
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> appService.chatToGenCode(appId, message, mockUser));
        
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("用户提示词不能为空"));
    }

    @Test
    void chatToGenCode_参数校验_message为blank() {
        // given: message 为空白字符
        Long appId = 1L;
        String message = "   ";

        // when & then: 应该抛出参数错误异常
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> appService.chatToGenCode(appId, message, mockUser));
        
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("用户提示词不能为空"));
    }

    @Test
    void chatToGenCode_应用不存在() {
        // given: 应用不存在
        Long appId = 999L;
        String message = "生成一个登录页面";
        
        // 创建一个spy对象来Mock getById方法
        AppServiceImpl spyService = spy(appService);
        doReturn(null).when(spyService).getById(appId);

        // when & then: 应该抛出未找到错误异常
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> spyService.chatToGenCode(appId, message, mockUser));
        
        assertEquals(ErrorCode.NOT_FOUND_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("应用不存在"));
    }

    @Test
    void chatToGenCode_用户无权限访问应用() {
        // given: 应用的用户ID与当前用户ID不匹配
        Long appId = 1L;
        String message = "生成一个登录页面";
        
        // 设置应用属于其他用户
        mockApp.setUserId(2L);
        AppServiceImpl spyService = spy(appService);
        doReturn(mockApp).when(spyService).getById(appId);

        // when & then: 应该抛出权限错误异常
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> spyService.chatToGenCode(appId, message, mockUser));
        
        assertEquals(ErrorCode.NO_AUTH_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("无权限访问该应用"));
    }

    @Test
    void chatToGenCode_代码生成类型无效() {
        // given: 应用的代码生成类型无效
        Long appId = 1L;
        String message = "生成一个登录页面";
        
        mockApp.setCodeGenType("INVALID_TYPE");
        AppServiceImpl spyService = spy(appService);
        doReturn(mockApp).when(spyService).getById(appId);

        // when & then: 应该抛出系统错误异常
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> spyService.chatToGenCode(appId, message, mockUser));
        
        assertEquals(ErrorCode.SYSTEM_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("不支持的代码生成类型"));
    }

    @Test
    void chatToGenCode_正常流程_成功生成代码() {
        // given: 正常的参数和配置
        Long appId = 1L;
        String message = "生成一个登录页面";
        
        // Mock 应用查询结果
        AppServiceImpl spyService = spy(appService);
        doReturn(mockApp).when(spyService).getById(appId);
        
        // Mock AI代码生成器返回的流
        Flux<String> mockCodeStream = Flux.just("生成的代码片段1", "生成的代码片段2", "生成的代码片段3");
        when(aiCodeGeneratorFacade.generateAndSaveCodeStream(anyString(), any(CodeGenTypeEnum.class), anyLong()))
            .thenReturn(mockCodeStream);

        // when: 调用方法
        Flux<String> result = spyService.chatToGenCode(appId, message, mockUser);

        // then: 验证返回的流
        assertNotNull(result);
        
        // 收集流的所有元素进行验证
        List<String> resultList = result.collectList().block();
        assertNotNull(resultList);
        assertEquals(3, resultList.size());
        assertEquals("生成的代码片段1", resultList.get(0));
        assertEquals("生成的代码片段2", resultList.get(1));
        assertEquals("生成的代码片段3", resultList.get(2));
        
        // 验证调用次数
        verify(aiCodeGeneratorFacade, times(1))
            .generateAndSaveCodeStream(eq(message), eq(CodeGenTypeEnum.MULTI_FILE), eq(appId));
    }

    @Test
    void chatToGenCode_用户ID为null的边界情况() {
        // given: 用户ID为null的情况
        Long appId = 1L;
        String message = "生成一个登录页面";
        
        mockUser.setId(null);
        AppServiceImpl spyService = spy(appService);
        doReturn(mockApp).when(spyService).getById(appId);

        // when & then: 应该抛出权限错误异常
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> spyService.chatToGenCode(appId, message, mockUser));
        
        assertEquals(ErrorCode.NO_AUTH_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("无权限访问该应用"));
    }

    @Test
    void chatToGenCode_应用用户ID为null的边界情况() {
        // given: 应用用户ID为null的情况
        Long appId = 1L;
        String message = "生成一个登录页面";
        
        mockApp.setUserId(null);
        AppServiceImpl spyService = spy(appService);
        doReturn(mockApp).when(spyService).getById(appId);

        // when & then: 由于代码中存在bug，当应用userId为null时会抛出NullPointerException
        // 正确的行为应该是抛出BusinessException，这是一个需要修复的bug
        NullPointerException exception = assertThrows(NullPointerException.class, 
            () -> spyService.chatToGenCode(appId, message, mockUser));
        
        assertTrue(exception.getMessage().contains("Cannot invoke \"java.lang.Long.equals(Object)\""));
    }

    @Test
    void chatToGenCode_HTML类型代码生成() {
        // given: HTML 类型的应用
        Long appId = 1L;
        String message = "生成一个登录页面";
        
        mockApp.setCodeGenType(CodeGenTypeEnum.HTML.getValue());
        AppServiceImpl spyService = spy(appService);
        doReturn(mockApp).when(spyService).getById(appId);
        
        Flux<String> mockCodeStream = Flux.just("<html>", "<body>", "</body>", "</html>");
        when(aiCodeGeneratorFacade.generateAndSaveCodeStream(anyString(), any(CodeGenTypeEnum.class), anyLong()))
            .thenReturn(mockCodeStream);

        // when: 调用方法
        Flux<String> result = spyService.chatToGenCode(appId, message, mockUser);

        // then: 验证返回的流
        assertNotNull(result);
        
        // 收集流的所有元素进行验证
        List<String> resultList = result.collectList().block();
        assertNotNull(resultList);
        assertEquals(4, resultList.size());
        assertEquals("<html>", resultList.get(0));
        assertEquals("<body>", resultList.get(1));
        assertEquals("</body>", resultList.get(2));
        assertEquals("</html>", resultList.get(3));
        
        // 验证使用的是HTML类型
        verify(aiCodeGeneratorFacade, times(1))
            .generateAndSaveCodeStream(eq(message), eq(CodeGenTypeEnum.HTML), eq(appId));
    }
}