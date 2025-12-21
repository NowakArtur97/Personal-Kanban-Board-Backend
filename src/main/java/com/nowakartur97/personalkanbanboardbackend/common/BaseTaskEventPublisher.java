package com.nowakartur97.personalkanbanboardbackend.common;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
public class BaseTaskEventPublisher<R extends BaseTaskResponse> {

    private final Sinks.Many<BaseTaskEvent<R>> sink = Sinks.many().replay().limit(1);

    @PreDestroy
    public void shutdown() {
        sink.tryEmitComplete();
    }

    public Flux<BaseTaskEvent<R>> tasksEvents() {
        return sink.asFlux();
    }

    public void emitTaskEvent(BaseTaskEvent<R> taskEvent) {
        sink.tryEmitNext(taskEvent);
    }
}
