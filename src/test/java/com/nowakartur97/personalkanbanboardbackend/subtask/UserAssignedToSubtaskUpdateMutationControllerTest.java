package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.common.DoubleRequestVariable;
import com.nowakartur97.personalkanbanboardbackend.common.RequestVariable;
import com.nowakartur97.personalkanbanboardbackend.common.TaskIntegrationTest;
import com.nowakartur97.personalkanbanboardbackend.task.TaskEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.UPDATE_USER_ASSIGNED_TO_SUBTASK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UserAssignedToSubtaskUpdateMutationControllerTest extends TaskIntegrationTest {

    private final static String UPDATE_USER_ASSIGNED_TO_SUBTASK_PATH = "updateUserAssignedToSubtask";

    public UserAssignedToSubtaskUpdateMutationControllerTest() {
        super(UPDATE_USER_ASSIGNED_TO_SUBTASK_PATH, UPDATE_USER_ASSIGNED_TO_SUBTASK,
                new DoubleRequestVariable("subtaskId", UUID.randomUUID(), "assignedToId", UUID.randomUUID()));
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenUpdateUserAssignedToSubtask_shouldReturnTaskResponse(UserRole role) {

        UserEntity userEntity = createUser(role);
        TaskEntity taskEntity = createTask(userEntity.getUserId(), userEntity.getUserId(), userEntity.getUserId());
        SubtaskEntity subtaskEntity = createSubtask(taskEntity.getTaskId(), userEntity.getUserId(), userEntity.getUserId(), userEntity.getUserId());
        UserEntity assignedTo = createUser("developer", "developer@domain.com");
        DoubleRequestVariable doubleRequestVariable = new DoubleRequestVariable("subtaskId", subtaskEntity.getSubtaskId(), "assignedToId", assignedTo.getUserId());

        SubtaskResponse subtaskResponse = (SubtaskResponse) sendRequest(userEntity, document, path, doubleRequestVariable, SubtaskResponse.class, false);

        SubtaskEntity updatedSubtaskEntity = subtaskRepository.findAll().blockLast();
        assertTaskEntity(subtaskEntity, updatedSubtaskEntity, assignedTo.getUserId());
        assertSubtaskResponse(subtaskResponse, updatedSubtaskEntity, assignedTo.getUsername(), userEntity.getUsername(), userEntity.getUsername());
    }

    @Test
    public void whenUpdateUserAssignedToNotExistingSubtask_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        UUID subtaskId = UUID.randomUUID();

        assertNotFoundErrorResponse(sendUpdateUserAssignedToSubtaskRequestWithErrors(userEntity, subtaskId, userEntity.getUserId()),
                UPDATE_USER_ASSIGNED_TO_SUBTASK_PATH, "Subtask with subtaskId: '" + subtaskId + "' not found.");
    }

    @Test
    public void whenUpdateUserAssignedToSubtaskForNotExistingUser_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        SubtaskEntity subtaskEntity = createSubtask(taskEntity.getTaskId(), userEntity.getUserId());
        UUID assignedTo = UUID.randomUUID();

        assertNotFoundErrorResponse(sendUpdateUserAssignedToSubtaskRequestWithErrors(userEntity, subtaskEntity.getSubtaskId(), assignedTo),
                UPDATE_USER_ASSIGNED_TO_SUBTASK_PATH, "User with userId: '" + assignedTo + "' not found.");
    }

    @Test
    public void whenUpdateUserAssignedToSubtaskWithoutSubtaskId_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        RequestVariable reqVariable = new RequestVariable("assignedToId", UUID.randomUUID());

        assertValidationErrorResponse(sendRequestWithErrors(userEntity, document, reqVariable), new SourceLocation(1, 42),
                "Variable 'subtaskId' has an invalid value: Variable 'subtaskId' has coerced Null value for NonNull type 'UUID!'");
    }

    @Test
    public void whenUpdateUserAssignedToSubtaskWithoutAssignedToId_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        RequestVariable reqVariable = new RequestVariable("subtaskId", UUID.randomUUID());

        assertValidationErrorResponse(sendRequestWithErrors(userEntity, document, reqVariable), new SourceLocation(1, 61),
                "Variable 'assignedToId' has an invalid value: Variable 'assignedToId' has coerced Null value for NonNull type 'UUID!'");
    }

    private GraphQlTester.Errors sendUpdateUserAssignedToSubtaskRequestWithErrors(UserEntity userEntity, UUID subtaskId, UUID assignedToId) {
        DoubleRequestVariable doubleRequestVariable = new DoubleRequestVariable("subtaskId", subtaskId, "assignedToId", assignedToId);
        return sendRequestWithErrors(userEntity, document, doubleRequestVariable);
    }

    private void assertTaskEntity(SubtaskEntity subtaskEntity, SubtaskEntity subtaskEntityAfterUpdate, UUID assignedTo) {
        assertBaseTaskEntity(subtaskEntity, subtaskEntityAfterUpdate, assignedTo);
        assertThat(subtaskEntityAfterUpdate.getSubtaskId()).isEqualTo(subtaskEntity.getSubtaskId());
        assertThat(subtaskEntityAfterUpdate.getTaskId()).isEqualTo(subtaskEntity.getTaskId());
    }
}
