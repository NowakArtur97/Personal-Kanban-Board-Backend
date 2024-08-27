package com.nowakartur97.personalkanbanboardbackend.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

@Controller
@Validated
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationController {

    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserMapper userMapper;
    private final UserValidator userValidator;

    @MutationMapping
    public Mono<UserResponse> registerUser(@Argument @Valid UserDTO userDTO) {

        log.info("New registration request for user with username: {} and email: {}",
                userDTO.getUsername(), userDTO.getEmail());

        return userValidator.validate(userDTO)
                .map(__ -> userMapper.mapToEntity(userDTO))
                .flatMap(userService::save)
                .map(userMapper::mapToResponse);
    }
}
