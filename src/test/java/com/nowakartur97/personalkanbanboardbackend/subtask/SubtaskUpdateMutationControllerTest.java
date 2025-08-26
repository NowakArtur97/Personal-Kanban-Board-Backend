package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskUpdateMutationControllerTest;
import com.nowakartur97.personalkanbanboardbackend.common.DoubleRequestVariable;
import com.nowakartur97.personalkanbanboardbackend.common.RequestVariable;
import com.nowakartur97.personalkanbanboardbackend.task.TaskDTO;
import com.nowakartur97.personalkanbanboardbackend.task.TaskEntity;
import com.nowakartur97.personalkanbanboardbackend.task.TaskPriority;
import com.nowakartur97.personalkanbanboardbackend.task.TaskStatus;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.UPDATE_SUBTASK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SubtaskUpdateMutationControllerTest extends BaseTaskUpdateMutationControllerTest<SubtaskEntity, SubtaskResponse> {

    private final static String UPDATE_SUBTASK_PATH = "updateSubtask";

    public SubtaskUpdateMutationControllerTest() {
        super(UPDATE_SUBTASK_PATH, UPDATE_SUBTASK,
                new DoubleRequestVariable("subtaskDTO", new TaskDTO("title", "description", null, null, null, null), "subtaskId", UUID.randomUUID()),
                44, "Subtask", "subtaskId", 25);
    }

    @BeforeEach
    public void setRepository() {
        setRepository(subtaskRepository);
    }

    @Override
    protected SubtaskEntity createTask(UserEntity userEntity) {
        TaskEntity taskEntity = createTask(userEntity.getUserId(), userEntity.getUserId(), userEntity.getUserId());
        return createSubtask(taskEntity.getTaskId(), userEntity.getUserId(), userEntity.getUserId(), userEntity.getUserId());
    }

    @Override
    protected SubtaskResponse sendUpdateTaskRequest(UserEntity userEntity, SubtaskEntity subtaskEntity, TaskDTO subtaskDTO) {
        RequestVariable doubleRequestVariable = new DoubleRequestVariable(requestVariable.getName(), subtaskDTO, idFieldName, subtaskEntity.getSubtaskId());
        return (SubtaskResponse) sendRequest(userEntity, document, path, doubleRequestVariable, SubtaskResponse.class, false);
    }

    @Override
    protected GraphQlTester.Errors sendTaskRequestWithErrors(UserEntity userEntity, TaskDTO subtaskDTO) {
        UUID taskId = createTask(userEntity.getUserId()).getTaskId();
        UUID subtaskId = createSubtask(taskId, userEntity.getUserId()).getSubtaskId();
        RequestVariable doubleRequestVariable = new DoubleRequestVariable(requestVariable.getName(), subtaskDTO, idFieldName, subtaskId);
        return sendRequestWithErrors(userEntity, document, doubleRequestVariable);
    }

    @Override
    protected void assertTaskId(SubtaskEntity updatedSubtaskEntity, SubtaskEntity subtaskEntity) {
        assertThat(updatedSubtaskEntity.getSubtaskId()).isEqualTo(subtaskEntity.getSubtaskId());
        assertThat(updatedSubtaskEntity.getTaskId()).isEqualTo(subtaskEntity.getTaskId());
    }

    @Override
    protected void assertTaskResponse(SubtaskResponse subtaskResponse, SubtaskEntity subtaskEntity, TaskDTO subtaskDTO, String createdBy,
                                      String updatedBy, String assignedTo, TaskStatus status, TaskPriority priority) {
        assertBaseTaskResponse(subtaskResponse, subtaskEntity, subtaskDTO, createdBy, updatedBy, assignedTo, status, priority);
        assertThat(subtaskResponse.getSubtaskId()).isEqualTo(subtaskEntity.getSubtaskId());
        assertThat(subtaskResponse.getTaskId()).isEqualTo(subtaskEntity.getTaskId());
    }
}
