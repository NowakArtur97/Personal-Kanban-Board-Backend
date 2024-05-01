package com.nowakartur97.personalkanbanboardbackend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/user-data-validator")
@RequiredArgsConstructor
public class UserValidatorController {

    private final UserService userService;

    @GetMapping
    public Mono<Boolean> isUsernameAndEmailAvailable(@RequestParam("username") String username,
                                                     @RequestParam("email") String email) {
        return userService.existsByUsernameOrEmail(username, email)
                .map(exists -> !exists);
    }
}
