package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskCreationMutationControllerTest;
import com.nowakartur97.personalkanbanboardbackend.common.DoubleRequestVariable;
import com.nowakartur97.personalkanbanboardbackend.common.RequestVariable;
import com.nowakartur97.personalkanbanboardbackend.task.TaskDTO;
import com.nowakartur97.personalkanbanboardbackend.task.TaskPriority;
import com.nowakartur97.personalkanbanboardbackend.task.TaskStatus;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.CREATE_SUBTASK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SubtaskCreationMutationControllerTest extends BaseTaskCreationMutationControllerTest<SubtaskEntity, SubtaskResponse> {

    private final static String CREATE_SUBTASK_PATH = "createSubtask";
    private UUID taskId;

    public SubtaskCreationMutationControllerTest() {
        super(CREATE_SUBTASK_PATH, CREATE_SUBTASK,
                new DoubleRequestVariable("subtaskDTO", new TaskDTO("title", "description", null, null, null, null),
                        "taskId", UUID.randomUUID()), 41);
    }

    @BeforeEach
    public void setRepository() {
        setRepository(subtaskRepository);
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
    protected void assertTaskResponse(SubtaskResponse subtaskResponse, TaskDTO subTaskDTO,
                                      String createdBy, String assignedTo,
                                      TaskStatus status, TaskPriority priority) {
        assertBaseTaskResponse(subtaskResponse, subTaskDTO, createdBy, assignedTo, status, priority);
        assertThat(subtaskResponse.getSubtaskId()).isNotNull();
        assertThat(subtaskResponse.getTaskId()).isEqualTo(taskId);
    }

    @Override
    protected void assertTaskEntity(SubtaskEntity subtaskEntity, TaskDTO subTaskDTO, UUID createdBy, UUID assignedTo,
                                    TaskStatus taskStatus, TaskPriority taskPriority) {
        assertBaseTaskEntity(subtaskEntity, subTaskDTO, createdBy, assignedTo, taskStatus, taskPriority);
        assertThat(subtaskEntity.getSubtaskId()).isNotNull();
        assertThat(subtaskEntity.getTaskId()).isEqualTo(taskId);
    }

    @Override
    protected GraphQlTester.Errors sendTaskRequestWithErrors(UserEntity userEntity, TaskDTO subTaskDTO) {
        UUID taskId = createTask(userEntity.getUserId()).getTaskId();
        DoubleRequestVariable doubleRequestVariable = new DoubleRequestVariable(requestVariable.getName(), subTaskDTO, "taskId", taskId);
        return sendRequestWithErrors(userEntity, document, doubleRequestVariable);
    }
}
