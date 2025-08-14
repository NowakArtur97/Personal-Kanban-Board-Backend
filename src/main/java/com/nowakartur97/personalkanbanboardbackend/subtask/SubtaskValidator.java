package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskValidator;
import com.nowakartur97.personalkanbanboardbackend.exception.ResourceNotFoundException;
import com.nowakartur97.personalkanbanboardbackend.task.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SubtaskValidator implements BaseTaskValidator {

    private final TaskService taskService;

    @Override
    public Mono<Boolean> validate(UUID taskId) {
        return taskService.existsByTaskId(taskId)
                .map(exists -> {
                    if (!exists) {
                        throw new ResourceNotFoundException("Task", "taskId", taskId.toString());
                    }
                    return true;
                });
    }
}
