package com.nowakartur97.personalkanbanboardbackend.common;

import com.nowakartur97.personalkanbanboardbackend.exception.ResourceNotFoundException;
import com.nowakartur97.personalkanbanboardbackend.task.TaskDTO;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;

import java.util.List;
import java.util.UUID;

public abstract class BaseTaskMapper<E extends BaseTaskEntity, R extends BaseTaskResponse> {

    public abstract R mapToResponse(E task, List<UserEntity> t1);

    public abstract R mapToResponse(E task, String createdBy);

    public abstract R mapToResponse(E task, String createdBy, String assignedTo);

    public abstract E mapToEntity(UUID taskId, TaskDTO dto, UUID createdBy);

    public abstract E mapToEntity(UUID taskId, TaskDTO dto, UUID createdBy, UUID assignedTo);

    protected String getUsernameByUserId(UUID userId, List<UserEntity> users) {
        if (userId == null) {
            return null;
        }
        return users.stream().filter(user -> user.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId.toString()))
                .getUsername();
    }
}
