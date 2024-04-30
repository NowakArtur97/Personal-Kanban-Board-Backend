package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.integration.IntegrationTest;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.execution.ErrorType;

import java.time.LocalDate;
import java.util.List;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.GET_TASKS;
import static graphql.ErrorType.ValidationError;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TasksQueryControllerTest extends IntegrationTest {

    private final static String TASKS_PATH = "tasks";

    @Test
    public void whenGetTasksByUsername_shouldReturnTasksForSpecifiedUser() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity);

        List<TaskResponse> taskResponses = httpGraphQlTester
                .mutate()
                .headers(headers -> headers.add(jwtConfigurationProperties.getAuthorizationHeader(),
                        jwtConfigurationProperties.getAuthorizationType()
                                + " "
                                + jwtUtil.generateToken(userEntity.getUsername(), userEntity.getRole().name())))
                .build()
                .document(GET_TASKS)
                .variable("username", userEntity.getUsername())
                .execute()
                .errors()
                .verify()
                .path(TASKS_PATH)
                .entityList(TaskResponse.class)
                .get();

        assertThat(taskResponses.size()).isOne();
        TaskResponse taskResponse = taskResponses.getFirst();
        assertTask(taskResponse, taskEntity, userEntity.getUsername());
    }

    @Test
    public void whenGetTasksForNotExistingUser_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();

        httpGraphQlTester
                .mutate()
                .headers(headers -> headers.add(jwtConfigurationProperties.getAuthorizationHeader(),
                        jwtConfigurationProperties.getAuthorizationType()
                                + " "
                                + jwtUtil.generateToken(userEntity.getUsername(), userEntity.getRole().name())))
                .build()
                .document(GET_TASKS)
                .variable("username", "notExistingUser")
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertErrorResponse(responseError, ErrorType.NOT_FOUND,
                            "User with username: 'notExistingUser' not found.");
                });
    }

    @Test
    public void whenGetTasksWithoutProvidingUsername_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();

        httpGraphQlTester
                .mutate()
                .headers(headers -> headers.add(jwtConfigurationProperties.getAuthorizationHeader(),
                        jwtConfigurationProperties.getAuthorizationType()
                                + " "
                                + jwtUtil.generateToken(userEntity.getUsername(), userEntity.getRole().name())))
                .build()
                .document(GET_TASKS)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertErrorResponse(responseError, ValidationError,
                            "Variable 'username' has an invalid value: Variable 'username' has coerced Null value for NonNull type 'String!'");
                });
    }

    @Test
    public void whenGetTasksWithoutProvidingAuthorizationHeader_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();

        httpGraphQlTester
                .mutate()
                .build()
                .document(GET_TASKS)
                .variable("username", userEntity.getUsername())
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertErrorResponse(responseError, ErrorType.UNAUTHORIZED, "Unauthorized");
                });
    }

    @Test
    public void whenGetTasksWithExpiredToken_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();

        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjpbIlVTRVIiXSwic3ViIjoidGVzdFVzZXIiLCJpYXQiOjE3MTEyNzg1ODAsImV4cCI6MTcxMTI3ODU4MH0.nouAgIkDaanTk0LX37HSRjM4SDZxqBqz1gDufnU2fzQ";

        httpGraphQlTester
                .mutate()
                .headers(headers -> headers.add(jwtConfigurationProperties.getAuthorizationHeader(),
                        jwtConfigurationProperties.getAuthorizationType()
                                + " "
                                + expiredToken))
                .build()
                .document(GET_TASKS)
                .variable("username", userEntity.getUsername())
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertErrorResponse(responseError, ErrorType.UNAUTHORIZED, "JWT expired");
                });
    }

    @Test
    public void whenGetTasksWithInvalidToken_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();

        String invalidToken = "invalid";

        httpGraphQlTester
                .mutate()
                .headers(headers -> headers.add(jwtConfigurationProperties.getAuthorizationHeader(),
                        jwtConfigurationProperties.getAuthorizationType()
                                + " "
                                + invalidToken))
                .build()
                .document(GET_TASKS)
                .variable("username", userEntity.getUsername())
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertErrorResponse(responseError, ErrorType.UNAUTHORIZED,
                            "Invalid compact JWT string: Compact JWSs must contain exactly 2 period characters, and compact JWEs must contain exactly 4.  Found: 0");
                });
    }

    @Test
    public void whenGetTasksWithDifferentTokenSignature_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();

        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjpbIlVTRVIiXSwic3ViIjoidXNlciIsImlhdCI6MTcxMTIwOTEzMiwiZXhwIjoxNzExMjE5OTMyfQ.n-h8vIdov2voZhwNdqbmgiO44XjeCdAMzf7ddqufoXc";

        httpGraphQlTester
                .mutate()
                .headers(headers -> headers.add(jwtConfigurationProperties.getAuthorizationHeader(),
                        jwtConfigurationProperties.getAuthorizationType()
                                + " "
                                + invalidToken))
                .build()
                .document(GET_TASKS)
                .variable("username", userEntity.getUsername())
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertErrorResponse(responseError, ErrorType.UNAUTHORIZED,
                            "JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.");
                });
    }

    private void assertTask(TaskResponse taskResponse, TaskEntity taskEntity, String username) {
        assertThat(taskResponse.taskId()).isEqualTo(taskEntity.getTaskId());
        assertThat(taskResponse.title()).isEqualTo(taskEntity.getTitle());
        assertThat(taskResponse.status()).isEqualTo(taskEntity.getStatus());
        assertThat(taskResponse.priority()).isEqualTo(taskEntity.getPriority());
        assertThat(taskResponse.targetEndDate()).isEqualTo(taskEntity.getTargetEndDate());
        assertThat(taskResponse.assignedTo()).isEqualTo(username);
        assertThat(taskResponse.createdOn()).isEqualTo(taskEntity.getCreatedOn());
        assertThat(taskResponse.createdBy()).isEqualTo(username);
        assertThat(taskResponse.updatedOn()).isEqualTo(taskEntity.getUpdatedOn());
        assertThat(taskResponse.updatedBy()).isEqualTo(username);
    }

    private void assertErrorResponse(ResponseError responseError, ErrorType errorType, String message) {
        assertThat(responseError.getErrorType()).isEqualTo(errorType);
        assertErrorResponse(responseError, message, "tasks", new SourceLocation(2, 3));
    }

    private void assertErrorResponse(ResponseError responseError, graphql.ErrorType errorType, String message) {
        assertThat(responseError.getErrorType()).isEqualTo(errorType);
        assertErrorResponse(responseError, message, "", new SourceLocation(1, 25));
    }

    private TaskEntity createTask(UserEntity user) {
        TaskEntity task = new TaskEntity();
        task.setTitle("task1");
        task.setDescription("desc1");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setPriority(TaskPriority.MEDIUM);
        task.setTargetEndDate(LocalDate.now());
        task.setAssignedTo(user.getUserId());
        task.setCreatedOn(LocalDate.now());
        task.setCreatedBy(user.getUserId());
        task.setUpdatedOn(LocalDate.now());
        task.setUpdatedBy(user.getUserId());
        return taskRepository.save(task).block();
    }
}
