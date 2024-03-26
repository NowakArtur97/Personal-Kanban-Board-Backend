package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import static com.nowakartur97.personalkanbanboardbackend.auth.AuthorizationHeaderInterceptor.TOKEN_IN_CONTEXT;

@Controller
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final JWTUtil jwtUtil;

    @QueryMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public Flux<TaskResponse> tasks(DataFetchingEnvironment env) {
        String username = jwtUtil.extractUsername(env.getGraphQlContext().get(TOKEN_IN_CONTEXT));
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
