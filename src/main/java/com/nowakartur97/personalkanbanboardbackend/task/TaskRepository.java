package com.nowakartur97.personalkanbanboardbackend.task;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface TaskRepository extends R2dbcRepository<TaskEntity, UUID> {

    Flux<TaskEntity> findAllByAssignedTo(UUID assignedToId);
}
