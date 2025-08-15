package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskService;
import com.nowakartur97.personalkanbanboardbackend.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Slf4j
public class SubtaskService extends BaseTaskService<SubtaskEntity> {

    private final SubtaskRepository subtaskRepository;

    public SubtaskService(SubtaskRepository subtaskRepository) {
        super(subtaskRepository);
        this.subtaskRepository = subtaskRepository;
    }

    @Override
    public Mono<SubtaskEntity> findById(UUID subtaskId) {

        log.info("Looking up subtask by id: '{}'", subtaskId);

        return subtaskRepository.findById(subtaskId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Subtask", "subtaskId", subtaskId.toString())));
    }

    Flux<SubtaskEntity> findAllByTaskId(UUID taskId) {

        log.info("Looking up subtasks by task id: '{}' ordered by target end date", taskId);

        return subtaskRepository.findAllByTaskIdOrderByTargetEndDate(taskId);
    }

    @Override
    public Mono<SubtaskEntity> save(SubtaskEntity subtask) {

        log.info("Creation of new subtask: {}", subtask);

        return subtaskRepository.save(subtask);
    }

    @Override
    public Mono<SubtaskEntity> update(SubtaskEntity subtask) {

        log.info("Updating subtask: {}", subtask);

        return subtaskRepository.save(subtask);
    }

    Mono<Void> deleteById(UUID subtaskId) {

        log.info("Deleting subtask: by id: '{}'", subtaskId);

        return subtaskRepository.deleteById(subtaskId);
    }
}
