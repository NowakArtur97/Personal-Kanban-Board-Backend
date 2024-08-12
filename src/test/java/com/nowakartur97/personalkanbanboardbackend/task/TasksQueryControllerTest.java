package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.integration.IntegrationTest;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.execution.ErrorType;

import java.time.LocalDate;
import java.util.List;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.GET_TASKS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TasksQueryControllerTest extends IntegrationTest {

    private final static String TASKS_PATH = "tasks";

    @Test
    public void whenGetTasksByUsername_shouldReturnTasksForSpecifiedUser() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity);

        List<TaskResponse> taskResponses = httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(GET_TASKS)
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

        httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, jwtUtil.generateToken("notExistingUser", UserRole.USER.name())))
                .build()
                .document(GET_TASKS)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertErrorResponse(responseError, "tasks", ErrorType.NOT_FOUND,
                            "User with username: 'notExistingUser' not found.");
                });
    }

    @Test
    public void whenGetTasksWithoutProvidingAuthorizationHeader_shouldReturnGraphQLErrorResponse() {

        httpGraphQlTester
                .mutate()
                .build()
                .document(GET_TASKS)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertErrorResponse(responseError, "tasks", ErrorType.UNAUTHORIZED, "Unauthorized");
                });
    }

    @Test
    public void whenGetTasksWithExpiredToken_shouldReturnGraphQLErrorResponse() {

        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjpbIlVTRVIiXSwic3ViIjoidGVzdFVzZXIiLCJpYXQiOjE3MTEyNzg1ODAsImV4cCI6MTcxMTI3ODU4MH0.nouAgIkDaanTk0LX37HSRjM4SDZxqBqz1gDufnU2fzQ";

        httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, expiredToken))
                .build()
                .document(GET_TASKS)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertErrorResponse(responseError, "tasks", ErrorType.UNAUTHORIZED, "JWT expired");
                });
    }

    @Test
    public void whenGetTasksWithInvalidToken_shouldReturnGraphQLErrorResponse() {

        String invalidToken = "invalid";

        httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, invalidToken))
                .build()
                .document(GET_TASKS)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertErrorResponse(responseError, "tasks", ErrorType.UNAUTHORIZED,
                            "Invalid compact JWT string: Compact JWSs must contain exactly 2 period characters, and compact JWEs must contain exactly 4.  Found: 0");
                });
    }

    @Test
    public void whenGetTasksWithDifferentTokenSignature_shouldReturnGraphQLErrorResponse() {

        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjpbIlVTRVIiXSwic3ViIjoidXNlciIsImlhdCI6MTcxMTIwOTEzMiwiZXhwIjoxNzExMjE5OTMyfQ.n-h8vIdov2voZhwNdqbmgiO44XjeCdAMzf7ddqufoXc";

        httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, invalidToken))
                .build()
                .document(GET_TASKS)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertErrorResponse(responseError, "tasks", ErrorType.UNAUTHORIZED,
                            "JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.");
                });
    }

    private void assertTask(TaskResponse taskResponse, TaskEntity taskEntity, String username) {
        assertThat(taskResponse).isNotNull();
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

    private TaskEntity createTask(UserEntity user) {
        TaskEntity task = TaskEntity.builder()
                .title("task1")
                .description("desc1")
                .assignedTo(user.getUserId())
                .status(TaskStatus.READY_TO_START)
                .priority(TaskPriority.MEDIUM)
                .targetEndDate(LocalDate.now())
                .createdBy(user.getUserId())
                .createdOn(LocalDate.now())
                .updatedOn(LocalDate.now())
                .updatedBy(user.getUserId())
                .build();
        return taskRepository.save(task).block();
    }
}
