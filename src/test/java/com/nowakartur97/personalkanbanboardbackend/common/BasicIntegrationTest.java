package com.nowakartur97.personalkanbanboardbackend.common;

import com.nowakartur97.personalkanbanboardbackend.integration.IntegrationTest;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import graphql.language.SourceLocation;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.test.tester.GraphQlTester;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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

    protected void assertResponseErrors(GraphQlTester.Errors errors, String message) {
        errors.satisfy(
                responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getLast();
                    assertErrorResponse(responseError, message, path, new SourceLocation(2, 3));
                });
    }

    protected void assertNotFoundErrorResponse(GraphQlTester.Errors errors, String message) {
        errors.satisfy(
                responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertNotFoundErrorResponse(responseError, path, message);
                });
    }

    protected void assertResponseErrors(GraphQlTester.Errors errors, String message, String message2) {
        errors.satisfy(
                responseErrors -> {
                    assertThat(responseErrors.size()).isEqualTo(2);
                    ResponseError firstResponseError = responseErrors.getFirst();
                    assertErrorResponse(firstResponseError, message, path, new SourceLocation(2, 3));
                    ResponseError secondResponseError = responseErrors.getLast();
                    assertErrorResponse(secondResponseError, message2, path, new SourceLocation(2, 3));
                });
    }
}
