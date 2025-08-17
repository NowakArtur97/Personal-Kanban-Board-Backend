package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.RequestVariable;
import com.nowakartur97.personalkanbanboardbackend.common.TaskMutationTest;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;
import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.CREATE_TASK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TaskCreationMutationControllerTest extends TaskMutationTest {

    private final static String CREATE_TASK_PATH = "createTask";

    public TaskCreationMutationControllerTest() {
        super(CREATE_TASK_PATH, CREATE_TASK,
                new RequestVariable("taskDTO", new TaskDTO("title", "description", null, null, null, null)),
                "taskDTO", 22);
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenCreateTask_shouldReturnTaskResponse(UserRole role) {

        UserEntity userEntity = createUser(role);
        UserEntity assignedTo = createUser("developer", "developer@domain.com");
        TaskDTO taskDTO = new TaskDTO("title", "description", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, LocalDate.now(), assignedTo.getUserId());

        TaskResponse taskResponse = sendCreateTaskRequest(userEntity, taskDTO);

        assertTaskEntity(taskRepository.findAll().blockLast(), taskDTO, userEntity.getUserId(), assignedTo.getUserId());
        assertTaskResponse(taskResponse, taskDTO, userEntity.getUsername(), assignedTo.getUsername(),
                taskDTO.getStatus(), taskDTO.getPriority());
    }

    @Test
    public void whenCreateTask_shouldCreateTaskWithDefaultValuesAndReturnTaskResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, null);

        TaskResponse taskResponse = sendCreateTaskRequest(userEntity, taskDTO);

        assertTaskEntity(taskRepository.findAll().blockLast(), taskDTO, userEntity.getUserId());
        assertTaskResponse(taskResponse, taskDTO, userEntity.getUsername());
    }

    private TaskResponse sendCreateTaskRequest(UserEntity userEntity, TaskDTO taskDTO) {
        return httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(CREATE_TASK)
                .variable("taskDTO", taskDTO)
                .execute()
                .errors()
                .verify()
                .path(CREATE_TASK_PATH)
                .entity(TaskResponse.class)
                .get();
    }

    private void assertTaskResponse(TaskResponse taskResponse, TaskDTO taskDTO, String createdBy) {
        assertTaskResponse(taskResponse, taskDTO, createdBy, createdBy, TaskStatus.READY_TO_START, TaskPriority.LOW);
    }

    private void assertTaskResponse(TaskResponse taskResponse, TaskDTO taskDTO, String createdBy, String assignedTo,
                                    TaskStatus status, TaskPriority priority) {
        assertBaseTaskResponse(taskResponse, taskDTO, createdBy, assignedTo, status, priority);
        assertThat(taskResponse.getTaskId()).isNotNull();
        assertThat(taskResponse.getSubtasks()).isNull();
    }

    private void assertTaskEntity(TaskEntity taskEntity, TaskDTO taskDTO, UUID createdBy) {
        assertTaskEntity(taskEntity, taskDTO, createdBy, createdBy, TaskStatus.READY_TO_START, TaskPriority.LOW);
    }

    private void assertTaskEntity(TaskEntity taskEntity, TaskDTO taskDTO, UUID createdBy, UUID assignedTo) {
        assertTaskEntity(taskEntity, taskDTO, createdBy, assignedTo, taskDTO.getStatus(), taskDTO.getPriority());
    }

    private void assertTaskEntity(TaskEntity taskEntity, TaskDTO taskDTO, UUID createdBy, UUID assignedTo,
                                  TaskStatus taskStatus, TaskPriority taskPriority) {
        assertBaseTaskEntity(taskEntity, taskDTO, createdBy, assignedTo, taskStatus, taskPriority);
        assertThat(taskEntity.getTaskId()).isNotNull();
    }
}
