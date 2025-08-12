package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.integration.IntegrationTest;
import com.nowakartur97.personalkanbanboardbackend.task.TaskEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.graphql.ResponseError;

import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.DELETE_SUBTASK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SubtaskDeletionMutationControllerTest extends IntegrationTest {

    private final static String DELETE_SUBTASK_PATH = "deleteSubtask";

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenDeleteExistingSubtask_shouldReturnEmptyResponse(UserRole role) {

        UserEntity userEntity = createUser(role);
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        SubtaskEntity subtaskEntity = createSubtask(taskEntity.getTaskId(), userEntity.getUserId());

        sendDeleteSubtaskRequest(userEntity, subtaskEntity.getSubtaskId());

        assertThat(subtaskRepository.count().block()).isZero();
        assertThat(taskRepository.count().block()).isOne();
    }

    @Test
    public void whenDeleteNotExistingSubtask_shouldReturnEmptyResponse() {

        UserEntity userEntity = createUser();

        sendDeleteSubtaskRequest(userEntity, UUID.randomUUID());

        assertThat(taskRepository.count().block()).isZero();
    }

    @Test
    public void whenDeleteSubtaskWithoutSubtaskId_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();

        httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(DELETE_SUBTASK)
                .execute()
                .errors()
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertValidationErrorResponse(responseError, new SourceLocation(1, 25),
                                    "Variable 'subtaskId' has an invalid value: Variable 'subtaskId' has coerced Null value for NonNull type 'UUID!'"
                            );
                        });
    }

    @Test
    public void whenDeleteSubtaskWithoutProvidingAuthorizationHeader_shouldReturnGraphQLErrorResponse() {

        runTestForSendingRequestWithoutProvidingAuthorizationHeader(DELETE_SUBTASK, DELETE_SUBTASK_PATH, "subtaskId", UUID.randomUUID());
    }

    @Test
    public void whenDeleteSubtaskWithExpiredToken_shouldReturnGraphQLErrorResponse() {

        runTestForSendingRequestWithExpiredToken(DELETE_SUBTASK, DELETE_SUBTASK_PATH, "subtaskId", UUID.randomUUID());
    }

    @Test
    public void whenDeleteSubtaskWithInvalidToken_shouldReturnGraphQLErrorResponse() {

        runTestForSendingRequestWithInvalidToken(DELETE_SUBTASK, DELETE_SUBTASK_PATH, "subtaskId", UUID.randomUUID());
    }

    @Test
    public void whenDeleteSubtaskWithDifferentTokenSignature_shouldReturnGraphQLErrorResponse() {

        runTestForSendingRequestWithDifferentTokenSignature(DELETE_SUBTASK, DELETE_SUBTASK_PATH, "subtaskId", UUID.randomUUID());
    }

    private void sendDeleteSubtaskRequest(UserEntity userEntity, UUID subtaskId) {
        httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(DELETE_SUBTASK)
                .variable("subtaskId", subtaskId)
                .execute()
                .errors()
                .verify()
                .path(DELETE_SUBTASK_PATH);
    }
}
