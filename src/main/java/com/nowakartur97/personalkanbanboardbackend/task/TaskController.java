package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
import com.nowakartur97.personalkanbanboardbackend.exception.ResourceNotFoundException;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
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

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        return taskService.findAll()
                .flatMap(task -> taskService.findAll().collectList()
                        .map(t -> Stream.of(
                                        getUuidsFromTasksByProperty(t, TaskEntity::getCreatedBy),
                                        getUuidsFromTasksByProperty(t, TaskEntity::getUpdatedBy),
                                        getUuidsFromTasksByProperty(t, TaskEntity::getAssignedTo))
                                .flatMap(Collection::stream)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet()))
                        .flatMap(userIds -> userService.findAllByIds(userIds.stream().toList()).collectList())
                        .map(users -> mapToResponse(task, users)));
    }

    @MutationMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public Mono<TaskResponse> createTask(@Argument @Valid TaskDTO taskDTO, DataFetchingEnvironment env) {
        String username = jwtUtil.extractUsername(env.getGraphQlContext().get(TOKEN_IN_CONTEXT));
        Mono<UserEntity> author = userService.findByUsername(username);
        if (taskDTO.getAssignedTo() == null) {
            return author
                    .flatMap(user -> Mono.just(mapToEntity(taskDTO, user.getUserId()))
                            .flatMap(taskService::saveTask)
                            .map(task -> mapToResponse(task, user.getUsername())));
        }
        Mono<UserEntity> assignedTo = userService.findById(taskDTO.getAssignedTo());
        return Mono.zip(author, assignedTo)
                .flatMap(tuple -> Mono.just(mapToEntity(taskDTO, tuple.getT1().getUserId(), tuple.getT2().getUserId()))
                        .flatMap(taskService::saveTask)
                        .map(task -> mapToResponse(task, tuple.getT1().getUsername(), tuple.getT2().getUsername())));
    }

    @MutationMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public Mono<TaskResponse> updateTask(@Argument UUID taskId, @Argument @Valid TaskDTO taskDTO, DataFetchingEnvironment env) {
        String username = jwtUtil.extractUsername(env.getGraphQlContext().get(TOKEN_IN_CONTEXT));
        Mono<TaskEntity> taskById = taskService.findById(taskId);
        Mono<UserEntity> author = userService.findByUsername(username);
        if (taskDTO.getAssignedTo() == null) {
            return Mono.zip(taskById, author)
                    .flatMap(tuple -> Mono.just(updateEntity(tuple.getT1(), taskDTO, tuple.getT2().getUserId()))
                            .flatMap(taskService::updateTask)
                            .map(task -> mapToResponse(task, username, username, tuple.getT2().getUsername())));
        }
        Mono<UserEntity> assignedTo = userService.findById(taskDTO.getAssignedTo());
        return Mono.zip(taskById, author, assignedTo)
                .flatMap(tuple -> Mono.just(updateEntity(tuple.getT1(), taskDTO, tuple.getT2().getUserId(), tuple.getT3().getUserId()))
                        .flatMap(taskService::updateTask)
                        .map(task -> mapToResponse(task, tuple.getT2().getUsername(), username, tuple.getT3().getUsername())));
    }

    private TaskEntity mapToEntity(TaskDTO taskDTO, UUID createdBy) {
        return mapToEntity(taskDTO, createdBy, createdBy);
    }

    private TaskEntity mapToEntity(TaskDTO taskDTO, UUID createdBy, UUID assignedTo) {
        return TaskEntity.builder()
                .title(taskDTO.getTitle())
                .description(taskDTO.getDescription())
                .status(taskDTO.getStatus() != null ? taskDTO.getStatus() : TaskStatus.READY_TO_START)
                .priority(taskDTO.getPriority() != null ? taskDTO.getPriority() : TaskPriority.LOW)
                .targetEndDate(taskDTO.getTargetEndDate())
                .assignedTo(assignedTo)
                .createdBy(createdBy)
                // TODO: Check in Postgres to see if the date is auto-populated
                .createdOn(Instant.now())
                .build();
    }

    private TaskEntity updateEntity(TaskEntity taskEntity, TaskDTO taskDTO, UUID updatedBy) {
        return updateEntity(taskEntity, taskDTO, updatedBy, updatedBy);
    }

    private TaskEntity updateEntity(TaskEntity taskEntity, TaskDTO taskDTO, UUID updatedBy, UUID assignedTo) {
        taskEntity.setTitle(taskDTO.getTitle());
        taskEntity.setDescription(taskDTO.getDescription());
        taskEntity.setStatus(taskDTO.getStatus() != null ? taskDTO.getStatus() : TaskStatus.READY_TO_START);
        taskEntity.setPriority(taskDTO.getPriority() != null ? taskDTO.getPriority() : TaskPriority.LOW);
        taskEntity.setTargetEndDate(taskDTO.getTargetEndDate());
        taskEntity.setAssignedTo(assignedTo);
        taskEntity.setUpdatedBy(updatedBy);
        // TODO: Check in Postgres to see if the date is auto-populated
        taskEntity.setUpdatedOn(Instant.now());
        return taskEntity;
    }

    private TaskResponse mapToResponse(TaskEntity taskEntity, String createdBy) {
        return mapToResponse(taskEntity, createdBy, createdBy);
    }

    private TaskResponse mapToResponse(TaskEntity taskEntity, String createdBy, String assignedTo) {
        return mapToResponse(taskEntity, createdBy, null, assignedTo);
    }

    private TaskResponse mapToResponse(TaskEntity taskEntity, List<UserEntity> users) {
        String createdBy = getUsernameByUserId(taskEntity.getCreatedBy(), users);
        String updatedBy = getUsernameByUserId(taskEntity.getUpdatedBy(), users);
        String assignedTo = getUsernameByUserId(taskEntity.getAssignedTo(), users);
        return mapToResponse(taskEntity, createdBy, updatedBy, assignedTo);
    }

    private TaskResponse mapToResponse(TaskEntity taskEntity, String createdBy, String updatedBy, String assignedTo) {
        return new TaskResponse(
                taskEntity.getTaskId(),
                taskEntity.getTitle(),
                taskEntity.getDescription(),
                taskEntity.getStatus(),
                taskEntity.getPriority(),
                taskEntity.getTargetEndDate(),
                createdBy,
                taskEntity.getCreatedOn().toString(),
                updatedBy,
                taskEntity.getUpdatedOn() != null ? taskEntity.getUpdatedOn().toString() : null,
                assignedTo
        );
    }

    private List<UUID> getUuidsFromTasksByProperty(TaskEntity task, Function<TaskEntity, UUID> byProperty) {
        return List.of(byProperty.apply(task));
    }

    private List<UUID> getUuidsFromTasksByProperty(List<TaskEntity> tasks, Function<TaskEntity, UUID> byProperty) {
        return tasks.stream()
                .map(byProperty)
                .toList();
    }

    private String getUsernameByUserId(UUID userId, List<UserEntity> users) {
        if (userId == null) {
            return null;
        }
        return users.stream().filter(user -> user.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId.toString()))
                .getUsername();
    }
}
