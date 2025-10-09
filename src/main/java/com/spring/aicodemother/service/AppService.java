package com.spring.aicodemother.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.spring.aicodemother.model.dto.app.AppAddRequest;
import com.spring.aicodemother.model.dto.app.AppQueryRequest;
import com.spring.aicodemother.model.entity.User;
import com.spring.aicodemother.model.vo.AppVO;
import com.spring.aicodemother.model.entity.App;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;
import com.spring.aicodemother.core.control.GenerationControlRegistry.GenerationControl;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
public interface AppService extends IService<App> {
    void generateAppScreenshotAsync(Long appId, String appUrl);

    AppVO getAppVO(App app);

    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    List<AppVO> getAppVOList(List<App> appList);

    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    // New signature supporting manual cancellation
    Flux<String> chatToGenCode(Long appId, String message, User loginUser, GenerationControl control);

    // New signature supporting dynamic model selection
    Flux<String> chatToGenCode(Long appId, String message, User loginUser, GenerationControl control, String modelKey);

    Long createApp(AppAddRequest appAddRequest, User loginUser);

    String deployApp(Long appId, User loginUser);
}
