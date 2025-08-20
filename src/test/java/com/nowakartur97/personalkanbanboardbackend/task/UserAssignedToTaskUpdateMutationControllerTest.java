package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.DoubleRequestVariable;
import com.nowakartur97.personalkanbanboardbackend.common.RequestVariable;
import com.nowakartur97.personalkanbanboardbackend.common.TaskIntegrationTest;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.UPDATE_USER_ASSIGNED_TO_TASK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UserAssignedToTaskUpdateMutationControllerTest extends TaskIntegrationTest {

    private final static String UPDATE_USER_ASSIGNED_TO_TASK_PATH = "updateUserAssignedToTask";

    public UserAssignedToTaskUpdateMutationControllerTest() {
        super(UPDATE_USER_ASSIGNED_TO_TASK_PATH, UPDATE_USER_ASSIGNED_TO_TASK,
                new DoubleRequestVariable("taskId", UUID.randomUUID(), "assignedToId", UUID.randomUUID()));
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenUpdateUserAssignedToTask_shouldReturnTaskResponse(UserRole role) {

        UserEntity userEntity = createUser(role);
        TaskEntity taskEntity = createTask(userEntity.getUserId(), userEntity.getUserId(), userEntity.getUserId());
        UserEntity assignedTo = createUser("developer", "developer@domain.com");
        DoubleRequestVariable doubleRequestVariable = new DoubleRequestVariable("taskId", taskEntity.getTaskId(), "assignedToId", assignedTo.getUserId());

        TaskResponse taskResponse = (TaskResponse) sendRequest(userEntity, document, path, doubleRequestVariable, TaskResponse.class, false);

        TaskEntity udatedTaskEntity = taskRepository.findAll().blockLast();
        assertTaskEntity(taskEntity, udatedTaskEntity, assignedTo.getUserId());
        assertThat(udatedTaskEntity.getTaskId()).isEqualTo(taskEntity.getTaskId());
        assertTaskResponse(taskResponse, udatedTaskEntity, assignedTo.getUsername(), userEntity.getUsername(), userEntity.getUsername());
    }

    @Test
    public void whenUpdateUserAssignedToNotExistingTask_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        UUID taskId = UUID.randomUUID();

        assertNotFoundErrorResponse(sendUpdateUserAssignedToTaskRequestWithErrors(userEntity, taskId, userEntity.getUserId()),
                UPDATE_USER_ASSIGNED_TO_TASK_PATH, "Task with taskId: '" + taskId + "' not found.");
    }

    @Test
    public void whenUpdateUserAssignedToTaskForNotExistingUser_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        UUID assignedTo = UUID.randomUUID();

        assertNotFoundErrorResponse(sendUpdateUserAssignedToTaskRequestWithErrors(userEntity, taskEntity.getTaskId(), assignedTo),
                UPDATE_USER_ASSIGNED_TO_TASK_PATH, "User with userId: '" + assignedTo + "' not found.");
    }

    @Test
    public void whenUpdateUserAssignedToTaskWithoutTaskId_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        RequestVariable reqVariable = new RequestVariable("assignedToId", UUID.randomUUID());

        assertValidationErrorResponse(sendRequestWithErrors(userEntity, document, reqVariable), new SourceLocation(1, 39),
                "Variable 'taskId' has an invalid value: Variable 'taskId' has coerced Null value for NonNull type 'UUID!'");
    }

    @Test
    public void whenUpdateUserAssignedToTaskWithoutAssignedToId_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        RequestVariable reqVariable = new RequestVariable("taskId", UUID.randomUUID());

        assertValidationErrorResponse(sendRequestWithErrors(userEntity, document, reqVariable), new SourceLocation(1, 55),
                "Variable 'assignedToId' has an invalid value: Variable 'assignedToId' has coerced Null value for NonNull type 'UUID!'");
    }

    private GraphQlTester.Errors sendUpdateUserAssignedToTaskRequestWithErrors(UserEntity userEntity, UUID taskId, UUID assignedToId) {
        DoubleRequestVariable doubleRequestVariable = new DoubleRequestVariable("taskId", taskId, "assignedToId", assignedToId);
        return sendRequestWithErrors(userEntity, document, doubleRequestVariable);
    }

    private void assertTaskEntity(TaskEntity taskEntity, TaskEntity taskEntityAfterUpdate, UUID assignedTo) {
        assertThat(taskEntityAfterUpdate).isNotNull();
        assertThat(taskEntityAfterUpdate.getTaskId()).isEqualTo(taskEntity.getTaskId());
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
}
