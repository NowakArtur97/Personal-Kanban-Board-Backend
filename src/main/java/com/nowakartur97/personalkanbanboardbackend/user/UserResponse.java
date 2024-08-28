package com.nowakartur97.personalkanbanboardbackend.user;

import java.util.UUID;

public record UserResponse(UUID userId,
                           String username,
                           String email,
                           String token,
                           long expirationTimeInMilliseconds,
                           UserRole role) {
}
