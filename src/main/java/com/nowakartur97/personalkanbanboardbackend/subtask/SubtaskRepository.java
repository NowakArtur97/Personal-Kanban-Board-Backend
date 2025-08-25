package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface SubtaskRepository extends BaseTaskRepository<SubtaskEntity> {

    Flux<SubtaskEntity> findAllByTaskIdOrderByTargetEndDate(UUID taskId);

    Mono<Void> deleteAllByTaskId(UUID taskId);
}
