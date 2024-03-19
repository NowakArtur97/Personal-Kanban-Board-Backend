package com.nowakartur97.personalkanbanboardbackend.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

@Controller
@Validated
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationController {

    private final UserService userService;

    @MutationMapping
    public Mono<UserResponse> registerUser(@Argument @Valid UserDTO userDTO) {

        log.info("Registration of new user: ${}", userDTO);

        return Mono.just(new UserResponse(userDTO.getUsername(), userDTO.getPassword(), userDTO.getEmail(), "token", 72000000));
    }
}
