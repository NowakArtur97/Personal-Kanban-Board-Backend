package com.nowakartur97.personalkanbanboardbackend.common;

import java.util.UUID;

public interface BaseTaskEventPublisher<R extends BaseTaskResponse> {

    void emitTaskEvent(TaskEventType taskEventType, R task);

    void emitDeleteTaskEvent(UUID taskId);
}
