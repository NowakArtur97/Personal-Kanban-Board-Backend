package com.nowakartur97.personalkanbanboardbackend.integration;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTConfigurationProperties;
import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
import com.nowakartur97.personalkanbanboardbackend.task.TaskEntity;
import com.nowakartur97.personalkanbanboardbackend.task.TaskPriority;
import com.nowakartur97.personalkanbanboardbackend.task.TaskRepository;
import com.nowakartur97.personalkanbanboardbackend.task.TaskResponse;
import com.nowakartur97.personalkanbanboardbackend.task.TaskStatus;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRepository;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
// TODO: test with Postgres test container
//@ActiveProfiles("test-container")
public class IntegrationTest {

    @Autowired
    protected TaskRepository taskRepository;
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
    public void cleanUpTables() {
        taskRepository.deleteAll().block();
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

    protected TaskEntity createTask(UUID authorId) {
        return createTask(authorId, authorId, null);
    }

    protected TaskEntity createTask(UUID authorId, UUID assignedToId, UUID updatedById) {
        return taskRepository.save(TaskEntity.builder()
                        .title("testTask")
                        .description("test")
                        .assignedTo(authorId)
                        .status(TaskStatus.READY_TO_START)
                        .priority(TaskPriority.LOW)
                        .targetEndDate(LocalDate.now().plusDays(new Random().nextInt(3)))
                        .createdOn(Instant.now())
                        .createdBy(assignedToId)
                        .updatedOn(updatedById != null ? Instant.now() : null)
                        .updatedBy(updatedById)
                        .build())
                .block();
    }

    protected void addAuthorizationHeader(HttpHeaders headers, String token) {
        String authHeader = jwtConfigurationProperties.getAuthorizationType() + " " + token;
        headers.add(jwtConfigurationProperties.getAuthorizationHeader(), authHeader);
    }

    protected void addAuthorizationHeader(HttpHeaders headers, UserEntity userEntity) {
        String token = jwtUtil.generateToken(userEntity.getUsername(), userEntity.getRole().name());
        String authHeader = jwtConfigurationProperties.getAuthorizationType() + " " + token;
        headers.add(jwtConfigurationProperties.getAuthorizationHeader(), authHeader);
    }

    protected void assertTaskResponse(TaskResponse taskResponse, TaskEntity taskEntity,
                                      String assignedTo, String createdBy, String updatedBy) {
        assertThat(taskResponse).isNotNull();
        assertThat(taskResponse.taskId()).isEqualTo(taskEntity.getTaskId());
        assertThat(taskResponse.title()).isEqualTo(taskEntity.getTitle());
        assertThat(taskResponse.status()).isEqualTo(taskEntity.getStatus());
        assertThat(taskResponse.priority()).isEqualTo(taskEntity.getPriority());
        assertThat(taskResponse.targetEndDate()).isEqualTo(taskEntity.getTargetEndDate());
        assertThat(taskResponse.assignedTo()).isEqualTo(assignedTo);
        assertThat(Instant.parse(taskResponse.createdOn()).toEpochMilli()).isEqualTo(taskEntity.getCreatedOn().toEpochMilli());
        assertThat(taskResponse.createdBy()).isEqualTo(createdBy);
        if (updatedBy != null) {
            assertThat(Instant.parse(taskResponse.updatedOn()).toEpochMilli()).isEqualTo(taskEntity.getUpdatedOn().toEpochMilli());
        } else {
            assertThat(taskResponse.updatedOn()).isNull();
        }
        assertThat(taskResponse.updatedBy()).isEqualTo(updatedBy);
    }

    protected void assertErrorResponse(ResponseError responseError, String message, String path, SourceLocation sourceLocation) {
        assertThat(responseError.getMessage()).contains(message);
        assertThat(responseError.getPath()).isEqualTo(path);
        assertThat(responseError.getLocations()).isEqualTo(List.of(sourceLocation));
    }

    protected void assertValidationErrorResponse(ResponseError responseError, SourceLocation sourceLocation, String message) {
        assertThat(responseError.getErrorType()).isEqualTo(graphql.ErrorType.ValidationError);
        assertErrorResponse(responseError, message, "", sourceLocation);
    }

    protected void assertUnauthorizedErrorResponse(ResponseError responseError, String path, String message) {
        assertThat(responseError.getErrorType()).isEqualTo(ErrorType.UNAUTHORIZED);
        assertErrorResponse(responseError, message, path, new SourceLocation(2, 3));
    }

    protected void asserForbiddenErrorResponse(ResponseError responseError, String path, String message) {
        assertThat(responseError.getErrorType()).isEqualTo(ErrorType.FORBIDDEN);
        assertErrorResponse(responseError, message, path, new SourceLocation(2, 3));
    }

    protected void assertNotFoundErrorResponse(ResponseError responseError, String path, String message) {
        assertThat(responseError.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        assertErrorResponse(responseError, message, path, new SourceLocation(2, 3));
    }

    protected void runTestForSendingRequestWithInvalidCredentials(String document, String path,
                                                                  String variableName, Object object) {

        httpGraphQlTester
                .document(document)
                .variable(variableName, object)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertUnauthorizedErrorResponse(responseError, path, "Invalid login credentials.");
                });
    }

