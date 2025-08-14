package com.nowakartur97.personalkanbanboardbackend.common;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseTaskService<E extends BaseTaskEntity> {

    private final BaseTaskRepository<E> repository;
}
