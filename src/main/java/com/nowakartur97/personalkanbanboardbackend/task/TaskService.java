package com.nowakartur97.personalkanbanboardbackend.task;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public Mono<TaskEntity> saveTask(TaskEntity task){
        return taskRepository.save(task);
    }
}
