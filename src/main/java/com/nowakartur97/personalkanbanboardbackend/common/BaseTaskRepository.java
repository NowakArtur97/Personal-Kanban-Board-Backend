package com.nowakartur97.personalkanbanboardbackend.common;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import java.util.UUID;

public interface BaseTaskRepository<E extends BaseTaskEntity> extends R2dbcRepository<E, UUID> {
}
