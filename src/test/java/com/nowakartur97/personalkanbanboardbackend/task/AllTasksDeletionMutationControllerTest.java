package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.TaskIntegrationTest;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import org.junit.jupiter.api.Test;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.DELETE_ALL_TASKS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AllTasksDeletionMutationControllerTest extends TaskIntegrationTest {

    private final static String DELETE_ALL_TASKS_PATH = "deleteAllTasks";

    public AllTasksDeletionMutationControllerTest() {
        super(DELETE_ALL_TASKS_PATH, DELETE_ALL_TASKS, null);
    }

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

        UserEntity admin = createUser("testAdmin", "testAdmin@domain.com", UserRole.ADMIN);

        sendDeleteAllTasksRequest(admin);

        assertThat(taskRepository.count().block()).isZero();
    }

    @Test
    public void whenDeleteAllTasksByNonAdminUser_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();

        asserForbiddenErrorResponse(sendRequestWithErrors(userEntity, document, null), DELETE_ALL_TASKS_PATH);
    }

    private void sendDeleteAllTasksRequest(UserEntity userEntity) {
        sendRequest(userEntity, document, path, null, null, false);
    }
}
