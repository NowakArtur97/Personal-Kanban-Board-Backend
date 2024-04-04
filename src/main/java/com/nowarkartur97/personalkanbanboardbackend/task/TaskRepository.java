package com.nowarkartur97.personalkanbanboardbackend.task;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskRepository extends R2dbcRepository<TaskEntity, UUID> {
}
