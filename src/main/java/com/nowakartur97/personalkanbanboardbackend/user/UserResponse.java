package com.nowakartur97.personalkanbanboardbackend.user;

public record UserResponse(String username,
                           String email,
                           String token,
                           long expirationTimeInMilliseconds) {
}
