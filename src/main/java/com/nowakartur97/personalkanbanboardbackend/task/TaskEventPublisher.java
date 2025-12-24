package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskEventPublisher;
import com.nowakartur97.personalkanbanboardbackend.common.TaskEventType;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.UUID;

@Component
public class TaskEventPublisher implements BaseTaskEventPublisher<TaskResponse> {

    private final Sinks.Many<TaskEvent> sink = Sinks.many().replay().limit(1);

    @PreDestroy
    public void shutdown() {
        sink.tryEmitComplete();
    }

    public Flux<TaskEvent> tasksEvents() {
        return sink.asFlux();
    }

    public void emitTaskEvent(TaskEvent taskEvent) {
        sink.tryEmitNext(taskEvent);
    }

    @Override
    public void emitTaskEvent(TaskResponse task, TaskEventType taskEventType) {
        sink.tryEmitNext(new TaskEvent(taskEventType, task));
    }

    @Override
    public void emitTaskEvent(UUID taskId) {
        sink.tryEmitNext(new TaskEvent(TaskEventType.DELETE, taskId));
    }
}
