package com.nowakartur97.personalkanbanboardbackend.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @QueryMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public Flux<UserResponse> users() {
        return userService.findAll()
                .map(userMapper::mapToResponse);
    }
}
