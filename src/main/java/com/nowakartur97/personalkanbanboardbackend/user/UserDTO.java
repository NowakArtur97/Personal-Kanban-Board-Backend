package com.nowakartur97.personalkanbanboardbackend.user;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class UserDTO {

    private String username;
    private String password;
    @Email(message = "{registerUser.userDTO.email.correctFormat}")
    private String email;
}
