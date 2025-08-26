package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.BaseUserAssignedToTaskUpdateMutationControllerTest;
import com.nowakartur97.personalkanbanboardbackend.common.DoubleRequestVariable;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.UPDATE_USER_ASSIGNED_TO_TASK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UserAssignedToTaskUpdateMutationControllerTest extends BaseUserAssignedToTaskUpdateMutationControllerTest<TaskEntity, TaskResponse> {

    private final static String UPDATE_USER_ASSIGNED_TO_TASK_PATH = "updateUserAssignedToTask";

    public UserAssignedToTaskUpdateMutationControllerTest() {
        super(UPDATE_USER_ASSIGNED_TO_TASK_PATH, UPDATE_USER_ASSIGNED_TO_TASK,
                new DoubleRequestVariable("taskId", UUID.randomUUID(), "assignedToId", UUID.randomUUID()),
                "Task", "taskId", 39, 55);
    }

    @BeforeEach
    public void setRepository() {
        setRepository(taskRepository);
    }

    @Override
    protected TaskEntity createTask(UserEntity userEntity) {
        return createTask(userEntity.getUserId(), userEntity.getUserId(), userEntity.getUserId());
    }

    @Override
    protected TaskResponse sendUpdateUserAssignedToTaskRequest(UserEntity userEntity, TaskEntity taskEntity, UUID assignedToId) {
        DoubleRequestVariable doubleRequestVariable = new DoubleRequestVariable("taskId", taskEntity.getTaskId(), "assignedToId", assignedToId);
        return (TaskResponse) sendRequest(userEntity, document, path, doubleRequestVariable, TaskResponse.class, false);
    }

    @Override
    protected GraphQlTester.Errors sendUpdateUserAssignedToTaskRequestWithErrors(UserEntity userEntity, TaskEntity task, UUID assignedToId) {
        return sendUpdateUserAssignedToTaskRequestWithErrors(userEntity, task.getTaskId(), assignedToId);
    }

    @Override
    protected void assertTaskEntity(TaskEntity taskEntity, TaskEntity taskEntityAfterUpdate, UUID assignedTo) {
        assertBaseTaskEntity(taskEntity, taskEntityAfterUpdate, assignedTo);
        assertThat(taskEntityAfterUpdate.getTaskId()).isEqualTo(taskEntity.getTaskId());
    }

    @Override
    protected void assertTaskResponse(TaskResponse taskResponse, TaskEntity updatedTaskEntity, UserEntity assignedTo, UserEntity userEntity) {
        assertTaskResponse(taskResponse, updatedTaskEntity, assignedTo.getUsername(), userEntity.getUsername(), userEntity.getUsername());
    }
}
