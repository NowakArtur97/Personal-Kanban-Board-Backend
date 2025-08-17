package com.nowakartur97.personalkanbanboardbackend.integration;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTConfigurationProperties;
import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskEntity;
import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskResponse;
import com.nowakartur97.personalkanbanboardbackend.common.DoubleRequestVariable;
import com.nowakartur97.personalkanbanboardbackend.common.RequestVariable;
import com.nowakartur97.personalkanbanboardbackend.subtask.SubtaskEntity;
import com.nowakartur97.personalkanbanboardbackend.subtask.SubtaskRepository;
import com.nowakartur97.personalkanbanboardbackend.subtask.SubtaskResponse;
import com.nowakartur97.personalkanbanboardbackend.task.TaskDTO;
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
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
    protected SubtaskRepository subtaskRepository;
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
                        .targetEndDate(LocalDate.now().plusDays(7))
                        .createdOn(Instant.now())
                        .createdBy(assignedToId)
                        .updatedOn(updatedById != null ? Instant.now() : null)
                        .updatedBy(updatedById)
                        .build())
                .block();
    }

    protected SubtaskEntity createSubtask(UUID taskId, UUID authorId) {
        return createSubtask(taskId, authorId, authorId, null);
    }

    protected SubtaskEntity createSubtask(UUID taskId, UUID authorId, UUID assignedToId, UUID updatedById) {
        return subtaskRepository.save(SubtaskEntity.builder()
                        .taskId(taskId)
                        .title("testTask")
                        .description("test")
                        .assignedTo(authorId)
                        .status(TaskStatus.READY_TO_START)
                        .priority(TaskPriority.LOW)
                        .targetEndDate(LocalDate.now().plusDays(7))
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

    protected void assertBaseTaskEntity(BaseTaskEntity taskEntity, TaskDTO taskDTO, UUID createdBy, UUID assignedTo,
                                        TaskStatus taskStatus, TaskPriority taskPriority) {
        assertThat(taskEntity).isNotNull();
        assertThat(taskEntity.getTitle()).isEqualTo(taskDTO.getTitle());
        assertThat(taskEntity.getStatus()).isEqualTo(taskStatus);
        assertThat(taskEntity.getPriority()).isEqualTo(taskPriority);
        assertThat(taskEntity.getTargetEndDate()).isEqualTo(taskDTO.getTargetEndDate());
        assertThat(taskEntity.getAssignedTo()).isEqualTo(assignedTo);
        assertThat(taskEntity.getCreatedOn()).isNotNull();
        assertThat(taskEntity.getCreatedBy()).isEqualTo(createdBy);
        assertThat(taskEntity.getUpdatedOn()).isNull();
        assertThat(taskEntity.getUpdatedBy()).isNull();
    }

    protected void assertBaseTaskEntity(BaseTaskEntity taskEntity, BaseTaskEntity taskEntityAfterUpdate, UUID assignedTo) {
        assertThat(taskEntityAfterUpdate).isNotNull();
        assertThat(taskEntityAfterUpdate.getTitle()).isEqualTo(taskEntity.getTitle());
        assertThat(taskEntityAfterUpdate.getStatus()).isEqualTo(taskEntity.getStatus());
        assertThat(taskEntityAfterUpdate.getPriority()).isEqualTo(taskEntity.getPriority());
        assertThat(taskEntityAfterUpdate.getTargetEndDate()).isEqualTo(taskEntity.getTargetEndDate());
        assertThat(taskEntityAfterUpdate.getAssignedTo()).isEqualTo(assignedTo);
        assertThat(taskEntityAfterUpdate.getCreatedOn().toEpochMilli()).isEqualTo(taskEntity.getCreatedOn().toEpochMilli());
        assertThat(taskEntityAfterUpdate.getCreatedBy()).isEqualTo(taskEntity.getCreatedBy());
        assertThat(taskEntityAfterUpdate.getUpdatedOn()).isAfter(taskEntity.getUpdatedOn());
        assertThat(taskEntityAfterUpdate.getUpdatedBy()).isEqualTo(taskEntity.getUpdatedBy());
    }

    protected void assertBaseTaskResponse(BaseTaskResponse taskResponse, TaskDTO taskDTO, String createdBy, String assignedTo,
                                          TaskStatus status, TaskPriority priority) {
        assertThat(taskResponse).isNotNull();
        assertThat(taskResponse.getTaskId()).isNotNull();
        assertThat(taskResponse.getTitle()).isEqualTo(taskDTO.getTitle());
        assertThat(taskResponse.getStatus()).isEqualTo(status);
        assertThat(taskResponse.getPriority()).isEqualTo(priority);
        assertThat(taskResponse.getTargetEndDate()).isEqualTo(taskDTO.getTargetEndDate());
        assertThat(taskResponse.getAssignedTo()).isEqualTo(assignedTo);
        assertThat(taskResponse.getCreatedOn()).isNotNull();
        assertThat(taskResponse.getCreatedBy()).isEqualTo(createdBy);
        assertThat(taskResponse.getUpdatedOn()).isNull();
        assertThat(taskResponse.getUpdatedBy()).isNull();
    }

    protected void assertBaseTaskResponse(BaseTaskResponse taskResponse, TaskEntity taskEntity, TaskDTO taskDTO, String createdBy,
                                          String updatedBy, String assignedTo, TaskStatus status, TaskPriority priority) {
        assertThat(taskResponse).isNotNull();
        assertThat(taskResponse.getTaskId()).isEqualTo(taskEntity.getTaskId());
        assertThat(taskResponse.getTitle()).isEqualTo(taskDTO.getTitle());
        assertThat(taskResponse.getStatus()).isEqualTo(status);
        assertThat(taskResponse.getPriority()).isEqualTo(priority);
        assertThat(taskResponse.getTargetEndDate()).isEqualTo(taskDTO.getTargetEndDate());
        assertThat(taskResponse.getAssignedTo()).isEqualTo(assignedTo);
        assertThat(Instant.parse(taskResponse.getCreatedOn()).toEpochMilli()).isEqualTo(taskEntity.getCreatedOn().toEpochMilli());
        assertThat(taskResponse.getCreatedBy()).isEqualTo(createdBy);
        assertThat(Instant.parse(taskResponse.getUpdatedOn()).toEpochMilli()).isEqualTo(taskEntity.getUpdatedOn().toEpochMilli());
        assertThat(taskResponse.getUpdatedBy()).isEqualTo(updatedBy);
    }


    private void assertBaseTaskResponse(BaseTaskResponse taskResponse, BaseTaskEntity taskEntity,
                                        String assignedTo, String createdBy, String updatedBy) {
        assertThat(taskResponse).isNotNull();
        assertThat(taskResponse.getTaskId()).isNotNull();
        assertThat(taskResponse.getTitle()).isEqualTo(taskEntity.getTitle());
        assertThat(taskResponse.getStatus()).isEqualTo(taskEntity.getStatus());
        assertThat(taskResponse.getPriority()).isEqualTo(taskEntity.getPriority());
        assertThat(taskResponse.getTargetEndDate()).isEqualTo(taskEntity.getTargetEndDate());
        assertThat(taskResponse.getAssignedTo()).isEqualTo(assignedTo);
        assertThat(Instant.parse(taskResponse.getCreatedOn()).toEpochMilli()).isEqualTo(taskEntity.getCreatedOn().toEpochMilli());
        assertThat(taskResponse.getCreatedBy()).isEqualTo(createdBy);
        if (updatedBy != null) {
            assertThat(Instant.parse(taskResponse.getUpdatedOn()).toEpochMilli()).isEqualTo(taskEntity.getUpdatedOn().toEpochMilli());
        } else {
            assertThat(taskResponse.getUpdatedOn()).isNull();
        }
        assertThat(taskResponse.getUpdatedBy()).isEqualTo(updatedBy);
    }

    protected void assertTaskResponse(TaskResponse taskResponse, TaskEntity taskEntity,
                                      String createdBy, String assignedTo, String updatedBy) {
        assertBaseTaskResponse(taskResponse, taskEntity, createdBy, assignedTo, updatedBy);
        assertThat(taskResponse.getTaskId()).isEqualTo(taskEntity.getTaskId());
    }

    protected void assertSubtaskResponse(SubtaskResponse subtaskResponse, SubtaskEntity subtaskEntity,
                                         String createdBy, String assignedTo, String updatedBy) {
        assertBaseTaskResponse(subtaskResponse, subtaskEntity, createdBy, assignedTo, updatedBy);
        assertThat(subtaskResponse.getSubtaskId()).isEqualTo(subtaskEntity.getSubtaskId());
        assertThat(subtaskResponse.getTaskId()).isEqualTo(subtaskEntity.getTaskId());
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

    protected void runTestForSendingRequestWithInvalidCredentials(String document, String path, String token, RequestVariable requestVariable) {

        sendRequestWithErrors(token, document, path, requestVariable, "Invalid login credentials.");
    }

    protected void runTestForSendingRequestWithoutProvidingAuthorizationHeader(String document, String path, RequestVariable requestVariable) {

        sendRequestWithErrors(null, document, path, requestVariable, "Unauthorized");
    }

    protected void runTestForSendingRequestWithExpiredToken(String document, String path, RequestVariable requestVariable) {

        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjpbIlVTRVIiXSwic3ViIjoidGVzdFVzZXIiLCJpYXQiOjE3MTEyNzg1ODAsImV4cCI6MTcxMTI3ODU4MH0.nouAgIkDaanTk0LX37HSRjM4SDZxqBqz1gDufnU2fzQ";

        sendRequestWithErrors(expiredToken, document, path, requestVariable, "JWT expired");
    }

    protected void runTestForSendingRequestWithInvalidToken(String document, String path, RequestVariable requestVariable) {

        String invalidToken = "invalid";

        sendRequestWithErrors(invalidToken, document, path, requestVariable,
                "Invalid compact JWT string: Compact JWSs must contain exactly 2 period characters, and compact JWEs must contain exactly 4.  Found: 0");
    }

    protected void runTestForSendingRequestWithDifferentTokenSignature(String document, String path, RequestVariable requestVariable) {

        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjpbIlVTRVIiXSwic3ViIjoidXNlciIsImlhdCI6MTcxMTIwOTEzMiwiZXhwIjoxNzExMjE5OTMyfQ.n-h8vIdov2voZhwNdqbmgiO44XjeCdAMzf7ddqufoXc";

        sendRequestWithErrors(invalidToken, document, path, requestVariable,
                "JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.");
    }

    private void sendRequestWithErrors(String token, String document, String path, RequestVariable requestVariable, String message) {

        HttpGraphQlTester.Builder<?> builder = httpGraphQlTester
                .mutate();
        if (StringUtils.isNotBlank(token)) {
            builder = builder.headers(headers -> addAuthorizationHeader(headers, token));
        }
        GraphQlTester.Request<?> requestDocument = builder.build().document(document);
        GraphQlTester.Request<?> requestVar = requestDocument;
        if (requestVariable instanceof DoubleRequestVariable) {
            DoubleRequestVariable doubleRequestVariable = (DoubleRequestVariable) requestVariable;
            requestVar = requestDocument
                    .variable(requestVariable.getName(), requestVariable.getValue())
                    .variable(doubleRequestVariable.getName2(), doubleRequestVariable.getValue2());
        } else if (requestVariable != null) {
            requestVar = requestDocument
                    .variable(requestVariable.getName(), requestVariable.getValue());
        }
        requestVar.execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertUnauthorizedErrorResponse(responseError, path, message);
                });
    }
}
