package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskCreationMutationControllerTest;
import com.nowakartur97.personalkanbanboardbackend.common.RequestVariable;
import com.nowakartur97.personalkanbanboardbackend.common.TaskEvent;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.Collections;
import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.CREATE_TASK;
import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.TASK_EVENT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskCreationMutationControllerTest extends BaseTaskCreationMutationControllerTest<TaskEntity, TaskResponse> {

    public TaskCreationMutationControllerTest() {
        super("createTask", CREATE_TASK,
                new RequestVariable("taskDTO", new TaskDTO("title", "description", null, null, null, null)),
                22, TASK_EVENT, "taskEvent", TaskEvent.class);
    }

    @Override
    protected TaskResponse sendCreateTaskRequest(UserEntity userEntity, TaskDTO taskDTO) {
        RequestVariable reqVariable = new RequestVariable("taskDTO", taskDTO);
        return (TaskResponse) sendRequest(userEntity, document, path, reqVariable, TaskResponse.class, false);
    }

    @Override
    protected GraphQlTester.Errors sendTaskRequestWithErrors(UserEntity userEntity, TaskDTO taskDTO) {
        RequestVariable reqVariable = new RequestVariable(requestVariable.getName(), taskDTO);
        return sendRequestWithErrors(userEntity, document, reqVariable);
    }

    @Override
    protected void assertTaskEntity(TaskEntity taskEntity, TaskDTO taskDTO, UUID createdBy, UUID assignedTo) {
        assertBaseTaskEntity(taskEntity, taskDTO, createdBy, assignedTo);
        assertThat(taskEntity.getTaskId()).isNotNull();
    }

    @Override
    protected void assertTaskResponse(TaskResponse taskResponse, TaskDTO taskDTO, String createdBy, String assignedTo) {
        assertBaseTaskResponse(taskResponse, taskDTO, createdBy, assignedTo);
        assertThat(taskResponse.getTaskId()).isNotNull();
        assertThat(taskResponse.getSubtasks()).isNull();
    }

    @Override
    protected void assertTaskEventResponse(TaskResponse mutationTaskResponse, TaskResponse subscriptionTaskResponse) {
        assertBaseTaskResponse(mutationTaskResponse, subscriptionTaskResponse);
        assertTrue(subscriptionTaskResponse.getSubtasks().isEmpty());
    }

    @Override
    protected TaskResponse createExpectedSubscriptionResponse(TaskEntity taskEntity, String createdBy, String assignedTo) {
        return new TaskResponse(
                taskEntity.getTaskId(),
                taskEntity.getTitle(),
                taskEntity.getDescription(),
                taskEntity.getStatus(),
                taskEntity.getPriority(),
                taskEntity.getTargetEndDate(),
                createdBy,
                taskEntity.getCreatedOn().toString(),
                null,
                taskEntity.getUpdatedOn() != null ? taskEntity.getUpdatedOn().toString() : null,
                assignedTo,
                Collections.emptyList()
        );
    }
}
