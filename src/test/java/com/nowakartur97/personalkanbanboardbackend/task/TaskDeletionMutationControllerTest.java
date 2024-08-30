package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.integration.IntegrationTest;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.graphql.ResponseError;

import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.DELETE_TASK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TaskDeletionMutationControllerTest extends IntegrationTest {

    private final static String DELETE_TASK_PATH = "deleteTask";

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenDeleteExistingTask_shouldReturnEmptyResponse(UserRole role) {

        UserEntity userEntity = createUser(role);
        TaskEntity taskEntity = createTask(userEntity.getUserId());

        sendDeleteTaskRequest(userEntity, taskEntity.getTaskId());

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

        httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(DELETE_TASK)
                .execute()
                .errors()
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertValidationErrorResponse(responseError, new SourceLocation(1, 22),
                                    "Variable 'taskId' has an invalid value: Variable 'taskId' has coerced Null value for NonNull type 'UUID!'"
                            );
                        });
    }

    @Test
    public void whenDeleteTaskWithoutProvidingAuthorizationHeader_shouldReturnGraphQLErrorResponse() {

        runTestForSendingRequestWithoutProvidingAuthorizationHeader(DELETE_TASK, DELETE_TASK_PATH, "taskId", UUID.randomUUID());
    }

    @Test
    public void whenDeleteTaskWithExpiredToken_shouldReturnGraphQLErrorResponse() {

        runTestForSendingRequestWithExpiredToken(DELETE_TASK, DELETE_TASK_PATH, "taskId", UUID.randomUUID());
    }

    @Test
    public void whenDeleteTaskWithInvalidToken_shouldReturnGraphQLErrorResponse() {

        runTestForSendingRequestWithInvalidToken(DELETE_TASK, DELETE_TASK_PATH, "taskId", UUID.randomUUID());
    }

    @Test
    public void whenDeleteTaskWithDifferentTokenSignature_shouldReturnGraphQLErrorResponse() {

        runTestForSendingRequestWithDifferentTokenSignature(DELETE_TASK, DELETE_TASK_PATH, "taskId", UUID.randomUUID());
    }

    private void sendDeleteTaskRequest(UserEntity userEntity, UUID taskId) {
        httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(DELETE_TASK)
                .variable("taskId", taskId)
                .execute()
                .errors()
                .verify()
                .path(DELETE_TASK_PATH);
    }
}
