package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.integration.IntegrationTest;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.GET_TASKS_ASSIGNED_TO;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TasksAssignedToQueryControllerTest extends IntegrationTest {

    private final static String TASKS_ASSIGNED_TO_PATH = "tasksAssignedTo";

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenGetAllTasksAssignedToUser_shouldReturnOnlyTasksAssignedToUser(UserRole role) {

        UserEntity userEntity = createUser(role);
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        UserEntity author = createUser("developer2", "developer2@domain.com");
        UserEntity assignedTo = createUser("developer3", "developer3@domain.com");
        UserEntity updatedBy = createUser("developer4", "developer4@domain.com");
        createTask(author.getUserId(), assignedTo.getUserId(), updatedBy.getUserId());

        List<TaskResponse> taskResponses = httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(GET_TASKS_ASSIGNED_TO)
                .variable("assignedToId", userEntity.getUserId())
                .execute()
                .errors()
                .verify()
                .path(TASKS_ASSIGNED_TO_PATH)
                .entityList(TaskResponse.class)
                .get();

        assertThat(taskResponses.size()).isOne();
        TaskResponse taskResponse = taskResponses.getFirst();
        assertTaskResponse(taskResponse, taskEntity, userEntity.getUsername(), userEntity.getUsername(), null);
    }

    @Test
    public void whenGetAllTasksAssignedToNotExistingUser_shouldReturnGraphQLErrorResponse() {

        runTestForSendingRequestWithInvalidCredentials(GET_TASKS_ASSIGNED_TO, TASKS_ASSIGNED_TO_PATH,
                jwtUtil.generateToken("notExistingUser", UserRole.USER.name()), "assignedToId", UUID.randomUUID());
    }

    @Test
    public void whenGetAllTasksAssignedToUserWithoutProvidingAuthorizationHeader_shouldReturnGraphQLErrorResponse() {
        runTestForSendingRequestWithoutProvidingAuthorizationHeader(GET_TASKS_ASSIGNED_TO, TASKS_ASSIGNED_TO_PATH, "assignedToId", UUID.randomUUID());
    }

    @Test
    public void whenGetAllTasksAssignedToUserWithExpiredToken_shouldReturnGraphQLErrorResponse() {
        runTestForSendingRequestWithExpiredToken(GET_TASKS_ASSIGNED_TO, TASKS_ASSIGNED_TO_PATH, "assignedToId", UUID.randomUUID());
    }

    @Test
    public void whenGetAllTasksAssignedToUserWithInvalidToken_shouldReturnGraphQLErrorResponse() {
        runTestForSendingRequestWithInvalidToken(GET_TASKS_ASSIGNED_TO, TASKS_ASSIGNED_TO_PATH, "assignedToId", UUID.randomUUID());
    }

    @Test
    public void whenGetAllTasksAssignedToUserWithDifferentTokenSignature_shouldReturnGraphQLErrorResponse() {
        runTestForSendingRequestWithDifferentTokenSignature(GET_TASKS_ASSIGNED_TO, TASKS_ASSIGNED_TO_PATH, "assignedToId", UUID.randomUUID());
    }
}
