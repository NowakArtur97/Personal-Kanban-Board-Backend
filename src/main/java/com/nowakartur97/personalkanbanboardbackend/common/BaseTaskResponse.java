package com.nowakartur97.personalkanbanboardbackend.common;

import com.nowakartur97.personalkanbanboardbackend.task.TaskPriority;
import com.nowakartur97.personalkanbanboardbackend.task.TaskStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
public abstract class BaseTaskResponse {

    private final UUID taskId;
    private final String title;
    private final String description;
    private final TaskStatus status;
    private final TaskPriority priority;
    private final LocalDate targetEndDate;
    private final String createdBy;
    private final String createdOn;
    private final String updatedBy;
    private final String updatedOn;
    private final String assignedTo;
}
