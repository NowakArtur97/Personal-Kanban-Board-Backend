package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
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
@PreAuthorize("hasAuthority('USER')")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;
    private final JWTUtil jwtUtil;
    private final TaskMapper taskMapper;

    @QueryMapping
    public Flux<TaskResponse> tasks() {
        Mono<List<TaskEntity>> allTasks = taskService.findAll().collectList();
        return mapToTasksResponse(allTasks);
    }

    @QueryMapping
    public Flux<TaskResponse> tasksAssignedTo(@Argument UUID assignedToId) {
        Mono<List<TaskEntity>> assignedToUserTasks = taskService.findAllByAssignedTo(assignedToId).collectList();
        return mapToTasksResponse(assignedToUserTasks);
    }

    private Flux<TaskResponse> mapToTasksResponse(Mono<List<TaskEntity>> tasksList) {
        return tasksList
                .map(tasks -> Stream.of(
                                getUuidsFromTasksByProperty(tasks, TaskEntity::getCreatedBy),
                                getUuidsFromTasksByProperty(tasks, TaskEntity::getUpdatedBy),
                                getUuidsFromTasksByProperty(tasks, TaskEntity::getAssignedTo))
                        .flatMap(Collection::stream)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()))
                .flatMap(userIds -> userService.findAllByIds(userIds.stream().toList()).collectList())
                .zipWith(tasksList)
                .flatMapIterable(tuple -> tuple.getT2().stream()
                        .map(task -> taskMapper.mapToResponse(task, tuple.getT1()))
                        .toList());
    }

    @MutationMapping
    public Mono<TaskResponse> createTask(@Argument @Valid TaskDTO taskDTO, DataFetchingEnvironment env) {
        String username = jwtUtil.extractUsername(env.getGraphQlContext().get(TOKEN_IN_CONTEXT));
        Mono<UserEntity> createdBy = userService.findByUsername(username);
        if (taskDTO.getAssignedTo() == null) {
            return createdBy
                    .flatMap(user -> Mono.just(taskMapper.mapToEntity(taskDTO, user.getUserId()))
                            .flatMap(taskService::save)
                            .map(task -> taskMapper.mapToResponse(task, user.getUsername())));
        }
        Mono<UserEntity> assignedTo = userService.findById(taskDTO.getAssignedTo());
        return Mono.zip(createdBy, assignedTo)
                .flatMap(tuple -> Mono.just(taskMapper.mapToEntity(taskDTO, tuple.getT1().getUserId(), tuple.getT2().getUserId()))
                        .flatMap(taskService::save)
                        .map(task -> taskMapper.mapToResponse(task, tuple.getT1().getUsername(), tuple.getT2().getUsername())));
    }

    @MutationMapping
    public Mono<TaskResponse> updateTask(@Argument UUID taskId, @Argument @Valid TaskDTO taskDTO, DataFetchingEnvironment env) {
        String username = jwtUtil.extractUsername(env.getGraphQlContext().get(TOKEN_IN_CONTEXT));
        Mono<TaskEntity> taskById = taskService.findById(taskId);
        Mono<UserEntity> createdBy = taskById.map(TaskEntity::getCreatedBy)
                .flatMap(userService::findById);
        Mono<UserEntity> updatedBy = userService.findByUsername(username);
        if (taskDTO.getAssignedTo() == null) {
            return Mono.zip(taskById, createdBy, updatedBy)
                    .flatMap(tuple -> Mono.just(taskMapper.updateEntity(tuple.getT1(), taskDTO, tuple.getT3().getUserId()))
                            .flatMap(taskService::update)
                            .map(task -> taskMapper.mapToResponse(task, username, tuple.getT2().getUsername(), tuple.getT3().getUsername())));
        }
        Mono<UserEntity> assignedTo = userService.findById(taskDTO.getAssignedTo());
        return Mono.zip(taskById, createdBy, updatedBy, assignedTo)
                .flatMap(tuple -> Mono.just(taskMapper.updateEntity(tuple.getT1(), taskDTO, tuple.getT3().getUserId(), tuple.getT4().getUserId()))
                        .flatMap(taskService::update)
                        .map(task -> taskMapper.mapToResponse(task, tuple.getT2().getUsername(), tuple.getT3().getUsername(), tuple.getT4().getUsername())));
    }

    @MutationMapping
    public Mono<TaskResponse> updateUserAssignedToTask(@Argument UUID taskId, @Argument UUID assignedToId, DataFetchingEnvironment env) {
        String username = jwtUtil.extractUsername(env.getGraphQlContext().get(TOKEN_IN_CONTEXT));
        Mono<TaskEntity> taskById = taskService.findById(taskId);
        Mono<UserEntity> createdBy = taskById.map(TaskEntity::getCreatedBy)
                .flatMap(userService::findById);
        Mono<UserEntity> updatedBy = userService.findByUsername(username);
        Mono<UserEntity> assignedTo = userService.findById(assignedToId);
        return Mono.zip(taskById, createdBy, updatedBy, assignedTo)
                .flatMap(tuple -> Mono.just(taskMapper.updateUserAssignedToEntity(tuple.getT1(), tuple.getT3().getUserId(), tuple.getT4().getUserId()))
                        .flatMap(taskService::updateAssignedTo)
                        .map(task -> taskMapper.mapToResponse(task, tuple.getT2().getUsername(), tuple.getT3().getUsername(), tuple.getT4().getUsername())));
    }

    @MutationMapping
    public Mono<Void> deleteTask(@Argument UUID taskId) {
        return taskService.deleteById(taskId);
    }

    @MutationMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Mono<Void> deleteAllTasks() {
        return taskService.deleteAll();
    }

    private List<UUID> getUuidsFromTasksByProperty(List<TaskEntity> tasks, Function<TaskEntity, UUID> byProperty) {
        return tasks.stream()
                .map(byProperty)
                .toList();
    }
}
