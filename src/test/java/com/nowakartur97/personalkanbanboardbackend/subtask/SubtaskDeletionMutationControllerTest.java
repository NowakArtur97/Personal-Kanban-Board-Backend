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
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.DELETE_SUBTASK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SubtaskDeletionMutationControllerTest extends TaskIntegrationTest {

    private final static String DELETE_SUBTASK_PATH = "deleteSubtask";

    public SubtaskDeletionMutationControllerTest() {
        super(DELETE_SUBTASK_PATH, DELETE_SUBTASK,
                new RequestVariable("subtaskId", UUID.randomUUID()));
    }

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

        GraphQlTester.Errors errors = httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(DELETE_SUBTASK)
                .execute()
                .errors();
        assertValidationErrorResponse(errors, new SourceLocation(1, 25),
                "Variable 'subtaskId' has an invalid value: Variable 'subtaskId' has coerced Null value for NonNull type 'UUID!'");
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
