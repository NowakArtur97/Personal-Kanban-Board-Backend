package com.nowakartur97.personalkanbanboardbackend.common;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class BaseTaskValidator {

    public Mono<Boolean> validate(Object parameter) {
        return Mono.just(true);
    }
}
