package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.TaskEvent;
import com.nowakartur97.personalkanbanboardbackend.common.request.DoubleRequestVariable;
import com.nowakartur97.personalkanbanboardbackend.common.test.BaseUserAssignedToTaskUpdateMutationControllerTest;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.Collections;
import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.common.test.GraphQLQueries.TASK_EVENT;
import static com.nowakartur97.personalkanbanboardbackend.common.test.GraphQLQueries.UPDATE_USER_ASSIGNED_TO_TASK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserAssignedToTaskUpdateMutationControllerTest extends BaseUserAssignedToTaskUpdateMutationControllerTest<TaskEntity, TaskResponse> {

    public UserAssignedToTaskUpdateMutationControllerTest() {
        super("updateUserAssignedToTask", UPDATE_USER_ASSIGNED_TO_TASK,
                new DoubleRequestVariable("taskId", UUID.randomUUID(), "assignedToId", UUID.randomUUID()),
                TASK_EVENT, "taskEvent", TaskEvent.class,
                "Task", "taskId", 39, 55);
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

    @Override
    protected void assertTaskEventResponse(TaskResponse mutationTaskResponse, TaskResponse subscriptionTaskResponse) {
        assertBaseTaskResponse(mutationTaskResponse, subscriptionTaskResponse);
        assertTrue(subscriptionTaskResponse.getSubtasks().isEmpty());
    }

    @Override
    protected TaskResponse createExpectedSubscriptionResponse(TaskEntity taskEntity, String createdBy, String updatedBy, String assignedTo) {
        return new TaskResponse(
                taskEntity.getTaskId(),
                taskEntity.getTitle(),
                taskEntity.getDescription(),
                taskEntity.getStatus(),
                taskEntity.getPriority(),
                taskEntity.getTargetEndDate(),
                createdBy,
                taskEntity.getCreatedOn().toString(),
                updatedBy,
                taskEntity.getUpdatedOn().toString(),
                assignedTo,
                Collections.emptyList()
        );
    }
}
