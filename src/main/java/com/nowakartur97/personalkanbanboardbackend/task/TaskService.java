package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.exception.ResourceNotFoundException;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserService;
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
    private final UserService userService;

    public Mono<TaskEntity> saveTask(TaskEntity task) {

        log.info("Creation of new task: {}", task);

        return taskRepository.save(task);
    }

    public Mono<TaskEntity> updateTask(TaskEntity task) {

        log.info("Updating task: {}", task);

        return taskRepository.save(task);
    }

    public Mono<TaskEntity> findById(UUID taskId) {

        log.info("Looking up task by id: '{}'", taskId);

        return taskRepository.findById(taskId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Task", "taskId", taskId.toString())));
    }

    public Flux<TaskEntity> findAllByAssignedTo(String username) {

        log.info("Looking up tasks for user: '{}'", username);

        return userService.findByUsername(username)
                .map(UserEntity::getUserId)
                .flatMapMany(taskRepository::findAllByAssignedTo);
    }
}
