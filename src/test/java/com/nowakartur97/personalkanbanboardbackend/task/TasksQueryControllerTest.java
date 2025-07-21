package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.integration.IntegrationTest;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.GET_TASKS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TasksQueryControllerTest extends IntegrationTest {

    private final static String TASKS_PATH = "tasks";

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenGetAllTasks_shouldReturnAllTasks(UserRole role) {

        UserEntity userEntity = createUser(role);
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        UserEntity author = createUser("developer2", "developer2@domain.com");
        UserEntity assignedTo = createUser("developer3", "developer3@domain.com");
        UserEntity updatedBy = createUser("developer4", "developer4@domain.com");
        TaskEntity taskEntity2 = createTask(author.getUserId(), assignedTo.getUserId(), updatedBy.getUserId());

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
        TaskResponse taskResponse2 = taskResponses.getLast();
        assertTaskResponse(taskResponse, taskEntity, userEntity.getUsername(), userEntity.getUsername(), null);
        assertTaskResponse(taskResponse2, taskEntity2, author.getUsername(), assignedTo.getUsername(), updatedBy.getUsername());
    }

    @Test
    public void whenGetTasksByNotExistingUser_shouldReturnGraphQLErrorResponse() {

        runTestForSendingRequestWithInvalidCredentials(GET_TASKS, TASKS_PATH,
                jwtUtil.generateToken("notExistingUser", UserRole.USER.name()));
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
}
