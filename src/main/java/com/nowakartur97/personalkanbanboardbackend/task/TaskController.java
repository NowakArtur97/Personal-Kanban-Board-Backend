package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskEntity;
import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskResponse;
import com.nowakartur97.personalkanbanboardbackend.subtask.SubtaskEntity;
import com.nowakartur97.personalkanbanboardbackend.subtask.SubtaskService;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserService;
import graphql.com.google.common.collect.Sets;
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
import java.util.Set;
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
    private final SubtaskService subtaskService;
    private final UserService userService;
    private final JWTUtil jwtUtil;
    private final TaskMapper taskMapper;

    @QueryMapping
    public Flux<BaseTaskResponse> tasks() {
        Flux<TaskEntity> allTasks = taskService.findAll();
        return mapToTasksResponse(allTasks);
    }

    @QueryMapping
    public Flux<BaseTaskResponse> tasksAssignedTo(@Argument UUID assignedToId) {
        Flux<TaskEntity> assignedToUserTasks = taskService.findAllByAssignedTo(assignedToId);
        return mapToTasksResponse(assignedToUserTasks);
    }

    private Flux<BaseTaskResponse> mapToTasksResponse(Flux<TaskEntity> tasksList) {
        Mono<List<SubtaskEntity>> subtasks = tasksList.map(TaskEntity::getTaskId)
                .flatMap(subtaskService::findAllByTaskId)
                .collectList();
        Mono<Set<UUID>> subtasksUsersIds = subtasks.map(getUserIdsForResponse());
        return tasksList
                .collectList()
                .map(getUserIdsForResponse())
                .zipWith(subtasksUsersIds)
                .flatMap(tuple -> userService.findAllByIds(Sets.union(tuple.getT1(), tuple.getT2())
                        .stream().toList()).collectList())
                .zipWith(tasksList.collectList())
                .zipWith(subtasks)
                .flatMapIterable(tuple -> tuple.getT1().getT2().stream()
                        .map(task -> taskMapper.mapToResponse(task, tuple.getT1().getT1(), tuple.getT2()))
                        .toList());
    }

    @MutationMapping
    public Mono<BaseTaskResponse> createTask(@Argument @Valid TaskDTO taskDTO, DataFetchingEnvironment env) {
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
    public Mono<BaseTaskResponse> updateTask(@Argument UUID taskId, @Argument @Valid TaskDTO taskDTO, DataFetchingEnvironment env) {
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
    public Mono<BaseTaskResponse> updateUserAssignedToTask(@Argument UUID taskId, @Argument UUID assignedToId, DataFetchingEnvironment env) {
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

    private <T extends BaseTaskEntity> Function<List<T>, Set<UUID>> getUserIdsForResponse() {
        return tasks -> Stream.of(
                        getUuidsFromTasksByProperty(tasks, BaseTaskEntity::getCreatedBy),
                        getUuidsFromTasksByProperty(tasks, BaseTaskEntity::getUpdatedBy),
                        getUuidsFromTasksByProperty(tasks, BaseTaskEntity::getAssignedTo))
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private <T extends BaseTaskEntity> List<UUID> getUuidsFromTasksByProperty(List<T> tasks, Function<BaseTaskEntity, UUID> byProperty) {
        return tasks.stream()
                .map(byProperty)
                .toList();
    }
}
