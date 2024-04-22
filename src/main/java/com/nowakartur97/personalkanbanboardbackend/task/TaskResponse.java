package com.nowakartur97.personalkanbanboardbackend.task;

import java.time.LocalDate;
import java.util.UUID;

public record TaskResponse(UUID taskId,
                           String title,
                           String description,
                           TaskStatus status,
                           TaskPriority priority,
                           LocalDate targetEndDate,
                           String createdBy,
                           LocalDate createdOn,
                           String updatedBy,
                           LocalDate updatedOn,
                           String assignedTo
) {
}
