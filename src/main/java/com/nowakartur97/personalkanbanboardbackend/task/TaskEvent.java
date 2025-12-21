package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskEvent;
import com.nowakartur97.personalkanbanboardbackend.common.TaskEventType;

import java.util.UUID;

public class TaskEvent extends BaseTaskEvent<TaskResponse> {

    public TaskEvent(TaskEventType taskEventType, TaskResponse task) {
        super(taskEventType, task);
    }

    public TaskEvent(TaskEventType taskEventType, UUID taskId) {
        super(taskEventType, taskId);
    }
}
