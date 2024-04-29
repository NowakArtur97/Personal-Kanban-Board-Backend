package com.nowakartur97.personalkanbanboardbackend.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class AuthenticationRequest {

    private String usernameOrEmail;
    private String password;
}
