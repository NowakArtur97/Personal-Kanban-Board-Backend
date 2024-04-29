package com.nowakartur97.personalkanbanboardbackend.user;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserRepository extends R2dbcRepository<UserEntity, UUID> {
    Mono<UserEntity> findByUsername(String username);
    Mono<UserEntity> findByUsernameOrEmail(String username, String email);
    Mono<Boolean> existsByUsernameOrEmail(String username, String email);
}
