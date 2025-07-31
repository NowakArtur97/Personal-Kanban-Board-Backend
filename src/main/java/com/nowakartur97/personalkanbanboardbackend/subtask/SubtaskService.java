package com.nowakartur97.personalkanbanboardbackend.subtask;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubtaskService {

    private final SubtaskRepository subtaskRepository;

    public Flux<SubtaskEntity> findAllByTaskId(UUID taskId) {

        log.info("Looking up subtasks by task id: '{}'", taskId);

        return subtaskRepository.findAllByTaskId(taskId);
    }

    public Mono<SubtaskEntity> save(SubtaskEntity subtask) {

        log.info("Creation of new subtask: {}", subtask);

        return subtaskRepository.save(subtask);
    }
}
