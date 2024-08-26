package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.exception.ResourceNotFoundException;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class TaskMapper {

    public TaskEntity mapToEntity(TaskDTO taskDTO, UUID createdBy) {
        return mapToEntity(taskDTO, createdBy, createdBy);
    }

    public TaskEntity mapToEntity(TaskDTO taskDTO, UUID createdBy, UUID assignedTo) {
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

    public TaskEntity updateEntity(TaskEntity taskEntity, TaskDTO taskDTO, UUID updatedBy) {
        return updateEntity(taskEntity, taskDTO, updatedBy, updatedBy);
    }

    public TaskEntity updateEntity(TaskEntity taskEntity, TaskDTO taskDTO, UUID updatedBy, UUID assignedTo) {
        taskEntity.setTitle(taskDTO.getTitle());
        taskEntity.setDescription(taskDTO.getDescription());
        taskEntity.setStatus(taskDTO.getStatus() != null ? taskDTO.getStatus() : TaskStatus.READY_TO_START);
        taskEntity.setPriority(taskDTO.getPriority() != null ? taskDTO.getPriority() : TaskPriority.LOW);
        taskEntity.setTargetEndDate(taskDTO.getTargetEndDate());
        taskEntity.setAssignedTo(assignedTo);
        taskEntity.setUpdatedBy(updatedBy);
        // TODO: Check in Postgres to see if the date is auto-populated
        taskEntity.setUpdatedOn(Instant.now());
        return taskEntity;
    }

    public TaskResponse mapToResponse(TaskEntity taskEntity, String createdBy) {
        return mapToResponse(taskEntity, createdBy, createdBy);
    }

    public TaskResponse mapToResponse(TaskEntity taskEntity, String createdBy, String assignedTo) {
        return mapToResponse(taskEntity, createdBy, null, assignedTo);
    }

    public TaskResponse mapToResponse(TaskEntity taskEntity, List<UserEntity> users) {
        String createdBy = getUsernameByUserId(taskEntity.getCreatedBy(), users);
        String updatedBy = getUsernameByUserId(taskEntity.getUpdatedBy(), users);
        String assignedTo = getUsernameByUserId(taskEntity.getAssignedTo(), users);
        return mapToResponse(taskEntity, createdBy, updatedBy, assignedTo);
    }

    public TaskResponse mapToResponse(TaskEntity taskEntity, String createdBy, String updatedBy, String assignedTo) {
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
                assignedTo
        );
    }

    private String getUsernameByUserId(UUID userId, List<UserEntity> users) {
        if (userId == null) {
            return null;
        }
        return users.stream().filter(user -> user.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId.toString()))
                .getUsername();
    }
}
