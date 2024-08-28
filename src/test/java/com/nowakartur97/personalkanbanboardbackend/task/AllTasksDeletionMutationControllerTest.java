package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.integration.IntegrationTest;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.ResponseError;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.DELETE_ALL_TASKS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AllTasksDeletionMutationControllerTest extends IntegrationTest {

    private final static String DELETE_ALL_TASKS_PATH = "deleteAllTasks";

    @Test
    public void whenDeleteAllTasksByAdmin_shouldReturnEmptyResponse() {

        UserEntity admin = createUser("admin", "admin@domain.com", UserRole.ADMIN);
        createTask(admin.getUserId());
        UserEntity userEntity = createUser("developer", "developer@domain.com");
        createTask(userEntity.getUserId());

        sendDeleteAllTasksRequest(admin);

        assertThat(taskRepository.count().block()).isZero();
    }

    @Test
    public void whenDeleteAllTasksWhenThereAreNoExistingTasks_shouldReturnEmptyResponse() {

        UserEntity admin = createUser("admin", "admin@domain.com", UserRole.ADMIN);

        sendDeleteAllTasksRequest(admin);

        assertThat(taskRepository.count().block()).isZero();
    }

    @Test
    public void whenDeleteAllTasksByUser_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();

        httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(DELETE_ALL_TASKS)
                .execute()
                .errors()
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            asserForbiddenErrorResponse(responseError, DELETE_ALL_TASKS_PATH, "Forbidden");
                        });
    }

    @Test
    public void whenDeleteAllTasksWithoutProvidingAuthorizationHeader_shouldReturnGraphQLErrorResponse() {

        runTestForSendingRequestWithoutProvidingAuthorizationHeader(DELETE_ALL_TASKS, DELETE_ALL_TASKS_PATH);
    }

    @Test
    public void whenDeleteAllTasksWithExpiredToken_shouldReturnGraphQLErrorResponse() {

        runTestForSendingRequestWithExpiredToken(DELETE_ALL_TASKS, DELETE_ALL_TASKS_PATH);
    }

    @Test
    public void whenDeleteAllTasksWithInvalidToken_shouldReturnGraphQLErrorResponse() {

        runTestForSendingRequestWithInvalidToken(DELETE_ALL_TASKS, DELETE_ALL_TASKS_PATH);
    }

    @Test
    public void whenDeleteAllTasksWithDifferentTokenSignature_shouldReturnGraphQLErrorResponse() {

        runTestForSendingRequestWithDifferentTokenSignature(DELETE_ALL_TASKS, DELETE_ALL_TASKS_PATH);
    }

    private void sendDeleteAllTasksRequest(UserEntity userEntity) {
        httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(DELETE_ALL_TASKS)
                .execute()
                .errors()
                .verify()
                .path(DELETE_ALL_TASKS_PATH);
    }
}
