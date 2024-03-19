package com.nowakartur97.personalkanbanboardbackend.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class UserDTO {

    private String username;
    private String password;
    private String email;
}
