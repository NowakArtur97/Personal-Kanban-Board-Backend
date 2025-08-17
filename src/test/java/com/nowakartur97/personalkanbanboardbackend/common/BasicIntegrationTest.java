package com.nowakartur97.personalkanbanboardbackend.common;

import com.nowakartur97.personalkanbanboardbackend.integration.IntegrationTest;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
public abstract class BasicIntegrationTest extends IntegrationTest {

    protected final String path;
    protected final String document;
    protected final RequestVariable requestVariable;

    @Test
    public void whenSendRequestByNotExistingUser_shouldReturnGraphQLErrorResponse() {

        String token = jwtUtil.generateToken("notExistingUser", UserRole.USER.name());

        runTestForSendingRequestWithInvalidCredentials(document, path, token, requestVariable);
    }

    @Test
    public void whenSendRequestWithoutProvidingAuthorizationHeader_shouldReturnGraphQLErrorResponse() {

        runTestForSendingRequestWithoutProvidingAuthorizationHeader(document, path, requestVariable);
    }

    @Test
    public void whenSendRequestWithExpiredToken_shouldReturnGraphQLErrorResponse() {

        runTestForSendingRequestWithExpiredToken(document, path, requestVariable);
    }

    @Test
    public void whenSendRequestWithInvalidToken_shouldReturnGraphQLErrorResponse() {

        runTestForSendingRequestWithInvalidToken(document, path, requestVariable);
    }

    @Test
    public void whenSendRequestWithDifferentTokenSignature_shouldReturnGraphQLErrorResponse() {

        runTestForSendingRequestWithDifferentTokenSignature(document, path, requestVariable);
    }
}
