package com.nowakartur97.personalkanbanboardbackend.integration;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTConfigurationProperties;
import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
import com.nowakartur97.personalkanbanboardbackend.common.DoubleRequestVariable;
import com.nowakartur97.personalkanbanboardbackend.common.RequestVariable;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRepository;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
// TODO: test with Postgres test container
//@ActiveProfiles("test-container")
public abstract class IntegrationTest {

    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected HttpGraphQlTester httpGraphQlTester;
    @Autowired
    protected JWTUtil jwtUtil;
    @Autowired
    protected JWTConfigurationProperties jwtConfigurationProperties;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    // TODO: Setup testcontainers
//    @BeforeAll
//    public static void startContainer() {
//        postgresContainer.start();
//    }

    @AfterEach
    public void cleanUpUserTable() {
        userRepository.deleteAll().block();
    }

    // TODO: Setup testcontainers
//    @AfterAll
//    public static void stopContainer() {
//        postgresContainer.stop();
//    }

    protected UserEntity createUser() {
        return userRepository.save(createUser("testUser", "testUser@domain.com")).block();
    }

    protected UserEntity createUser(UserRole role) {
        return userRepository.save(createUser("testUser", "testUser@domain.com", role)).block();
    }

    protected UserEntity createUser(String username, String email) {
        return createUser(username, email, UserRole.USER);
    }

    protected UserEntity createUser(String username, String email, UserRole role) {
        UserEntity user = UserEntity.builder()
                .username(username)
                .password(bCryptPasswordEncoder.encode("pass1"))
                .email(email)
                .role(role)
                .build();
        return userRepository.save(user).block();
    }

    protected void addAuthorizationHeader(HttpHeaders headers, UserEntity userEntity) {
        String token = jwtUtil.generateToken(userEntity.getUsername(), userEntity.getRole().name());
        String authHeader = jwtConfigurationProperties.getAuthorizationType() + " " + token;
        headers.add(jwtConfigurationProperties.getAuthorizationHeader(), authHeader);
    }

    protected void runTestForSendingRequestWithInvalidCredentials(String document, String path, String token, RequestVariable requestVariable) {

        assertUnauthorizedErrorResponse(sendRequestWithErrors(token, document, requestVariable), path, "Invalid login credentials.");
    }

    protected void runTestForSendingRequestWithoutProvidingAuthorizationHeader(String document, String path, RequestVariable requestVariable) {

        assertUnauthorizedErrorResponse(sendRequestWithErrors(document, requestVariable), path, "Unauthorized");
    }

    protected void runTestForSendingRequestWithExpiredToken(String document, String path, RequestVariable requestVariable) {

        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjpbIlVTRVIiXSwic3ViIjoidGVzdFVzZXIiLCJpYXQiOjE3MTEyNzg1ODAsImV4cCI6MTcxMTI3ODU4MH0.nouAgIkDaanTk0LX37HSRjM4SDZxqBqz1gDufnU2fzQ";
        assertUnauthorizedErrorResponse(sendRequestWithErrors(expiredToken, document, requestVariable), path, "JWT expired");
    }

    protected void runTestForSendingRequestWithInvalidToken(String document, String path, RequestVariable requestVariable) {

        String invalidToken = "invalid";
        assertUnauthorizedErrorResponse(sendRequestWithErrors(invalidToken, document, requestVariable), path,
                "Invalid compact JWT string: Compact JWSs must contain exactly 2 period characters, and compact JWEs must contain exactly 4.  Found: 0");
    }

    protected void runTestForSendingRequestWithDifferentTokenSignature(String document, String path, RequestVariable requestVariable) {

        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjpbIlVTRVIiXSwic3ViIjoidXNlciIsImlhdCI6MTcxMTIwOTEzMiwiZXhwIjoxNzExMjE5OTMyfQ.n-h8vIdov2voZhwNdqbmgiO44XjeCdAMzf7ddqufoXc";

        assertUnauthorizedErrorResponse(sendRequestWithErrors(invalidToken, document, requestVariable), path,
                "JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.");
    }

