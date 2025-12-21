package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskEvent;
import com.nowakartur97.personalkanbanboardbackend.common.TaskEventType;

import java.util.UUID;

public class SubtaskEvent extends BaseTaskEvent<SubtaskResponse> {

    public SubtaskEvent(TaskEventType taskEventType, SubtaskResponse task) {
        super(taskEventType, task);
    }

    public SubtaskEvent(TaskEventType taskEventType, UUID taskId) {
        super(taskEventType, taskId);
    }
}
