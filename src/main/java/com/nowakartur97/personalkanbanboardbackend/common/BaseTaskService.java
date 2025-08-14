package com.nowakartur97.personalkanbanboardbackend.common;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public abstract class BaseTaskService<E extends BaseTaskEntity> {

    private final BaseTaskRepository<E> repository;

    public abstract Mono<E> save(E task);
}
