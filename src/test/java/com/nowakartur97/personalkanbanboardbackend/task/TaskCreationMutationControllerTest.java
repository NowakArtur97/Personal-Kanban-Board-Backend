package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskCreationMutationControllerTest;
import com.nowakartur97.personalkanbanboardbackend.common.RequestVariable;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.CREATE_TASK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TaskCreationMutationControllerTest extends BaseTaskCreationMutationControllerTest<TaskEntity, TaskResponse> {

    private final static String CREATE_TASK_PATH = "createTask";

    public TaskCreationMutationControllerTest() {
        super(CREATE_TASK_PATH, CREATE_TASK,
                new RequestVariable("taskDTO", new TaskDTO("title", "description", null, null, null, null)),
                22);
    }

    @BeforeEach
    public void setRepository() {
        setRepository(taskRepository);
    }

    @Override
    protected TaskResponse sendCreateTaskRequest(UserEntity userEntity, TaskDTO taskDTO) {
        RequestVariable reqVariable = new RequestVariable("taskDTO", taskDTO);
        return (TaskResponse) sendRequest(userEntity, document, path, reqVariable, TaskResponse.class, false);
    }

    @Override
    protected void assertTaskResponse(TaskResponse taskResponse, TaskDTO taskDTO, String createdBy, String assignedTo,
                                      TaskStatus status, TaskPriority priority) {
        assertBaseTaskResponse(taskResponse, taskDTO, createdBy, assignedTo, status, priority);
        assertThat(taskResponse.getTaskId()).isNotNull();
        assertThat(taskResponse.getSubtasks()).isNull();
    }

    @Override
    protected void assertTaskEntity(TaskEntity taskEntity, TaskDTO taskDTO, UUID createdBy, UUID assignedTo,
                                    TaskStatus taskStatus, TaskPriority taskPriority) {
        assertBaseTaskEntity(taskEntity, taskDTO, createdBy, assignedTo, taskStatus, taskPriority);
        assertThat(taskEntity.getTaskId()).isNotNull();
    }

    @Override
    protected GraphQlTester.Errors sendTaskRequestWithErrors(UserEntity userEntity, TaskDTO taskDTO) {
        RequestVariable reqVariable = new RequestVariable(requestVariable.getName(), taskDTO);
        return sendRequestWithErrors(userEntity, document, reqVariable);
    }
}
