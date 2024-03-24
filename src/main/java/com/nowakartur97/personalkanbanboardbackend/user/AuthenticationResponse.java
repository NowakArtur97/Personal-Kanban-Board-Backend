package com.nowakartur97.personalkanbanboardbackend.user;

public record AuthenticationResponse(String token,
                                     long expirationTimeInMilliseconds
) {
}
