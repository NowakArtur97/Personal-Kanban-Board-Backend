package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;

    public Mono<TaskEntity> saveTask(TaskEntity task) {
        return taskRepository.save(task);
    }

    public Flux<TaskEntity> findAllTasksForUser(String username) {

        log.info("Looking up tasks for user: {}", username);

        return userService.findByUsername(username)
                .map(UserEntity::getUserId)
                .flatMapMany(taskRepository::findAllByAssignedTo);
    }
}
