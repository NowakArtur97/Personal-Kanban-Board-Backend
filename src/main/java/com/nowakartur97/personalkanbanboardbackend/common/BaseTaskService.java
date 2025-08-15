package com.nowakartur97.personalkanbanboardbackend.common;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public abstract class BaseTaskService<E extends BaseTaskEntity> {

    private final BaseTaskRepository<E> repository;

    public abstract Mono<E> findById(UUID id);

    public abstract Mono<E> save(E task);

    public abstract Mono<E> update(E task);
}
