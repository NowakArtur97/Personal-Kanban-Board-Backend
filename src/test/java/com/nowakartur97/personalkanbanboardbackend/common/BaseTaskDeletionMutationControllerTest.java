package com.nowakartur97.personalkanbanboardbackend.common;

import com.nowakartur97.personalkanbanboardbackend.task.TaskEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class BaseTaskDeletionMutationControllerTest<E extends TaskEntity> extends TaskIntegrationTest {

    private final int errorSourceLocationColumn;

    public BaseTaskDeletionMutationControllerTest(String path, String document, RequestVariable requestVariable, int errorSourceLocationColumn) {
        super(path, document, requestVariable);
        this.errorSourceLocationColumn = errorSourceLocationColumn;
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenDeleteExistingTask_shouldReturnEmptyResponse(UserRole role) {

        UserEntity userEntity = createUser(role);

        sendDeleteTaskRequest(userEntity);

        assertThat(taskRepository.count().block()).isZero();
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

        assertValidationErrorResponse(sendRequestWithErrors(userEntity, document, null), new SourceLocation(1, errorSourceLocationColumn),
                "Variable '" + requestVariable.getName() + "' has an invalid value: Variable '" + requestVariable.getName() + "' has coerced Null value for NonNull type 'UUID!'");
    }

    private void sendDeleteTaskRequest(UserEntity userEntity) {
        sendDeleteTaskRequest(userEntity, UUID.randomUUID());
    }

    protected abstract void sendDeleteTaskRequest(UserEntity userEntity, UUID taskId);
}
