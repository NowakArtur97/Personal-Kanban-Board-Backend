package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskEventPublisher;
import com.nowakartur97.personalkanbanboardbackend.common.TaskEventType;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.UUID;

@Component
public class SubtaskEventPublisher implements BaseTaskEventPublisher<SubtaskResponse> {

    private final Sinks.Many<SubtaskEvent> sink = Sinks.many().replay().limit(1);

    @PreDestroy
    public void shutdown() {
        sink.tryEmitComplete();
    }

    public Flux<SubtaskEvent> tasksEvents() {
        return sink.asFlux();
    }

    @Override
    public void emitTaskEvent(SubtaskResponse task, TaskEventType taskEventType) {
        sink.tryEmitNext(new SubtaskEvent(taskEventType, task));
    }

    @Override
    public void emitDeleteTaskEvent(UUID taskId) {
        sink.tryEmitNext(new SubtaskEvent(TaskEventType.DELETE, taskId));
    }
}
