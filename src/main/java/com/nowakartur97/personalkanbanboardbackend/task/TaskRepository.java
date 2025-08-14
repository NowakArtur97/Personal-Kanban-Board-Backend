package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskRepository;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface TaskRepository extends R2dbcRepository<TaskEntity, UUID>, BaseTaskRepository<TaskEntity> {

    Flux<TaskEntity> findAllByOrderByTargetEndDate();

    Flux<TaskEntity> findAllByAssignedToOrderByTargetEndDate(UUID assignedToId);
}
