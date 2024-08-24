package com.nowakartur97.personalkanbanboardbackend.user;

import com.nowakartur97.personalkanbanboardbackend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public Mono<UserEntity> findById(UUID userId) {

        log.info("Looking up user by user id: '{}'", userId);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User", "userId", userId.toString())));
    }

    public Mono<UserEntity> findByUsername(String username) {

        log.info("Looking up user by username: '{}'", username);

        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User", "username", username)));
    }

    public Mono<UserEntity> findByUsernameOrEmail(String usernameOrEmail) {

        log.info("Looking up user by username or email: '{}'", usernameOrEmail);

        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User", "username/email", usernameOrEmail)));
    }

    public Flux<UserEntity> findAll() {

        log.info("Looking up all users");

        return userRepository.findAll();
    }

    public Flux<UserEntity> findAllByIds(List<UUID> ids) {

        log.info("Looking up all users by ids: '{}'", ids);

        return userRepository.findAllById(ids);
    }

    public Mono<Boolean> existsByUsernameOrEmail(String username, String email) {

        log.info("Checking if user exists by username: '{}' or email: '{}'", username, email);

        return userRepository.existsByUsernameOrEmail(username, email);
    }

    public Mono<UserEntity> saveUser(UserEntity user) {

        log.info("Registration of new user: {}", user);

        return userRepository.save(user);
    }
}
