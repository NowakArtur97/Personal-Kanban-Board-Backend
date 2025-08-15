package com.nowakartur97.personalkanbanboardbackend.common;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
import com.nowakartur97.personalkanbanboardbackend.task.TaskDTO;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserService;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Qualifier;
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

public abstract class BaseTaskController<E extends BaseTaskEntity, R extends BaseTaskResponse> {

    private final BaseTaskService<E> service;
    protected final UserService userService;
    protected final JWTUtil jwtUtil;
    private final BaseTaskMapper<E, R> mapper;
    private final BaseTaskValidator validator;

    public BaseTaskController(BaseTaskService<E> service, UserService userService, JWTUtil
            jwtUtil, BaseTaskMapper<E, R> mapper, @Qualifier("BaseTaskValidator") BaseTaskValidator validator) {
        this.service = service;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.mapper = mapper;
        this.validator = validator;
    }

    protected Flux<R> mapToTasksResponse(Mono<List<E>> tasksList) {
        return tasksList
                .map(tasks -> Stream.of(
                                getUuidsFromTasksByProperty(tasks, E::getCreatedBy),
                                getUuidsFromTasksByProperty(tasks, E::getUpdatedBy),
                                getUuidsFromTasksByProperty(tasks, E::getAssignedTo))
                        .flatMap(Collection::stream)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()))
                .flatMap(userIds -> userService.findAllByIds(userIds.stream().toList()).collectList())
                .zipWith(tasksList)
                .flatMapIterable(tuple -> tuple.getT2().stream()
                        .map(task -> mapper.mapToResponse(task, tuple.getT1()))
                        .toList());
    }

    protected Mono<R> create(UUID taskId, TaskDTO taskDTO, DataFetchingEnvironment env) {
        String username = jwtUtil.extractUsername(env.getGraphQlContext().get(TOKEN_IN_CONTEXT));
        Mono<UserEntity> createdBy = userService.findByUsername(username);
        if (taskDTO.getAssignedTo() == null) {
            return validator.validate(taskId)
                    .flatMap(__ -> createdBy)
                    .flatMap(user -> Mono.just(mapper.mapToEntity(taskId, taskDTO, user.getUserId()))
                            .flatMap(service::save)
                            .map(subtask -> mapper.mapToResponse(subtask, user.getUsername())));
        }
        Mono<UserEntity> assignedTo = userService.findById(taskDTO.getAssignedTo());
        return validator.validate(taskId)
                .flatMap(__ -> Mono.zip(createdBy, assignedTo))
                .flatMap(tuple -> Mono.just(mapper.mapToEntity(taskId, taskDTO, tuple.getT1().getUserId(), tuple.getT2().getUserId()))
                        .flatMap(service::save)
                        .map(subtask -> mapper.mapToResponse(subtask, tuple.getT1().getUsername(), tuple.getT2().getUsername())));
    }

    protected Mono<R> update(UUID taskId, TaskDTO taskDTO, DataFetchingEnvironment env) {
        String username = jwtUtil.extractUsername(env.getGraphQlContext().get(TOKEN_IN_CONTEXT));
        Mono<E> taskById = service.findById(taskId);
        Mono<UserEntity> createdBy = taskById.map(E::getCreatedBy)
                .flatMap(userService::findById);
        Mono<UserEntity> updatedBy = userService.findByUsername(username);
        if (taskDTO.getAssignedTo() == null) {
            return Mono.zip(taskById, createdBy, updatedBy)
                    .flatMap(tuple -> Mono.just(mapper.updateEntity(tuple.getT1(), taskDTO, tuple.getT3().getUserId()))
                            .flatMap(service::update)
                            .map(task -> mapper.mapToResponse(task, username, tuple.getT2().getUsername(), tuple.getT3().getUsername())));
        }
        Mono<UserEntity> assignedTo = userService.findById(taskDTO.getAssignedTo());
        return Mono.zip(taskById, createdBy, updatedBy, assignedTo)
                .flatMap(tuple -> Mono.just(mapper.updateEntity(tuple.getT1(), taskDTO, tuple.getT3().getUserId(), tuple.getT4().getUserId()))
                        .flatMap(service::update)
                        .map(task -> mapper.mapToResponse(task, tuple.getT2().getUsername(), tuple.getT3().getUsername(), tuple.getT4().getUsername())));

    }

    protected List<UUID> getUuidsFromTasksByProperty(List<E> tasks, Function<E, UUID> byProperty) {
        return tasks.stream()
                .map(byProperty)
                .toList();
    }
}