    protected void runTestForSendingRequestWithInvalidCredentials(String document, String path, String token) {

        httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, token))
                .build()
                .document(document)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertUnauthorizedErrorResponse(responseError, path, "Invalid login credentials.");
                });
    }

    protected void runTestForSendingRequestWithInvalidCredentials(String document, String path, String token,
                                                                  String variableName, Object object) {

        httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, token))
                .build()
                .document(document)
                .variable(variableName, object)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertUnauthorizedErrorResponse(responseError, path, "Invalid login credentials.");
                });
    }

    protected void runTestForSendingRequestWithoutProvidingAuthorizationHeader(String document, String path) {

        httpGraphQlTester
                .mutate()
                .build()
                .document(document)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertUnauthorizedErrorResponse(responseError, path, "Unauthorized");
                });
    }

    protected void runTestForSendingRequestWithoutProvidingAuthorizationHeader(String document, String path,
                                                                               String variableName, Object object) {
        httpGraphQlTester
                .mutate()
                .build()
                .document(document)
                .variable(variableName, object)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertUnauthorizedErrorResponse(responseError, path, "Unauthorized");
                });
    }

    protected void runTestForSendingRequestWithoutProvidingAuthorizationHeader(String document, String path,
                                                                               String variableName, Object object,
                                                                               String variableName2, Object object2) {
        httpGraphQlTester
                .mutate()
                .build()
                .document(document)
                .variable(variableName, object)
                .variable(variableName2, object2)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertUnauthorizedErrorResponse(responseError, path, "Unauthorized");
                });
    }

    protected void runTestForSendingRequestWithExpiredToken(String document, String path) {

        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjpbIlVTRVIiXSwic3ViIjoidGVzdFVzZXIiLCJpYXQiOjE3MTEyNzg1ODAsImV4cCI6MTcxMTI3ODU4MH0.nouAgIkDaanTk0LX37HSRjM4SDZxqBqz1gDufnU2fzQ";

        sendRequestWithJWTErrors(expiredToken, document, path, "JWT expired");
    }

    protected void runTestForSendingRequestWithExpiredToken(String document, String path, String variableName, Object object) {

        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjpbIlVTRVIiXSwic3ViIjoidGVzdFVzZXIiLCJpYXQiOjE3MTEyNzg1ODAsImV4cCI6MTcxMTI3ODU4MH0.nouAgIkDaanTk0LX37HSRjM4SDZxqBqz1gDufnU2fzQ";

        sendRequestWithJWTErrors(expiredToken, document, path, variableName, object, "JWT expired");
    }

    protected void runTestForSendingRequestWithExpiredToken(String document, String path, String variableName, Object object, String variableName2, Object object2) {

        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjpbIlVTRVIiXSwic3ViIjoidGVzdFVzZXIiLCJpYXQiOjE3MTEyNzg1ODAsImV4cCI6MTcxMTI3ODU4MH0.nouAgIkDaanTk0LX37HSRjM4SDZxqBqz1gDufnU2fzQ";

        sendRequestWithJWTErrors(expiredToken, document, path, variableName, object, variableName2, object2, "JWT expired");
    }

    protected void runTestForSendingRequestWithInvalidToken(String document, String path) {

        String invalidToken = "invalid";

        sendRequestWithJWTErrors(invalidToken, document, path,
                "Invalid compact JWT string: Compact JWSs must contain exactly 2 period characters, and compact JWEs must contain exactly 4.  Found: 0");
    }

    protected void runTestForSendingRequestWithInvalidToken(String document, String path, String variableName, Object object) {

        String invalidToken = "invalid";

        sendRequestWithJWTErrors(invalidToken, document, path, variableName, object,
                "Invalid compact JWT string: Compact JWSs must contain exactly 2 period characters, and compact JWEs must contain exactly 4.  Found: 0");
    }

    protected void runTestForSendingRequestWithInvalidToken(String document, String path, String variableName, Object object, String variableName2, Object object2) {

        String invalidToken = "invalid";

        sendRequestWithJWTErrors(invalidToken, document, path, variableName, object, variableName2, object2,
                "Invalid compact JWT string: Compact JWSs must contain exactly 2 period characters, and compact JWEs must contain exactly 4.  Found: 0");
    }

    protected void runTestForSendingRequestWithDifferentTokenSignature(String document, String path) {

        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjpbIlVTRVIiXSwic3ViIjoidXNlciIsImlhdCI6MTcxMTIwOTEzMiwiZXhwIjoxNzExMjE5OTMyfQ.n-h8vIdov2voZhwNdqbmgiO44XjeCdAMzf7ddqufoXc";

        sendRequestWithJWTErrors(invalidToken, document, path,
                "JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.");
    }

    protected void runTestForSendingRequestWithDifferentTokenSignature(String document, String path, String variableName, Object object) {

        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjpbIlVTRVIiXSwic3ViIjoidXNlciIsImlhdCI6MTcxMTIwOTEzMiwiZXhwIjoxNzExMjE5OTMyfQ.n-h8vIdov2voZhwNdqbmgiO44XjeCdAMzf7ddqufoXc";

        sendRequestWithJWTErrors(invalidToken, document, path, variableName, object,
                "JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.");
    }

    protected void runTestForSendingRequestWithDifferentTokenSignature(String document, String path, String variableName, Object object, String variableName2, Object object2) {

        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjpbIlVTRVIiXSwic3ViIjoidXNlciIsImlhdCI6MTcxMTIwOTEzMiwiZXhwIjoxNzExMjE5OTMyfQ.n-h8vIdov2voZhwNdqbmgiO44XjeCdAMzf7ddqufoXc";

        sendRequestWithJWTErrors(invalidToken, document, path, variableName, object, variableName2, object2,
                "JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.");
    }

    private void sendRequestWithJWTErrors(String token, String document, String path, String message) {
        httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, token))
                .build()
                .document(document)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertUnauthorizedErrorResponse(responseError, path, message);
                });
    }

    private void sendRequestWithJWTErrors(String token, String document, String path,
                                          String variableName, Object object,
                                          String message) {
        httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, token))
                .build()
                .document(document)
                .variable(variableName, object)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertUnauthorizedErrorResponse(responseError, path, message);
                });
    }

    private void sendRequestWithJWTErrors(String token, String document, String path,
                                          String variableName, Object object,
                                          String variableName2, Object object2,
                                          String message) {
        httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, token))
                .build()
                .document(document)
                .variable(variableName, object)
                .variable(variableName2, object2)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertUnauthorizedErrorResponse(responseError, path, message);
                });
    }
}
