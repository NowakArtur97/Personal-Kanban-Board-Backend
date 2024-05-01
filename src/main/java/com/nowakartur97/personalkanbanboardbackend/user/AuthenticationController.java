package com.nowakartur97.personalkanbanboardbackend.user;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTConfigurationProperties;
import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
public class AuthenticationController extends UserController {

    public AuthenticationController(UserService userService, BCryptPasswordEncoder bCryptPasswordEncoder,
                                    JWTUtil jwtUtil, JWTConfigurationProperties jwtConfigurationProperties) {
        super(userService, bCryptPasswordEncoder, jwtUtil, jwtConfigurationProperties);
    }

    @QueryMapping
    public Mono<UserResponse> loginUser(@Argument AuthenticationRequest authenticationRequest) {

        log.info("New authentication request for user with username/email: {}", authenticationRequest.getUsernameOrEmail());

        return userService.findByUsernameOrEmail(authenticationRequest.getUsernameOrEmail())
                .map(userEntity -> {
                    if (isPasswordIncorrect(authenticationRequest, userEntity)) {
                        throw new BadCredentialsException("Invalid login credentials.");
                    }
                    return mapToUserResponse(userEntity);
                });
    }

    private boolean isPasswordIncorrect(AuthenticationRequest authenticationRequest, UserEntity userEntity) {
        return !bCryptPasswordEncoder.matches(authenticationRequest.getPassword(), userEntity.getPassword());
    }
}
