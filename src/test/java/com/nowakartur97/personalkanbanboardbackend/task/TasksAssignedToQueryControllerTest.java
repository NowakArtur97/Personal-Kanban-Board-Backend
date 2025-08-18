package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.BasicIntegrationTest;
import com.nowakartur97.personalkanbanboardbackend.common.RequestVariable;
import com.nowakartur97.personalkanbanboardbackend.subtask.SubtaskEntity;
import com.nowakartur97.personalkanbanboardbackend.subtask.SubtaskResponse;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.List;
import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.GET_TASKS_ASSIGNED_TO;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TasksAssignedToQueryControllerTest extends BasicIntegrationTest {

    private final static String TASKS_ASSIGNED_TO_PATH = "tasksAssignedTo";

    public TasksAssignedToQueryControllerTest() {
        super(TASKS_ASSIGNED_TO_PATH, GET_TASKS_ASSIGNED_TO,
                new RequestVariable("assignedToId", UUID.randomUUID()));
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenGetAllTasksAssignedToUser_shouldReturnOnlyTasksAssignedToUser(UserRole role) {

        UserEntity userEntity = createUser(role);
        UUID assignedToId = userEntity.getUserId();
        TaskEntity taskEntity = createTask(assignedToId);
        UserEntity author = createUser("developer2", "developer2@domain.com");
        UserEntity assignedTo = createUser("developer3", "developer3@domain.com");
        UserEntity updatedBy = createUser("developer4", "developer4@domain.com");
        createTask(author.getUserId(), assignedTo.getUserId(), updatedBy.getUserId());

        List<TaskResponse> taskResponses = sendGetAllTasksAssignedToUserRequest(userEntity, assignedToId);

        assertThat(taskResponses.size()).isOne();
        TaskResponse taskResponse = taskResponses.getFirst();
        assertTaskResponse(taskResponse, taskEntity, userEntity.getUsername(), userEntity.getUsername(), null);
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenGetAllTasksWithSubtasksAssignedToUser_shouldReturnOnlyTasksWithSubtasksAssignedToUser(UserRole role) {

        UserEntity userEntity = createUser(role);
        UUID assignedToId = userEntity.getUserId();
        TaskEntity taskEntity = createTask(assignedToId);
        SubtaskEntity subtaskEntity = createSubtask(taskEntity.getTaskId(), userEntity.getUserId());
        UserEntity author = createUser("developer2", "developer2@domain.com");
        UserEntity updatedBy = createUser("developer4", "developer4@domain.com");
        SubtaskEntity subtaskEntity2 = createSubtask(taskEntity.getTaskId(), author.getUserId(), userEntity.getUserId(), updatedBy.getUserId());
        UserEntity assignedTo = createUser("developer3", "developer3@domain.com");
        createTask(author.getUserId(), assignedTo.getUserId(), updatedBy.getUserId());

        List<TaskResponse> taskResponses = sendGetAllTasksAssignedToUserRequest(userEntity, assignedToId);

        assertThat(taskResponses.size()).isOne();
        TaskResponse taskResponse = taskResponses.getFirst();
        assertTaskResponse(taskResponse, taskEntity, userEntity.getUsername(), userEntity.getUsername(), null);
        SubtaskResponse subtaskResponse = taskResponse.getSubtasks().getFirst();
        assertSubtaskResponse(subtaskResponse, subtaskEntity, userEntity.getUsername(), userEntity.getUsername(), null);
        SubtaskResponse subtaskResponse2 = taskResponse.getSubtasks().getLast();
        assertSubtaskResponse(subtaskResponse2, subtaskEntity2, author.getUsername(), userEntity.getUsername(), updatedBy.getUsername());
    }

    @Test
    public void whenGetAllTasksAssignedToNotExistingUser_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();

        List<TaskResponse> taskResponses = sendGetAllTasksAssignedToUserRequest(userEntity, UUID.randomUUID());

        assertThat(taskResponses.size()).isZero();
    }

    @Test
    public void whenGetAllTasksAssignedToUserWithoutAssignedToId_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();

        GraphQlTester.Errors errors = httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(GET_TASKS_ASSIGNED_TO)
                .execute()
                .errors();
        assertValidationErrorResponse(errors, new SourceLocation(1, 25),
                "Variable 'assignedToId' has an invalid value: Variable 'assignedToId' has coerced Null value for NonNull type 'UUID!'");
    }

    private List<TaskResponse> sendGetAllTasksAssignedToUserRequest(UserEntity userEntity, UUID assignedToId) {
        return httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(GET_TASKS_ASSIGNED_TO)
                .variable("assignedToId", assignedToId)
                .execute()
                .errors()
                .verify()
                .path(TASKS_ASSIGNED_TO_PATH)
                .entityList(TaskResponse.class)
                .get();
    }
}
