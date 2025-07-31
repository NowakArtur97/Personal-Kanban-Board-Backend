package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskResponse;
import com.nowakartur97.personalkanbanboardbackend.subtask.SubtaskResponse;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
public class TaskResponse extends BaseTaskResponse {

    private final List<SubtaskResponse> subtasks;

    public TaskResponse(UUID taskId, String title, String description,
                        TaskStatus status, TaskPriority priority, LocalDate targetEndDate,
                        String createdBy, String createdOn, String updatedBy, String updatedOn, String assignedTo,
                        List<SubtaskResponse> subtasks) {
        super(taskId, title, description, status, priority, targetEndDate, createdBy, createdOn, updatedBy, updatedOn, assignedTo);
        this.subtasks = subtasks;
    }
}
