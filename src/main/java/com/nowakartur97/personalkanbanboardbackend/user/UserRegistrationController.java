package com.nowakartur97.personalkanbanboardbackend.user;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTConfigurationProperties;
import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

@Controller
@Validated
@Slf4j
public class UserRegistrationController extends UserBasicController {

    private final UserValidator userValidator;

    public UserRegistrationController(UserService userService, BCryptPasswordEncoder bCryptPasswordEncoder,
                                      JWTUtil jwtUtil, JWTConfigurationProperties jwtConfigurationProperties,
                                      UserValidator userValidator) {
        super(userService, bCryptPasswordEncoder, jwtUtil, jwtConfigurationProperties);
        this.userValidator = userValidator;
    }

    @MutationMapping
    public Mono<UserResponse> registerUser(@Argument @Valid UserDTO userDTO) {

        log.info("New registration request for user with username: {} and email: {}",
                userDTO.getUsername(), userDTO.getEmail());

        return userValidator.validate(userDTO)
                .map(__ -> mapToEntity(userDTO))
                .flatMap(userService::saveUser)
                .map(this::mapToResponse);
    }

    private UserEntity mapToEntity(UserDTO userDTO) {
        return UserEntity.builder()
                .username(userDTO.getUsername())
                .password(bCryptPasswordEncoder.encode(userDTO.getPassword()))
                .email(userDTO.getEmail())
                .role(UserRole.USER)
                .build();
    }
}
