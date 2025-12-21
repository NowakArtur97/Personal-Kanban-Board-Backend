package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.common.request.DoubleRequestVariable;
import com.nowakartur97.personalkanbanboardbackend.common.request.RequestVariable;
import com.nowakartur97.personalkanbanboardbackend.common.test.BaseTaskCreationMutationControllerTest;
import com.nowakartur97.personalkanbanboardbackend.task.TaskDTO;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.common.test.GraphQLQueries.CREATE_SUBTASK;
import static com.nowakartur97.personalkanbanboardbackend.common.test.GraphQLQueries.SUBTASK_EVENT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SubtaskCreationMutationControllerTest extends BaseTaskCreationMutationControllerTest<SubtaskEntity, SubtaskResponse> {

    private UUID taskId;

    public SubtaskCreationMutationControllerTest() {
        super("createSubtask", CREATE_SUBTASK,
                new DoubleRequestVariable("subtaskDTO", new TaskDTO("title", "description", null, null, null, null),
                        "taskId", UUID.randomUUID()), 41,
                SUBTASK_EVENT, "subtaskEvent", SubtaskEvent.class);
    }

    @Test
    public void whenCreateSubtaskForNotExistingTask_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        UUID taskId = UUID.randomUUID();
        TaskDTO subtaskDTO = new TaskDTO("title", "description", null, null, null, UUID.randomUUID());
        DoubleRequestVariable doubleRequestVariable = new DoubleRequestVariable("taskId", taskId, "subtaskDTO", subtaskDTO);

        assertNotFoundErrorResponse(sendRequestWithErrors(userEntity, document, doubleRequestVariable), path, "Task with taskId: '" + taskId + "' not found.");
    }

    @Test
    public void whenCreateSubtaskWithoutTaskId_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO subtaskDTO = new TaskDTO("title", "description", null, null, null, null);
        RequestVariable reqVariable = new RequestVariable("subtaskDTO", subtaskDTO);

        assertValidationErrorResponse(sendRequestWithErrors(userEntity, document, reqVariable), new SourceLocation(1, 25),
                "Variable 'taskId' has an invalid value: Variable 'taskId' has coerced Null value for NonNull type 'UUID!'");
    }

    @Override
    protected SubtaskResponse sendCreateTaskRequest(UserEntity userEntity, TaskDTO subtaskDTO) {
        taskId = createTask(userEntity.getUserId()).getTaskId();
        DoubleRequestVariable doubleRequestVariable = new DoubleRequestVariable("taskId", taskId, "subtaskDTO", subtaskDTO);
        return (SubtaskResponse) sendRequest(userEntity, document, path, doubleRequestVariable, SubtaskResponse.class, false);
    }

    @Override
    protected GraphQlTester.Errors sendTaskRequestWithErrors(UserEntity userEntity, TaskDTO subTaskDTO) {
        UUID taskId = createTask(userEntity.getUserId()).getTaskId();
        DoubleRequestVariable doubleRequestVariable = new DoubleRequestVariable(requestVariable.getName(), subTaskDTO, "taskId", taskId);
        return sendRequestWithErrors(userEntity, document, doubleRequestVariable);
    }

    @Override
    protected void assertTaskEntity(SubtaskEntity subtaskEntity, TaskDTO subTaskDTO, UUID createdBy, UUID assignedTo) {
        assertBaseTaskEntity(subtaskEntity, subTaskDTO, createdBy, assignedTo);
        assertThat(subtaskEntity.getSubtaskId()).isNotNull();
        assertThat(subtaskEntity.getTaskId()).isEqualTo(taskId);
    }

    @Override
    protected void assertTaskResponse(SubtaskResponse subtaskResponse, TaskDTO subTaskDTO, String createdBy, String assignedTo) {
        assertBaseTaskResponse(subtaskResponse, subTaskDTO, createdBy, assignedTo);
        assertThat(subtaskResponse.getSubtaskId()).isNotNull();
        assertThat(subtaskResponse.getTaskId()).isEqualTo(taskId);
    }

    @Override
    protected void assertTaskEventResponse(SubtaskResponse mutationSubtaskResponse, SubtaskResponse subscriptionSubtaskResponse) {
        assertBaseTaskResponse(mutationSubtaskResponse, subscriptionSubtaskResponse);
        assertThat(subscriptionSubtaskResponse.getSubtaskId()).isEqualTo(mutationSubtaskResponse.getSubtaskId());
    }

    @Override
    protected SubtaskResponse createExpectedSubscriptionResponse(SubtaskEntity subtaskEntity, String createdBy, String assignedTo) {
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
                null,
                subtaskEntity.getUpdatedOn() != null ? subtaskEntity.getUpdatedOn().toString() : null,
                assignedTo
        );
    }
}
