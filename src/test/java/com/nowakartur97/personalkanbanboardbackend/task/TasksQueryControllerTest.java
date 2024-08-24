package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.integration.IntegrationTest;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.ResponseError;

import java.time.Instant;
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
        assertTaskResponse(taskResponse, taskEntity, userEntity.getUsername());
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
                    assertNotFoundErrorResponse(responseError, TASKS_PATH, "User with username: 'notExistingUser' not found.");
                });
    }

    @Test
    public void whenGetTasksWithoutProvidingAuthorizationHeader_shouldReturnGraphQLErrorResponse() {
        runTestForSendingRequestWithoutProvidingAuthorizationHeader(GET_TASKS, TASKS_PATH);
    }

    @Test
    public void whenGetTasksWithExpiredToken_shouldReturnGraphQLErrorResponse() {
        runTestForSendingRequestWithExpiredToken(GET_TASKS, TASKS_PATH);
    }

    @Test
    public void whenGetTasksWithInvalidToken_shouldReturnGraphQLErrorResponse() {
        runTestForSendingRequestWithInvalidToken(GET_TASKS, TASKS_PATH);
    }

    @Test
    public void whenGetTasksWithDifferentTokenSignature_shouldReturnGraphQLErrorResponse() {
        runTestForSendingRequestWithDifferentTokenSignature(GET_TASKS, TASKS_PATH);
    }

    private void assertTaskResponse(TaskResponse taskResponse, TaskEntity taskEntity, String username) {
        assertThat(taskResponse).isNotNull();
        assertThat(taskResponse.taskId()).isEqualTo(taskEntity.getTaskId());
        assertThat(taskResponse.title()).isEqualTo(taskEntity.getTitle());
        assertThat(taskResponse.status()).isEqualTo(taskEntity.getStatus());
        assertThat(taskResponse.priority()).isEqualTo(taskEntity.getPriority());
        assertThat(taskResponse.targetEndDate()).isEqualTo(taskEntity.getTargetEndDate());
        assertThat(taskResponse.assignedTo()).isEqualTo(username);
        assertThat(Instant.parse(taskResponse.createdOn()).toEpochMilli()).isEqualTo(taskEntity.getCreatedOn().toEpochMilli());
        assertThat(taskResponse.createdBy()).isEqualTo(username);
        assertThat(Instant.parse(taskResponse.updatedOn()).toEpochMilli()).isEqualTo(taskEntity.getUpdatedOn().toEpochMilli());
        // TODO: Change
        assertThat(taskResponse.updatedBy()).isEqualTo(null);
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
                .createdOn(Instant.now())
                .updatedOn(Instant.now())
                .updatedBy(user.getUserId())
                .build();
        return taskRepository.save(task).block();
    }
}
