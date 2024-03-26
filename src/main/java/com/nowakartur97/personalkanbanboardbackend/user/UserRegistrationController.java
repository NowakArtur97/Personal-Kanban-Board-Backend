package com.nowakartur97.personalkanbanboardbackend.user;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTConfigurationProperties;
import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

@Controller
@Validated
@RequiredArgsConstructor
public class UserRegistrationController {

    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JWTUtil jwtUtil;
    private final JWTConfigurationProperties jwtConfigurationProperties;
    private final UserValidator userValidator;

    @MutationMapping
    public Mono<UserResponse> registerUser(@Argument @Valid UserDTO userDTO) {
        return userValidator.validate(userDTO)
                .map(__ -> mapToEntity(userDTO))
                .flatMap(userService::saveUser)
                .map(this::mapToUserResponse);
    }

    private UserEntity mapToEntity(UserDTO userDTO) {
        return new UserEntity(
                userDTO.getUsername(),
                bCryptPasswordEncoder.encode(userDTO.getPassword()),
                userDTO.getEmail(),
                UserRole.USER);
    }

    private UserResponse mapToUserResponse(UserEntity userEntity) {
        return new UserResponse(
                userEntity.getUsername(),
                userEntity.getEmail(),
                jwtUtil.generateToken(userEntity.getUsername(), userEntity.getRole().name()),
                jwtConfigurationProperties.getExpirationTimeInMilliseconds());
    }
}
