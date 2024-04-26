package com.nowakartur97.personalkanbanboardbackend.user;

import com.nowakartur97.personalkanbanboardbackend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public Mono<UserEntity> findByUsername(String username) {

        log.info("Looking up user by username: '{}'", username);

        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User", "username", username)));
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
