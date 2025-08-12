package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;

    Mono<TaskEntity> findById(UUID taskId) {

        log.info("Looking up task by id: '{}'", taskId);

        return taskRepository.findById(taskId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Task", "taskId", taskId.toString())));
    }

    Flux<TaskEntity> findAll() {

        log.info("Looking up all tasks ordered by target end date");

        return taskRepository.findAllByOrderByTargetEndDate();
    }

    Flux<TaskEntity> findAllByAssignedTo(UUID assignedToId) {

        log.info("Looking up all tasks for assigned to user with id '{}' ordered by target end date", assignedToId);

        return taskRepository.findAllByAssignedToOrderByTargetEndDate(assignedToId);
    }

    public Mono<TaskEntity> save(TaskEntity task) {

        log.info("Creation of new task: {}", task);

        return taskRepository.save(task);
    }

    Mono<TaskEntity> update(TaskEntity task) {

        log.info("Updating task: {}", task);

        return taskRepository.save(task);
    }

    Mono<TaskEntity> updateAssignedTo(TaskEntity task) {

        log.info("Updating user assigned to task: {}", task);

        return taskRepository.save(task);
    }

    Mono<Void> deleteById(UUID taskId) {

        log.info("Deleting task: by id: '{}'", taskId);

        return taskRepository.deleteById(taskId);
    }

    Mono<Void> deleteAll() {

        log.info("Deleting all tasks");

        return taskRepository.deleteAll();
    }
}
