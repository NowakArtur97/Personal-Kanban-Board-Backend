package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.common.RequestVariable;
import com.nowakartur97.personalkanbanboardbackend.common.TaskIntegrationTest;
import com.nowakartur97.personalkanbanboardbackend.task.TaskEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.DELETE_ALL_SUBTASKS_BY_TASK_ID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AllSubtasksDeletionByTaskIdMutationControllerTest extends TaskIntegrationTest {

    private final static String DELETE_ALL_SUBTASKS_BY_TASK_ID_PATH = "deleteAllSubtasksByTaskId";

    public AllSubtasksDeletionByTaskIdMutationControllerTest() {
        super(DELETE_ALL_SUBTASKS_BY_TASK_ID_PATH, DELETE_ALL_SUBTASKS_BY_TASK_ID,
                new RequestVariable("taskId", UUID.randomUUID()));
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenDeleteAllSubtasksByTaskId_shouldReturnEmptyResponse(UserRole role) {

        UserEntity userEntity = createUser(role);
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        createSubtask(taskEntity.getTaskId(), userEntity.getUserId());
        createSubtask(taskEntity.getTaskId(), userEntity.getUserId());

        sendDeleteAllSubtasksByTaskIdRequest(userEntity, taskEntity.getTaskId());

        assertThat(subtaskRepository.count().block()).isZero();
        assertThat(taskRepository.count().block()).isOne();
    }

    @Test
    public void whenDeleteAllSubtasksByTaskIdForNotExistingTask_shouldReturnEmptyResponse() {

        UserEntity userEntity = createUser();

        sendDeleteAllSubtasksByTaskIdRequest(userEntity, UUID.randomUUID());

        assertThat(taskRepository.count().block()).isZero();
    }

    @Test
    public void whenDeleteAllSubtasksByTaskIdWithoutTaskId_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();

        assertValidationErrorResponse(sendRequestWithErrors(userEntity, document, null), new SourceLocation(1, 41),
                "Variable 'taskId' has an invalid value: Variable 'taskId' has coerced Null value for NonNull type 'UUID!'");
    }

    private void sendDeleteAllSubtasksByTaskIdRequest(UserEntity userEntity, UUID taskId) {
        RequestVariable reqVariable = new RequestVariable("taskId", taskId);
        sendRequest(userEntity, document, path, reqVariable, null, false);
    }
}
