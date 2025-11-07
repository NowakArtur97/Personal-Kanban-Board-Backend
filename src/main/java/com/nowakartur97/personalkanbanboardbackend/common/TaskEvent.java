package com.nowakartur97.personalkanbanboardbackend.common;

import com.nowakartur97.personalkanbanboardbackend.task.TaskResponse;

public class TaskEvent extends BaseTaskEvent<TaskResponse> {

    public TaskEvent(TaskResponse taskResponse, TaskEventType taskEventType) {
        super(taskResponse, taskEventType);
    }
}
