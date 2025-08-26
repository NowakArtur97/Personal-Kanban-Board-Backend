package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskUpdateMutationControllerTest;
import com.nowakartur97.personalkanbanboardbackend.common.DoubleRequestVariable;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.UPDATE_TASK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TaskUpdateMutationControllerTest extends BaseTaskUpdateMutationControllerTest<TaskEntity, TaskResponse> {

    private final static String UPDATE_TASK_PATH = "updateTask";

    public TaskUpdateMutationControllerTest() {
        super(UPDATE_TASK_PATH, UPDATE_TASK,
                new DoubleRequestVariable("taskDTO", new TaskDTO("title", "description", null, null, null, null), "taskId", UUID.randomUUID()),
                38, "Task", "taskId", 22);
    }

    @BeforeEach
    public void setRepository() {
        setRepository(taskRepository);
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
                                      String updatedBy, String assignedTo, TaskStatus status, TaskPriority priority) {
        assertBaseTaskResponse(taskResponse, taskEntity, taskDTO, createdBy, updatedBy, assignedTo, status, priority);
        assertThat(taskResponse.getTaskId()).isEqualTo(taskEntity.getTaskId());
        assertThat(taskResponse.getSubtasks()).isNull();
    }
}
