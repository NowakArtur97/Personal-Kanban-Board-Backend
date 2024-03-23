package com.nowakartur97.personalkanbanboardbackend.user;

import com.nowakartur97.personalkanbanboardbackend.exception.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
class UserValidator {

    private final UserService userService;

    Mono<Boolean> validate(UserDTO userDTO) {
        return userService.existsByUsernameOrEmail(userDTO.getUsername(), userDTO.getEmail())
                .map(exists -> {
                    if (exists) {
                        throw new UserAlreadyExistsException("Username/email is already taken.");
                    }
                    return true;
                });
    }
}
