package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.integration.IntegrationTest;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.ResponseError;

import java.time.Instant;
import java.util.List;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.GET_TASKS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TasksQueryControllerTest extends IntegrationTest {

    private final static String TASKS_PATH = "tasks";

    @Test
    public void whenGetTasksByUsername_shouldReturnTasksForSpecifiedUser() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        UserEntity author = createUser("developer2", "developer2@domain.com");
        UserEntity assignedTo = createUser("developer3", "developer3@domain.com");
        UserEntity updatedBy = createUser("developer4", "developer4@domain.com");
        TaskEntity taskEntity3 = createTask(author.getUserId(), assignedTo.getUserId(), updatedBy.getUserId());

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

        assertThat(taskResponses.size()).isEqualTo(2);
        TaskResponse taskResponse = taskResponses.getFirst();
        TaskResponse taskResponse3 = taskResponses.getLast();
        assertTaskResponse(taskResponse, taskEntity, userEntity.getUsername(), userEntity.getUsername(), null);
        assertTaskResponse(taskResponse3, taskEntity3, author.getUsername(), assignedTo.getUsername(), updatedBy.getUsername());
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

    private void assertTaskResponse(TaskResponse taskResponse, TaskEntity taskEntity,
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
}
