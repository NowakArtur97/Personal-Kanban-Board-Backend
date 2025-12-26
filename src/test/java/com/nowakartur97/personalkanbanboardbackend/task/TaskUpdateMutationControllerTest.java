package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.request.DoubleRequestVariable;
import com.nowakartur97.personalkanbanboardbackend.common.test.BaseTaskUpdateMutationControllerTest;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.Collections;
import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.common.test.GraphQLQueries.TASK_EVENT;
import static com.nowakartur97.personalkanbanboardbackend.common.test.GraphQLQueries.UPDATE_TASK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskUpdateMutationControllerTest extends BaseTaskUpdateMutationControllerTest<TaskEntity, TaskResponse> {

    public TaskUpdateMutationControllerTest() {
        super("updateTask", UPDATE_TASK,
                new DoubleRequestVariable("taskDTO", new TaskDTO("title", "description", null, null, null, null), "taskId", UUID.randomUUID()),
                38, TASK_EVENT, "taskEvent", TaskEvent.class,
                "Task", "taskId", 22);
    }

    @Override
    protected TaskEntity createTask(UserEntity userEntity) {
        return createTask(userEntity.getUserId());
    }

    @Override
    protected TaskResponse sendUpdateTaskRequest(UserEntity userEntity, TaskEntity task, TaskDTO taskDTO) {
        DoubleRequestVariable doubleRequestVariable = new DoubleRequestVariable(requestVariable.getName(), taskDTO, idFieldName, task.getTaskId());
        return (TaskResponse) sendRequest(userEntity, document, path, doubleRequestVariable, TaskResponse.class, false);
    }

    @Override
    protected GraphQlTester.Errors sendTaskRequestWithErrors(UserEntity userEntity, TaskDTO taskDTO) {
        UUID taskId = createTask(userEntity.getUserId()).getTaskId();
        DoubleRequestVariable doubleRequestVariable = new DoubleRequestVariable(requestVariable.getName(), taskDTO, idFieldName, taskId);
        return sendRequestWithErrors(userEntity, document, doubleRequestVariable);
    }

    @Override
    protected void assertTaskId(TaskEntity updatedTaskEntity, TaskEntity taskEntity) {
        assertThat(updatedTaskEntity.getTaskId()).isEqualTo(taskEntity.getTaskId());
    }

    @Override
    protected void assertTaskResponse(TaskResponse taskResponse, TaskEntity taskEntity, TaskDTO taskDTO, String createdBy,
                                      String updatedBy, String assignedTo) {
        assertBaseTaskResponse(taskResponse, taskEntity, taskDTO, createdBy, updatedBy, assignedTo);
        assertThat(taskResponse.getTaskId()).isEqualTo(taskEntity.getTaskId());
        assertTrue(taskResponse.getSubtasks().isEmpty());
    }

    @Override
    protected void assertTaskEventResponse(TaskResponse mutationTaskResponse, TaskResponse subscriptionTaskResponse) {
        assertBaseTaskResponse(mutationTaskResponse, subscriptionTaskResponse);
        assertTrue(subscriptionTaskResponse.getSubtasks().isEmpty());
    }

    @Override
    protected TaskResponse createExpectedSubscriptionResponse(TaskEntity taskEntity, TaskDTO taskDTO, String createdBy, String updatedBy, String assignedTo) {
        return new TaskResponse(
                taskEntity.getTaskId(),
                taskDTO.getTitle(),
                taskDTO.getDescription(),
                taskDTO.getStatus() != null ? taskDTO.getStatus() : TaskStatus.READY_TO_START,
                taskDTO.getPriority() != null ? taskDTO.getPriority() : TaskPriority.LOW,
                taskDTO.getTargetEndDate(),
                createdBy,
                taskEntity.getCreatedOn().toString(),
                updatedBy,
                taskEntity.getUpdatedOn().toString(),
                assignedTo,
                Collections.emptyList()
        );
    }
}
