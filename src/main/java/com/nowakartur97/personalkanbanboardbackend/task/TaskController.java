package com.nowakartur97.personalkanbanboardbackend.task;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @QueryMapping
    @PreAuthorize("hasAuthority('USER')")
    public Flux<TaskResponse> tasks(@Argument String username) {
        return taskService.getAllTasksForUser(username)
                .map(task -> this.mapToResponse(task, username));
    }

    private TaskResponse mapToResponse(TaskEntity taskEntity, String username) {
        return new TaskResponse(
                taskEntity.getTaskId(),
                taskEntity.getTitle(),
                taskEntity.getDescription(),
                taskEntity.getStatus(),
                taskEntity.getPriority(),
                taskEntity.getTargetEndDate(),
                username,
                taskEntity.getCreatedOn(),
                username,
                taskEntity.getUpdatedOn(),
                username
        );
    }
}
