package com.nowakartur97.personalkanbanboardbackend.common;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
import com.nowakartur97.personalkanbanboardbackend.user.UserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseTaskController<R extends BaseTaskResponse, E extends BaseTaskEntity> {

    private final BaseTaskService<E> service;
    protected final UserService userService;
    protected final JWTUtil jwtUtil;
    private final BaseTaskMapper<R, E> mapper;
    private final BaseTaskValidator validator;

    public BaseTaskController(BaseTaskService<E> service, UserService userService, JWTUtil
            jwtUtil, BaseTaskMapper<R, E> mapper, BaseTaskValidator validator) {
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

    protected List<UUID> getUuidsFromTasksByProperty(List<E> tasks, Function<E, UUID> byProperty) {
        return tasks.stream()
                .map(byProperty)
                .toList();
    }
}
