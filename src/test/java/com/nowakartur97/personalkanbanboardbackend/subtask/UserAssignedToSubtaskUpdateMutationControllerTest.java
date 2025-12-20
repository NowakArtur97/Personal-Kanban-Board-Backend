package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.common.BaseUserAssignedToTaskUpdateMutationControllerTest;
import com.nowakartur97.personalkanbanboardbackend.common.DoubleRequestVariable;
import com.nowakartur97.personalkanbanboardbackend.common.SubtaskEvent;
import com.nowakartur97.personalkanbanboardbackend.task.TaskEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.SUBTASK_EVENT;
import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.UPDATE_USER_ASSIGNED_TO_SUBTASK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UserAssignedToSubtaskUpdateMutationControllerTest extends BaseUserAssignedToTaskUpdateMutationControllerTest<SubtaskEntity, SubtaskResponse> {

    public UserAssignedToSubtaskUpdateMutationControllerTest() {
        super("updateUserAssignedToSubtask", UPDATE_USER_ASSIGNED_TO_SUBTASK,
                new DoubleRequestVariable("subtaskId", UUID.randomUUID(), "assignedToId", UUID.randomUUID()),
                SUBTASK_EVENT, "subtaskEvent", SubtaskEvent.class,
                "Subtask", "subtaskId", 42, 61);
    }

    @Override
    protected SubtaskEntity createTask(UserEntity userEntity) {
        TaskEntity taskEntity = createTask(userEntity.getUserId(), userEntity.getUserId(), userEntity.getUserId());
        return createSubtask(taskEntity.getTaskId(), userEntity.getUserId(), userEntity.getUserId(), userEntity.getUserId());
    }

    @Override
    protected SubtaskResponse sendUpdateUserAssignedToTaskRequest(UserEntity userEntity, SubtaskEntity subtaskEntity, UUID assignedToId) {
        DoubleRequestVariable doubleRequestVariable = new DoubleRequestVariable("subtaskId", subtaskEntity.getSubtaskId(), "assignedToId", assignedToId);
        return (SubtaskResponse) sendRequest(userEntity, document, path, doubleRequestVariable, SubtaskResponse.class, false);
    }

    @Override
    protected GraphQlTester.Errors sendUpdateUserAssignedToTaskRequestWithErrors(UserEntity userEntity, SubtaskEntity subtask, UUID assignedToId) {
        return sendUpdateUserAssignedToTaskRequestWithErrors(userEntity, subtask.getSubtaskId(), assignedToId);
    }

    @Override
    protected void assertTaskEntity(SubtaskEntity subtaskEntity, SubtaskEntity subtaskEntityAfterUpdate, UUID assignedTo) {
        assertBaseTaskEntity(subtaskEntity, subtaskEntityAfterUpdate, assignedTo);
        assertThat(subtaskEntityAfterUpdate.getSubtaskId()).isEqualTo(subtaskEntity.getSubtaskId());
        assertThat(subtaskEntityAfterUpdate.getTaskId()).isEqualTo(subtaskEntity.getTaskId());
    }

    @Override
    protected void assertTaskResponse(SubtaskResponse subtaskResponse, SubtaskEntity updatedSubtaskEntity, UserEntity assignedTo, UserEntity userEntity) {
        assertSubtaskResponse(subtaskResponse, updatedSubtaskEntity, assignedTo.getUsername(), userEntity.getUsername(), userEntity.getUsername());
    }

    @Override
    protected void assertTaskEventResponse(SubtaskResponse mutationSubtaskResponse, SubtaskResponse subscriptionSubtaskResponse) {
        assertBaseTaskResponse(mutationSubtaskResponse, subscriptionSubtaskResponse);
        assertThat(subscriptionSubtaskResponse.getSubtaskId()).isEqualTo(mutationSubtaskResponse.getSubtaskId());
    }

    @Override
    protected SubtaskResponse createExpectedSubscriptionResponse(SubtaskEntity subtaskEntity, String createdBy, String updatedBy, String assignedTo) {
        return new SubtaskResponse(
                subtaskEntity.getSubtaskId(),
                subtaskEntity.getTaskId(),
                subtaskEntity.getTitle(),
                subtaskEntity.getDescription(),
                subtaskEntity.getStatus(),
                subtaskEntity.getPriority(),
                subtaskEntity.getTargetEndDate(),
                createdBy,
                subtaskEntity.getCreatedOn().toString(),
                updatedBy,
                subtaskEntity.getUpdatedOn().toString(),
                assignedTo
        );
    }
}
