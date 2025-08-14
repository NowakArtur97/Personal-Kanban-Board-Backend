package com.nowakartur97.personalkanbanboardbackend.common;

import com.nowakartur97.personalkanbanboardbackend.exception.ResourceNotFoundException;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;

import java.util.List;
import java.util.UUID;

public abstract class BaseTaskMapper<R extends BaseTaskResponse, E extends BaseTaskEntity> {

    public abstract R mapToResponse(E task, List<UserEntity> t1);

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
