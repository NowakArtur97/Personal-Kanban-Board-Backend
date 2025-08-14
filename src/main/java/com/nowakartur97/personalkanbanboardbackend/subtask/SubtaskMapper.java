package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskMapper;
import com.nowakartur97.personalkanbanboardbackend.task.TaskDTO;
import com.nowakartur97.personalkanbanboardbackend.task.TaskPriority;
import com.nowakartur97.personalkanbanboardbackend.task.TaskStatus;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class SubtaskMapper extends BaseTaskMapper {

    SubtaskEntity mapToEntity(UUID taskId, TaskDTO taskDTO, UUID createdBy) {
        return mapToEntity(taskId, taskDTO, createdBy, createdBy);
    }

    SubtaskEntity mapToEntity(UUID taskId, TaskDTO taskDTO, UUID createdBy, UUID assignedTo) {
        return SubtaskEntity.builder()
                .taskId(taskId)
                .title(taskDTO.getTitle())
                .description(taskDTO.getDescription())
                .status(taskDTO.getStatus() != null ? taskDTO.getStatus() : TaskStatus.READY_TO_START)
                .priority(taskDTO.getPriority() != null ? taskDTO.getPriority() : TaskPriority.LOW)
                .targetEndDate(taskDTO.getTargetEndDate())
                .assignedTo(assignedTo)
                .createdBy(createdBy)
                // TODO: Check in Postgres to see if the date is auto-populated
                .createdOn(Instant.now())
                .build();
    }

    SubtaskResponse mapToResponse(SubtaskEntity subtaskEntity, String createdBy) {
        return mapToResponse(subtaskEntity, createdBy, createdBy);
    }

    SubtaskResponse mapToResponse(SubtaskEntity subtaskEntity, String createdBy, String assignedTo) {
        return mapToResponse(subtaskEntity, createdBy, null, assignedTo);
    }

    SubtaskResponse mapToResponse(SubtaskEntity subtaskEntity, List<UserEntity> users) {
        String createdBy = getUsernameByUserId(subtaskEntity.getCreatedBy(), users);
        String updatedBy = getUsernameByUserId(subtaskEntity.getUpdatedBy(), users);
        String assignedTo = getUsernameByUserId(subtaskEntity.getAssignedTo(), users);
        return mapToResponse(subtaskEntity, createdBy, updatedBy, assignedTo);
    }

    SubtaskResponse mapToResponse(SubtaskEntity subtaskEntity, String createdBy, String updatedBy, String assignedTo) {
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
