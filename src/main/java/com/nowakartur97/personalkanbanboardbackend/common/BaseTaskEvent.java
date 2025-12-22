package com.nowakartur97.personalkanbanboardbackend.common;

import lombok.Getter;

import java.util.UUID;

@Getter
public class BaseTaskEvent<R extends BaseTaskResponse> {

    private final TaskEventType taskEventType;
    private final boolean isEventForTask;
    private R task;
    private UUID taskId;

    public BaseTaskEvent(TaskEventType taskEventType, R task, boolean isEventForTask) {
        this.taskEventType = taskEventType;
        this.task = task;
        this.isEventForTask = isEventForTask;
    }

    public BaseTaskEvent(TaskEventType taskEventType, UUID taskId, boolean isEventForTask) {
        this.taskEventType = taskEventType;
        this.taskId = taskId;
        this.isEventForTask = isEventForTask;
    }

    public static <R extends BaseTaskResponse> BaseTaskEvent<R> create(R task, boolean isEventForTask) {
        return new BaseTaskEvent<>(TaskEventType.CREATE, task, isEventForTask);
    }

    public static <R extends BaseTaskResponse> BaseTaskEvent<R> update(R task, boolean isEventForTask) {
        return new BaseTaskEvent<>(TaskEventType.UPDATE, task, isEventForTask);
    }

    public static <R extends BaseTaskResponse> BaseTaskEvent<R> delete(UUID taskId, boolean isEventForTask) {
        return new BaseTaskEvent<>(TaskEventType.DELETE, taskId, isEventForTask);
    }
}
