package com.nowakartur97.personalkanbanboardbackend.subtask;

import org.springframework.stereotype.Component;

@Component
public class SubtaskMapper {

    public SubtaskResponse mapToResponse(SubtaskEntity subtaskEntity, String createdBy, String updatedBy, String assignedTo) {
        return new SubtaskResponse(
                subtaskEntity.getSubtaskId(),
                subtaskEntity.getTaskId(),
                subtaskEntity.getTitle(),
                subtaskEntity.getDescription(),
                subtaskEntity.getStatus(),
                subtaskEntity.getPriority(),
                subtaskEntity.getTargetEndDate(),
                createdBy,
                subtaskEntity.getCreatedOn().toString(),
                updatedBy,
                subtaskEntity.getUpdatedOn() != null ? subtaskEntity.getUpdatedOn().toString() : null,
                assignedTo
        );
    }
}
