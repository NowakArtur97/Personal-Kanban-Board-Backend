package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.common.TaskEventType;
import lombok.Getter;

import java.util.UUID;

@Getter
public class SubtaskEvent {

    private final TaskEventType taskEventType;
    private SubtaskResponse task;
    private UUID taskId;

    public SubtaskEvent(TaskEventType taskEventType, SubtaskResponse task) {
        this.taskEventType = taskEventType;
        this.task = task;
    }

    public SubtaskEvent(TaskEventType taskEventType, UUID taskId) {
        this.taskEventType = taskEventType;
        this.taskId = taskId;
    }
}
