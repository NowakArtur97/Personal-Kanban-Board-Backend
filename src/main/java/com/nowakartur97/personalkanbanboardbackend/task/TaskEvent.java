package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskEvent;
import com.nowakartur97.personalkanbanboardbackend.common.TaskEventType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class TaskEvent implements BaseTaskEvent {

    private TaskEventType taskEventType;
    private TaskResponse task;
    private UUID taskId;

    public TaskEvent(TaskEventType taskEventType, TaskResponse task) {
        this.taskEventType = taskEventType;
        this.task = task;
    }

    public TaskEvent(TaskEventType taskEventType, UUID taskId) {
        this.taskEventType = taskEventType;
        this.taskId = taskId;
    }
}
