package com.spring.aicodemother.aop;

import com.spring.aicodemother.annotation.AuthCheck;
import com.spring.aicodemother.exception.BusinessException;
import com.spring.aicodemother.exception.ErrorCode;
import com.spring.aicodemother.model.entity.User;
import com.spring.aicodemother.model.enums.UserRoleEnum;
import com.spring.aicodemother.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 权限校验
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 通过 AOP 切面，在执行带有 @AuthCheck 注解的方法前，进行权限校验。
     *
     * @param joinPoint AOP 切点，代表即将被执行的目标方法。
     * @param authCheck 目标方法上的 @AuthCheck 注解实例，包含了所需的权限信息（如 mustRole）。
     * @return 返回目标方法的执行结果。
     * @throws Throwable 如果目标方法执行过程中抛出异常，则向上抛出。
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 1. 从注解中获取方法执行所必需的角色 (e.g., "admin")。
        String mustRole = authCheck.mustRole();
        
        // 2. 通过 Spring 上下文持有器获取当前的 HTTP 请求对象。
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        
        // 3. 从请求的 Session 中获取当前登录的用户信息。
        //    如果用户未登录，`getLoginUser` 方法内部会直接抛出 `BusinessException`。
        User loginUser = userService.getLoginUser(request);
        
        // 4. 将注解中定义的角色字符串转换为对应的枚举类型。
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        
        // 5. 如果注解中没有指定一个有效的必须角色（即 mustRoleEnum 为 null），
        //    则认为该接口不需要任何权限，直接放行。
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }
        
        // --- 以下是需要进行权限校验的逻辑 ---
        
        // 6. 获取当前登录用户的角色，并转换为枚举类型。
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        
        // 7. 如果用户的角色在系统中无效（例如，数据库中的 role 字段值不规范），则判定为无权限。
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        
        // 8. 核心校验逻辑：如果方法要求必须是管理员(ADMIN)角色，而当前用户的角色不是管理员，则抛出无权限异常。
        //    注意：此处的逻辑只处理了“必须是管理员”的场景，可以扩展以支持更复杂的角色层级。
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum)) {
            if (!UserRoleEnum.ADMIN.equals(userRoleEnum)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
        
        // 9. 如果所有权限校验都通过，则执行原始方法并返回其结果。
        return joinPoint.proceed();
    }
}


