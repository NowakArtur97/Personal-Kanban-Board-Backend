package com.nowakartur97.personalkanbanboardbackend.user;

public record UserResponse(String username,
                           String password,
                           String email,
                           String token,
                           int expirationTimeInMilliseconds
) {
}