    protected void assertResponseErrors(GraphQlTester.Errors errors, String path, String message) {
        errors.satisfy(
                responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertResponseError(responseError, message, path);
                });
    }

    protected void assertResponseErrors(GraphQlTester.Errors errors, String path, String message, String message2) {
        errors.satisfy(
                responseErrors -> {
                    assertThat(responseErrors.size()).isEqualTo(2);
                    ResponseError firstResponseError = responseErrors.getFirst();
                    assertResponseError(firstResponseError, message, path);
                    ResponseError secondResponseError = responseErrors.getLast();
                    assertResponseError(secondResponseError, message2, path);
                });
    }

    private void assertResponseError(ResponseError responseError, String message, String path) {
        assertResponseError(responseError, message, path, new SourceLocation(2, 3));
    }

    private void assertResponseError(ResponseError responseError, String message, String path, SourceLocation sourceLocation) {
        assertThat(responseError.getMessage()).contains(message);
        assertThat(responseError.getPath()).isEqualTo(path);
        assertThat(responseError.getLocations()).isEqualTo(List.of(sourceLocation));
    }

    protected void assertValidationErrorResponse(GraphQlTester.Errors errors, SourceLocation sourceLocation, String message) {
        errors.satisfy(
                responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertThat(responseError.getErrorType()).isEqualTo(graphql.ErrorType.ValidationError);
                    assertResponseError(responseError, message, "", sourceLocation);
                });
    }

    protected void assertUnauthorizedErrorResponse(GraphQlTester.Errors errors, String path, String message) {
        errors.satisfy(responseErrors -> assertErrorResponse(path, message, responseErrors, ErrorType.UNAUTHORIZED));
    }

    protected void asserForbiddenErrorResponse(GraphQlTester.Errors errors, String path) {
        errors.satisfy(responseErrors -> assertErrorResponse(path, "Forbidden", responseErrors, ErrorType.FORBIDDEN));
    }

    protected void assertNotFoundErrorResponse(GraphQlTester.Errors errors, String path, String message) {
        errors.satisfy(responseErrors -> assertErrorResponse(path, message, responseErrors, ErrorType.NOT_FOUND));
    }

    private void assertErrorResponse(String path, String message, List<ResponseError> responseErrors, ErrorType errorType) {
        assertThat(responseErrors.size()).isOne();
        ResponseError responseError = responseErrors.getFirst();
        assertThat(responseError.getErrorType()).isEqualTo(errorType);
        assertResponseError(responseError, message, path);
    }

    protected Object sendRequest(UserEntity userEntity, String document, String path, RequestVariable requestVariable,
                                 Class response, boolean isEntityList) {
        HttpGraphQlTester.Builder<?> builder = httpGraphQlTester.mutate();
        if (userEntity != null) {
            builder = builder.headers(headers -> addAuthorizationHeader(headers, userEntity));
        }
        GraphQlTester.Traversable verify = prepareRequest(document, requestVariable, builder)
                .verify();
        GraphQlTester.Path requestPath = verify
                .path(path);
        if (response == null) {
            return null;
        }
        if (isEntityList) {
            return requestPath
                    .entityList(response)
                    .get();
        } else {
            return requestPath.
                    entity(response)
                    .get();
        }
    }

    protected GraphQlTester.Errors sendRequestWithErrors(String document, RequestVariable requestVariable) {
        return prepareRequest(document, requestVariable, httpGraphQlTester.mutate());
    }

    protected GraphQlTester.Errors sendRequestWithErrors(String token, String document, RequestVariable requestVariable) {
        HttpGraphQlTester.Builder<?> builder = httpGraphQlTester.mutate();
        if (StringUtils.isNotBlank(token)) {
            String authHeader = jwtConfigurationProperties.getAuthorizationType() + " " + token;
            builder.headers(headers -> headers.add(jwtConfigurationProperties.getAuthorizationHeader(), authHeader));
        }
        return prepareRequest(document, requestVariable, builder);
    }

    protected GraphQlTester.Errors sendRequestWithErrors(UserEntity userEntity, String document, RequestVariable requestVariable) {
        HttpGraphQlTester.Builder<?> builder = httpGraphQlTester.mutate().headers(headers -> addAuthorizationHeader(headers, userEntity));
        return prepareRequest(document, requestVariable, builder);
    }

    private GraphQlTester.Errors prepareRequest(String document, RequestVariable requestVariable, HttpGraphQlTester.Builder<?> builder) {
        GraphQlTester.Request<?> requestVar = builder.build().document(document);
        if (requestVariable instanceof DoubleRequestVariable) {
            DoubleRequestVariable doubleRequestVariable = (DoubleRequestVariable) requestVariable;
            requestVar = requestVar
                    .variable(requestVariable.getName(), requestVariable.getValue())
                    .variable(doubleRequestVariable.getName2(), doubleRequestVariable.getValue2());
        } else if (requestVariable != null) {
            requestVar = requestVar.variable(requestVariable.getName(), requestVariable.getValue());
        }
        return requestVar.execute().errors();
    }
}
