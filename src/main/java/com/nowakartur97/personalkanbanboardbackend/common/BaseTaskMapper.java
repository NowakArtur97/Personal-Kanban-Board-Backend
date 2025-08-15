package com.nowakartur97.personalkanbanboardbackend.common;

import com.nowakartur97.personalkanbanboardbackend.exception.ResourceNotFoundException;
import com.nowakartur97.personalkanbanboardbackend.task.TaskDTO;
import com.nowakartur97.personalkanbanboardbackend.task.TaskPriority;
import com.nowakartur97.personalkanbanboardbackend.task.TaskStatus;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public abstract class BaseTaskMapper<E extends BaseTaskEntity, R extends BaseTaskResponse> {

    public final E mapToEntity(UUID taskId, TaskDTO taskDTO, UUID createdBy) {
        return mapToEntity(taskId, taskDTO, createdBy, createdBy);
    }

    public abstract E mapToEntity(UUID taskId, TaskDTO taskDTO, UUID createdBy, UUID assignedTo);

    public final void setEntityFields(BaseTaskEntity taskEntity, TaskDTO taskDTO, UUID createdBy, UUID assignedTo) {
        taskEntity.setTitle(taskDTO.getTitle());
        taskEntity.setDescription(taskDTO.getDescription());
        taskEntity.setStatus(taskDTO.getStatus() != null ? taskDTO.getStatus() : TaskStatus.READY_TO_START);
        taskEntity.setPriority(taskDTO.getPriority() != null ? taskDTO.getPriority() : TaskPriority.LOW);
        taskEntity.setTargetEndDate(taskDTO.getTargetEndDate());
        taskEntity.setAssignedTo(assignedTo);
        taskEntity.setCreatedBy(createdBy);
        // TODO: Check in Postgres to see if the date is auto-populated
        taskEntity.setCreatedOn(Instant.now());
    }

    public final E updateEntity(E taskEntity, TaskDTO taskDTO, UUID updatedBy) {
        return updateEntity(taskEntity, taskDTO, updatedBy, updatedBy);
    }

    public final E updateEntity(E taskEntity, TaskDTO taskDTO, UUID updatedBy, UUID assignedTo) {
        taskEntity.setTitle(taskDTO.getTitle());
        taskEntity.setDescription(taskDTO.getDescription());
        taskEntity.setStatus(taskDTO.getStatus() != null ? taskDTO.getStatus() : TaskStatus.READY_TO_START);
        taskEntity.setPriority(taskDTO.getPriority() != null ? taskDTO.getPriority() : TaskPriority.LOW);
        taskEntity.setTargetEndDate(taskDTO.getTargetEndDate());
        updateUserAssignedToEntity(taskEntity, updatedBy, assignedTo);
        return taskEntity;
    }

    public final E updateUserAssignedToEntity(E taskEntity, UUID updatedBy, UUID assignedTo) {
        taskEntity.setAssignedTo(assignedTo);
        // TODO: Check in Postgres to see if the date is auto-populated
        taskEntity.setAssignedTo(assignedTo);
        taskEntity.setUpdatedBy(updatedBy);
        taskEntity.setUpdatedOn(Instant.now());
        return taskEntity;
    }

    public final R mapToResponse(E taskEntity, String createdBy) {
        return mapToResponse(taskEntity, createdBy, createdBy);
    }

    public final R mapToResponse(E taskEntity, String createdBy, String assignedTo) {
        return mapToResponse(taskEntity, createdBy, null, assignedTo);
    }

    public abstract R mapToResponse(E taskEntity, String createdBy, String updatedBy, String assignedTo);

    public final R mapToResponse(E taskEntity, List<UserEntity> users) {
        String createdBy = getUsernameByUserId(taskEntity.getCreatedBy(), users);
        String updatedBy = getUsernameByUserId(taskEntity.getUpdatedBy(), users);
        String assignedTo = getUsernameByUserId(taskEntity.getAssignedTo(), users);
        return mapToResponse(taskEntity, createdBy, updatedBy, assignedTo);
    }

    public final void setResponseFields(R taskResponse, E taskEntity, String createdBy, String updatedBy, String assignedTo) {
        taskResponse.setTitle(taskEntity.getTitle());
        taskResponse.setDescription(taskEntity.getDescription());
        taskResponse.setStatus(taskEntity.getStatus());
        taskResponse.setPriority(taskEntity.getPriority());
        taskResponse.setTargetEndDate(taskEntity.getTargetEndDate());
        taskResponse.setCreatedBy(createdBy);
        taskResponse.setCreatedOn(taskEntity.getCreatedOn().toString());
        taskResponse.setCreatedBy(createdBy);
        taskResponse.setUpdatedOn(taskEntity.getUpdatedOn() != null ? taskEntity.getUpdatedOn().toString() : null);
        taskResponse.setUpdatedBy(updatedBy);
        taskResponse.setAssignedTo(assignedTo);
    }

    protected final String getUsernameByUserId(UUID userId, List<UserEntity> users) {
        if (userId == null) {
            return null;
        }
        return users.stream().filter(user -> user.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId.toString()))
                .getUsername();
    }
}
