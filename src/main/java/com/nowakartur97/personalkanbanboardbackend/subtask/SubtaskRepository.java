package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskRepository;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface SubtaskRepository extends R2dbcRepository<SubtaskEntity, UUID>, BaseTaskRepository<SubtaskEntity> {

    Flux<SubtaskEntity> findAllByTaskIdOrderByTargetEndDate(UUID taskId);
}
