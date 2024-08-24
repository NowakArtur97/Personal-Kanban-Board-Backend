package com.nowakartur97.personalkanbanboardbackend.user;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTConfigurationProperties;
import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
@Slf4j
public class UserController extends UserBasicController {

    public UserController(UserService userService, BCryptPasswordEncoder bCryptPasswordEncoder,
                          JWTUtil jwtUtil, JWTConfigurationProperties jwtConfigurationProperties) {
        super(userService, bCryptPasswordEncoder, jwtUtil, jwtConfigurationProperties);
    }

    @QueryMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public Flux<UserResponse> users() {
        return userService.findAll()
                .map(this::mapToResponse);
    }
}
