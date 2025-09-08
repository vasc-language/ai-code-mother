package com.spring.aicodemother.core.handler;

import cn.hutool.json.JSONUtil;
import com.spring.aicodemother.ai.model.message.AiResponseMessage;
import com.spring.aicodemother.model.entity.User;
import com.spring.aicodemother.service.ChatHistoryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;

import java.util.function.BooleanSupplier;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class JsonMessageStreamHandlerCancelTest {

    @Test
    void whenCancelled_shouldNotPersistHistory() {
        JsonMessageStreamHandler handler = new JsonMessageStreamHandler();
        ChatHistoryService chatHistoryService = Mockito.mock(ChatHistoryService.class);
        long appId = 1L;
        User user = new User();
        user.setId(1L);

        // Emit a minimal AI_RESPONSE chunk
        String chunk = JSONUtil.toJsonStr(new AiResponseMessage("hello"));
        Flux<String> origin = Flux.just(chunk);

        BooleanSupplier cancelled = () -> true; // simulate user cancel before completion
        handler.handle(origin, chatHistoryService, appId, user, cancelled)
                .collectList()
                .block();

        // Verify no history is written on cancel
        verify(chatHistoryService, never()).addChatMessage(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(), Mockito.anyLong());
    }
}

