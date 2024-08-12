package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.integration.IntegrationTest;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.CREATE_TASK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TaskCreationMutationControllerTest extends IntegrationTest {

    private final static String CREATE_TASK_PATH = "createTask";

    @Test
    public void whenCreateTask_shouldReturnTaskResponse() {

        UserEntity userEntity = createUser();
        UserEntity taskAssignedToUserEntity = createUser("developer", "developer@domain.com");
        TaskDTO taskDTO = new TaskDTO("title", "description", TaskPriority.MEDIUM, LocalDate.of(2024, 8, 12), taskAssignedToUserEntity.getUserId());

        TaskResponse taskResponse = makeCreateTaskRequest(userEntity, taskDTO);

        assertTaskEntity(taskRepository.findAll().blockLast(), taskDTO, userEntity.getUserId(),
                taskAssignedToUserEntity.getUserId(), TaskPriority.MEDIUM);
        assertTaskResponse(taskResponse, taskDTO, userEntity.getUsername(), taskAssignedToUserEntity.getUsername(), taskDTO.getPriority());
    }

    @Test
    public void whenCreateTask_shouldCreateTaskWithDefaultValuesAndReturnTaskResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null);

        TaskResponse taskResponse = makeCreateTaskRequest(userEntity, taskDTO);

        assertTaskEntity(taskRepository.findAll().blockLast(), taskDTO, userEntity.getUserId());
        assertTaskResponse(taskResponse, taskDTO, userEntity.getUsername());
    }

    private TaskResponse makeCreateTaskRequest(UserEntity userEntity, TaskDTO taskDTO) {
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
        assertTaskResponse(taskResponse, taskDTO, createdBy, createdBy, TaskPriority.LOW);
    }

    private void assertTaskResponse(TaskResponse taskResponse, TaskDTO taskDTO, String createdBy, String assignedTo, TaskPriority priority) {
        assertThat(taskResponse).isNotNull();
        assertThat(taskResponse.taskId()).isNotNull();
        assertThat(taskResponse.title()).isEqualTo(taskDTO.getTitle());
        assertThat(taskResponse.status()).isEqualTo(TaskStatus.READY_TO_START);
        assertThat(taskResponse.priority()).isEqualTo(priority);
        assertThat(taskResponse.targetEndDate()).isEqualTo(taskDTO.getTargetEndDate());
        assertThat(taskResponse.assignedTo()).isEqualTo(assignedTo);
        assertThat(taskResponse.createdOn()).isNotNull();
        assertThat(taskResponse.createdBy()).isEqualTo(createdBy);
        assertThat(taskResponse.updatedOn()).isNull();
        assertThat(taskResponse.updatedBy()).isNull();
    }

    private void assertTaskEntity(TaskEntity taskEntity, TaskDTO taskDTO, UUID createdBy) {
        assertTaskEntity(taskEntity, taskDTO, createdBy, createdBy, TaskPriority.LOW);
    }

    private void assertTaskEntity(TaskEntity taskEntity, TaskDTO taskDTO, UUID createdBy, UUID assignedTo, TaskPriority taskPriority) {
        assertThat(taskEntity).isNotNull();
        assertThat(taskEntity.getTaskId()).isNotNull();
        assertThat(taskEntity.getTitle()).isEqualTo(taskDTO.getTitle());
        assertThat(taskEntity.getStatus()).isEqualTo(TaskStatus.READY_TO_START);
        assertThat(taskEntity.getPriority()).isEqualTo(taskPriority);
        assertThat(taskEntity.getTargetEndDate()).isEqualTo(taskDTO.getTargetEndDate());
        assertThat(taskEntity.getAssignedTo()).isEqualTo(assignedTo);
        assertThat(taskEntity.getCreatedOn()).isNotNull();
        assertThat(taskEntity.getCreatedBy()).isEqualTo(createdBy);
        assertThat(taskEntity.getUpdatedOn()).isNull();
        assertThat(taskEntity.getUpdatedBy()).isNull();
    }
}
