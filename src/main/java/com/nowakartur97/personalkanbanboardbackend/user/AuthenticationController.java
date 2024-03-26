package com.nowakartur97.personalkanbanboardbackend.user;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTConfigurationProperties;
import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JWTUtil jwtUtil;
    private final JWTConfigurationProperties jwtConfigurationProperties;

    @QueryMapping
    public Mono<UserResponse> loginUser(@Argument AuthenticationRequest authenticationRequest) {
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

    private UserResponse mapToUserResponse(UserEntity userEntity) {
        return new UserResponse(
                userEntity.getUsername(),
                userEntity.getEmail(),
                jwtUtil.generateToken(userEntity.getUsername(), userEntity.getRole().name()),
                jwtConfigurationProperties.getExpirationTimeInMilliseconds());
    }
}
