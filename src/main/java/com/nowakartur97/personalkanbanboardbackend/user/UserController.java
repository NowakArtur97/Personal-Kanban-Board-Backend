package com.nowakartur97.personalkanbanboardbackend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('USER')")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @QueryMapping
    public Flux<UserResponse> users() {
        return userService.findAll()
                .map(userMapper::mapToResponse);
    }
}
