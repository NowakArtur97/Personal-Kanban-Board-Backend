package com.nowakartur97.personalkanbanboardbackend.common;

import lombok.Getter;

import java.util.UUID;

@Getter
public class BaseTaskEvent<R extends BaseTaskResponse> {

    private final TaskEventType taskEventType;
    private R task;
    private UUID taskId;

    public BaseTaskEvent(TaskEventType taskEventType, R task) {
        this.taskEventType = taskEventType;
        this.task = task;
    }

    public BaseTaskEvent(TaskEventType taskEventType, UUID taskId) {
        this.taskEventType = taskEventType;
        this.taskId = taskId;
    }

    public static <R extends BaseTaskResponse> BaseTaskEvent<R> create(R task) {
        return new BaseTaskEvent<>(TaskEventType.CREATE, task);
    }

    public static <R extends BaseTaskResponse> BaseTaskEvent<R> update(R task) {
        return new BaseTaskEvent<>(TaskEventType.UPDATE, task);
    }

    public static <R extends BaseTaskResponse> BaseTaskEvent<R> delete(UUID taskId) {
        return new BaseTaskEvent<>(TaskEventType.DELETE, taskId);
    }
}
