package com.nowakartur97.personalkanbanboardbackend.common;

import java.util.UUID;

public interface BaseTaskEventPublisher<R extends BaseTaskResponse> {

    void emitTaskEvent(R task, TaskEventType taskEventType);

    void emitDeleteTaskEvent(UUID taskId);
}
