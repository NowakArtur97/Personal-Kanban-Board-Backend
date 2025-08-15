package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskService;
import com.nowakartur97.personalkanbanboardbackend.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Slf4j
public class TaskService extends BaseTaskService<TaskEntity> {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        super(taskRepository);
        this.taskRepository = taskRepository;
    }

    @Override
    public Mono<TaskEntity> findById(UUID taskId) {

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

    public Mono<Boolean> existsByTaskId(UUID taskId) {

        log.info("Checking if task exists by task id: '{}'", taskId);

        return taskRepository.existsById(taskId);
    }

    @Override
    public Mono<TaskEntity> save(TaskEntity task) {

        log.info("Creation of new task: {}", task);

        return taskRepository.save(task);
    }

    @Override
    public Mono<TaskEntity> update(TaskEntity task) {

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
