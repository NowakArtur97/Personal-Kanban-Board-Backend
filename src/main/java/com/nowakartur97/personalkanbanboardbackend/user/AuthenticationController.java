package com.nowakartur97.personalkanbanboardbackend.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserMapper userMapper;

    @QueryMapping
    public Mono<UserResponse> loginUser(@Argument AuthenticationRequest authenticationRequest) {

        log.info("New authentication request for user with username/email: {}", authenticationRequest.getUsernameOrEmail());

        return userService.findByUsernameOrEmail(authenticationRequest.getUsernameOrEmail())
                .map(userEntity -> {
                    if (isPasswordCorrect(authenticationRequest, userEntity)) {
                        return userMapper.mapToResponse(userEntity);
                    } else {
                        throw new BadCredentialsException("Invalid login credentials.");
                    }
                });
    }

    private boolean isPasswordCorrect(AuthenticationRequest authenticationRequest, UserEntity userEntity) {
        return bCryptPasswordEncoder.matches(authenticationRequest.getPassword(), userEntity.getPassword());
    }
}
