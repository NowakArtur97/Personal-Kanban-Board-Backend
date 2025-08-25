package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskDeletionMutationControllerTest;
import com.nowakartur97.personalkanbanboardbackend.common.RequestVariable;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.DELETE_TASK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TaskDeletionMutationControllerTest extends BaseTaskDeletionMutationControllerTest {

    private final static String DELETE_TASK_PATH = "deleteTask";

    public TaskDeletionMutationControllerTest() {
        super(DELETE_TASK_PATH, DELETE_TASK, new RequestVariable("taskId", UUID.randomUUID()), 22);
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

    @Override
    protected void sendDeleteTaskRequest(UserEntity userEntity, UUID taskId) {
        RequestVariable reqVariable = new RequestVariable("taskId", taskId);
        sendRequest(userEntity, document, path, reqVariable, null, false);
    }
}
