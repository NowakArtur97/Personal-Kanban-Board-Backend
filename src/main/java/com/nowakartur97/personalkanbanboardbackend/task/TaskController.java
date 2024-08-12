package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
import com.nowakartur97.personalkanbanboardbackend.user.UserService;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.auth.AuthorizationHeaderInterceptor.TOKEN_IN_CONTEXT;

@Controller
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;
    private final JWTUtil jwtUtil;

    @QueryMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public Flux<TaskResponse> tasks(DataFetchingEnvironment env) {
        String username = jwtUtil.extractUsername(env.getGraphQlContext().get(TOKEN_IN_CONTEXT));
        return taskService.getAllTasksForUser(username)
                .map(task -> mapToResponse(task, username));
    }

    @MutationMapping
    public Mono<TaskResponse> createTask(@Argument @Valid TaskDTO taskDTO, DataFetchingEnvironment env) {
        String username = jwtUtil.extractUsername(env.getGraphQlContext().get(TOKEN_IN_CONTEXT));
        if (taskDTO.getAssignedTo() == null) {
            return userService.findByUsername(username)
                    .flatMap(user -> Mono.just(mapToEntity(taskDTO, user.getUserId()))
                            .flatMap(taskService::saveTask)
                            .map(task -> mapToResponse(task, user.getUsername())));
        }
        return userService.findByUsername(username)
                .zipWith(userService.findById(taskDTO.getAssignedTo()))
                .flatMap(tuple -> Mono.just(mapToEntity(taskDTO, tuple.getT1().getUserId(), tuple.getT2().getUserId()))
                        .flatMap(taskService::saveTask)
                        .map(task -> mapToResponse(task, tuple.getT1().getUsername(), tuple.getT2().getUsername())));
    }

    private TaskEntity mapToEntity(TaskDTO taskDTO, UUID createdBy) {
        return mapToEntity(taskDTO, createdBy, createdBy);
    }

    private TaskEntity mapToEntity(TaskDTO taskDTO, UUID createdBy, UUID assignedTo) {
        return TaskEntity.builder()
                .title(taskDTO.getTitle())
                .description(taskDTO.getDescription())
                .assignedTo(taskDTO.getAssignedTo())
                .status(TaskStatus.READY_TO_START)
                .priority(taskDTO.getPriority() != null ? taskDTO.getPriority() : TaskPriority.LOW)
                .targetEndDate(taskDTO.getTargetEndDate())
                .assignedTo(assignedTo)
                .createdBy(createdBy)
                // TODO: Check in Postgres to see if the date is auto-populated
                .createdOn(LocalDate.now())
                .build();
    }

    private TaskResponse mapToResponse(TaskEntity taskEntity, String createdBy) {
        return mapToResponse(taskEntity, createdBy, createdBy);
    }

    private TaskResponse mapToResponse(TaskEntity taskEntity, String createdBy, String assignedTo) {
        return new TaskResponse(
                taskEntity.getTaskId(),
                taskEntity.getTitle(),
                taskEntity.getDescription(),
                taskEntity.getStatus(),
                taskEntity.getPriority(),
                taskEntity.getTargetEndDate(),
                createdBy,
                taskEntity.getCreatedOn(),
                createdBy,
                taskEntity.getUpdatedOn(),
                assignedTo
        );
    }
}
