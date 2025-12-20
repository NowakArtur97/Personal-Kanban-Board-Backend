package com.nowakartur97.personalkanbanboardbackend.common;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.UUID;

@Component
public class BaseTaskEventPublisher<R extends BaseTaskResponse> {

    private final Sinks.Many<BaseTaskEvent<R>> sink = Sinks.many().replay().limit(1);
    private final Sinks.Many<UUID> deleteTaskSink = Sinks.many().replay().limit(1);

    @PreDestroy
    public void shutdown() {
        sink.tryEmitComplete();
        deleteTaskSink.tryEmitComplete();
    }

    public Flux<BaseTaskEvent<R>> tasksEvents() {
        return sink.asFlux();
    }

    public Flux<UUID> deleteTasksEvents() {
        return deleteTaskSink.asFlux();
    }

    public void emitTaskEvent(R task, TaskEventType taskEventType) {
        sink.tryEmitNext(new BaseTaskEvent<>(task, taskEventType));
    }

    public void emitDeleteTaskEvent(UUID taskId) {
        deleteTaskSink.tryEmitNext(taskId);
    }
}
