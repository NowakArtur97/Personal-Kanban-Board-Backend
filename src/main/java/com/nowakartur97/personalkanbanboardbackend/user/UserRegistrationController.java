package com.nowakartur97.personalkanbanboardbackend.user;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTConfigurationProperties;
import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

@Controller
@Validated
public class UserRegistrationController extends UserController {

    private final UserValidator userValidator;

    public UserRegistrationController(UserService userService, BCryptPasswordEncoder bCryptPasswordEncoder,
                                      JWTUtil jwtUtil, JWTConfigurationProperties jwtConfigurationProperties,
                                      UserValidator userValidator) {
        super(userService, bCryptPasswordEncoder, jwtUtil, jwtConfigurationProperties);
        this.userValidator = userValidator;
    }

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
}
