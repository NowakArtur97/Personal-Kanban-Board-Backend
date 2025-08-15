package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskResponse;
import com.nowakartur97.personalkanbanboardbackend.task.TaskPriority;
import com.nowakartur97.personalkanbanboardbackend.task.TaskStatus;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class SubtaskResponse extends BaseTaskResponse {

    private final UUID subtaskId;

    public SubtaskResponse(UUID subtaskId, UUID taskId, String title, String description,
                           TaskStatus status, TaskPriority priority, LocalDate targetEndDate,
                           String createdBy, String createdOn, String updatedBy, String updatedOn, String assignedTo) {
        super(taskId, title, description, status, priority, targetEndDate, createdBy, createdOn, updatedBy, updatedOn, assignedTo);
        this.subtaskId = subtaskId;
    }
}
