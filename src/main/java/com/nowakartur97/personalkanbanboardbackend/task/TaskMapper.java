package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskMapper;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
class TaskMapper extends BaseTaskMapper<TaskResponse, TaskEntity> {

    TaskEntity mapToEntity(TaskDTO taskDTO, UUID createdBy) {
        return mapToEntity(taskDTO, createdBy, createdBy);
    }

    TaskEntity mapToEntity(TaskDTO taskDTO, UUID createdBy, UUID assignedTo) {
        return TaskEntity.builder()
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

    TaskEntity updateEntity(TaskEntity taskEntity, TaskDTO taskDTO, UUID updatedBy) {
        return updateEntity(taskEntity, taskDTO, updatedBy, updatedBy);
    }

    TaskEntity updateEntity(TaskEntity taskEntity, TaskDTO taskDTO, UUID updatedBy, UUID assignedTo) {
        taskEntity.setTitle(taskDTO.getTitle());
        taskEntity.setDescription(taskDTO.getDescription());
        taskEntity.setStatus(taskDTO.getStatus() != null ? taskDTO.getStatus() : TaskStatus.READY_TO_START);
        taskEntity.setPriority(taskDTO.getPriority() != null ? taskDTO.getPriority() : TaskPriority.LOW);
        taskEntity.setTargetEndDate(taskDTO.getTargetEndDate());
        updateUserAssignedToEntity(taskEntity, updatedBy, assignedTo);
        return taskEntity;
    }

    TaskEntity updateUserAssignedToEntity(TaskEntity taskEntity, UUID updatedBy, UUID assignedTo) {
        taskEntity.setAssignedTo(assignedTo);
        // TODO: Check in Postgres to see if the date is auto-populated
        taskEntity.setAssignedTo(assignedTo);
        taskEntity.setUpdatedBy(updatedBy);
        taskEntity.setUpdatedOn(Instant.now());
        return taskEntity;
    }

    TaskResponse mapToResponse(TaskEntity taskEntity, String createdBy) {
        return mapToResponse(taskEntity, createdBy, createdBy);
    }

    TaskResponse mapToResponse(TaskEntity taskEntity, String createdBy, String assignedTo) {
        return mapToResponse(taskEntity, createdBy, null, assignedTo);
    }

    @Override
    public TaskResponse mapToResponse(TaskEntity taskEntity, List<UserEntity> users) {
        String createdBy = getUsernameByUserId(taskEntity.getCreatedBy(), users);
        String updatedBy = getUsernameByUserId(taskEntity.getUpdatedBy(), users);
        String assignedTo = getUsernameByUserId(taskEntity.getAssignedTo(), users);
        return mapToResponse(taskEntity, createdBy, updatedBy, assignedTo);
    }

    TaskResponse mapToResponse(TaskEntity taskEntity, String createdBy, String updatedBy, String assignedTo) {
        return new TaskResponse(
                taskEntity.getTaskId(),
                taskEntity.getTitle(),
                taskEntity.getDescription(),
                taskEntity.getStatus(),
                taskEntity.getPriority(),
                taskEntity.getTargetEndDate(),
                createdBy,
                taskEntity.getCreatedOn().toString(),
                updatedBy,
                taskEntity.getUpdatedOn() != null ? taskEntity.getUpdatedOn().toString() : null,
                assignedTo,
                Collections.emptyList()
        );
    }
}
