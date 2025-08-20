package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.RequestVariable;
import com.nowakartur97.personalkanbanboardbackend.common.TaskIntegrationTest;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.DELETE_TASK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TaskDeletionMutationControllerTest extends TaskIntegrationTest {

    private final static String DELETE_TASK_PATH = "deleteTask";

    public TaskDeletionMutationControllerTest() {
        super(DELETE_TASK_PATH, DELETE_TASK, new RequestVariable("taskId", UUID.randomUUID()));
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenDeleteExistingTask_shouldReturnEmptyResponse(UserRole role) {

        UserEntity userEntity = createUser(role);
        TaskEntity taskEntity = createTask(userEntity.getUserId());

        sendDeleteTaskRequest(userEntity, taskEntity.getTaskId());

        assertThat(taskRepository.count().block()).isZero();
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenDeleteExistingTaskWithSubtasks_shouldReturnEmptyResponse(UserRole role) {

        UserEntity userEntity = createUser(role);
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        createSubtask(taskEntity.getTaskId(), userEntity.getUserId());

        sendDeleteTaskRequest(userEntity, taskEntity.getTaskId());

        assertThat(taskRepository.count().block()).isZero();
        assertThat(subtaskRepository.count().block()).isZero();
    }

    @Test
    public void whenDeleteNotExistingTask_shouldReturnEmptyResponse() {

        UserEntity userEntity = createUser();

        sendDeleteTaskRequest(userEntity, UUID.randomUUID());

        assertThat(taskRepository.count().block()).isZero();
    }

    @Test
    public void whenDeleteTaskWithoutTaskId_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();

        assertValidationErrorResponse(sendRequestWithErrors(userEntity, document, null), new SourceLocation(1, 22),
                "Variable 'taskId' has an invalid value: Variable 'taskId' has coerced Null value for NonNull type 'UUID!'");
    }

    private void sendDeleteTaskRequest(UserEntity userEntity, UUID taskId) {
        RequestVariable reqVariable = new RequestVariable("taskId", taskId);
        sendRequest(userEntity, document, path, reqVariable, null, false);
    }
}
