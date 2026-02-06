package com.spring.aicodemother.core.control;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 用于协调手动取消流式生成的注册表。
 * 每一次生成运行都由一个 runId 来标识。
 */
@Component
public class GenerationControlRegistry {

    public static class GenerationControl {
        private final Sinks.Many<Integer> cancelSink = Sinks.many().replay().latest();
        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        private volatile String ownerUserId;
        private volatile Long appId;

        public reactor.core.publisher.Flux<Integer> cancelFlux() {
            return cancelSink.asFlux();
        }

        public void cancel() {
            if (cancelled.compareAndSet(false, true)) {
                // 不能向 Reactor sink 发射 null，否则会触发 NPE
                cancelSink.tryEmitNext(1);
                cancelSink.tryEmitComplete();
            }
        }

        public boolean isCancelled() {
            return cancelled.get();
        }

        public String getOwnerUserId() {
            return ownerUserId;
        }

        public void setOwnerIfAbsent(String ownerUserId, Long appId) {
            if (this.ownerUserId == null) {
                this.ownerUserId = ownerUserId;
            }
            if (this.appId == null) {
                this.appId = appId;
            }
        }

        public Long getAppId() {
            return appId;
        }
    }

    private final Map<String, GenerationControl> controls = new ConcurrentHashMap<>();

    public GenerationControl acquire(String runId) {
        if (runId == null || runId.isEmpty()) {
            throw new IllegalArgumentException("runId must not be empty");
        }
        return controls.computeIfAbsent(runId, k -> new GenerationControl());
    }

    public GenerationControl register(String runId, String ownerUserId, Long appId) {
        GenerationControl c = acquire(runId);
        c.setOwnerIfAbsent(ownerUserId, appId);
        return c;
    }

    public void cancel(String runId) {
        GenerationControl control = controls.get(runId);
        if (control != null) {
            control.cancel();
        }
    }

    public boolean isCancelled(String runId) {
        GenerationControl control = controls.get(runId);
        return control != null && control.isCancelled();
    }

    public void remove(String runId) {
        controls.remove(runId);
    }

    public GenerationControl get(String runId) {
        return controls.get(runId);
    }
}
