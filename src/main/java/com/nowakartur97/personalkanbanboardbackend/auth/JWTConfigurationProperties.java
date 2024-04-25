package com.nowakartur97.personalkanbanboardbackend.auth;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "app.jwt")
@Getter
public final class JWTConfigurationProperties {

    private final String secretKey;
    private final long expirationTimeInMilliseconds;
    private final String authorizationHeader;
    private final String authorizationType;
    private final int authorizationHeaderLength;

    @ConstructorBinding
    public JWTConfigurationProperties(String secretKey, long expirationTimeInMilliseconds,
                                      String authorizationHeader, String authorizationType, int authorizationHeaderLength) {
        this.secretKey = secretKey;
        this.expirationTimeInMilliseconds = expirationTimeInMilliseconds;
        this.authorizationHeader = authorizationHeader;
        this.authorizationType = authorizationType;
        this.authorizationHeaderLength = authorizationHeaderLength;
    }
}
