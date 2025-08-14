package com.nowakartur97.personalkanbanboardbackend.common;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BaseTaskValidator {

    default Mono<Boolean> validate(UUID taskId) {
        return Mono.just(true);
    }
}
