package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.DoubleRequestVariable;
import com.nowakartur97.personalkanbanboardbackend.common.TaskMutationTest;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.time.LocalDate;
import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.UPDATE_TASK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TaskUpdateMutationControllerTest extends TaskMutationTest {

    private final static String UPDATE_TASK_PATH = "updateTask";

    public TaskUpdateMutationControllerTest() {
        super(UPDATE_TASK_PATH, UPDATE_TASK,
                new DoubleRequestVariable("taskId", UUID.randomUUID(), "taskDTO", new TaskDTO("title", "description", null, null, null, null)),
                "taskDTO", 38);
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenUpdateTask_shouldReturnTaskResponse(UserRole role) {

        UserEntity userEntity = createUser(role);
        UserEntity author = createUser("author", "author@domain.com");
        TaskEntity taskEntity = createTask(author.getUserId());
        UserEntity assignedTo = createUser("developer", "developer@domain.com");
        TaskDTO taskDTO = new TaskDTO("title", "description", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, LocalDate.now(), assignedTo.getUserId());

        TaskResponse taskResponse = sendUpdateTaskRequest(userEntity, taskEntity.getTaskId(), taskDTO);

        TaskEntity udatedTaskEntity = taskRepository.findAll().blockLast();
        assertTaskEntity(udatedTaskEntity, taskDTO, author.getUserId(), userEntity.getUserId(), assignedTo.getUserId());
        assertThat(udatedTaskEntity.getTaskId()).isEqualTo(taskEntity.getTaskId());
        assertTaskResponse(taskResponse, udatedTaskEntity, taskDTO, author.getUsername(), userEntity.getUsername(), assignedTo.getUsername(),
                taskDTO.getStatus(), taskDTO.getPriority());
    }

    @Test
    public void whenUpdateAuthorsOwnTask_shouldReturnTaskResponse() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        TaskDTO taskDTO = new TaskDTO("title", "description", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, LocalDate.now(), userEntity.getUserId());

        TaskResponse taskResponse = sendUpdateTaskRequest(userEntity, taskEntity.getTaskId(), taskDTO);

        TaskEntity udatedTaskEntity = taskRepository.findAll().blockLast();
        assertTaskEntity(udatedTaskEntity, taskDTO, userEntity.getUserId(), userEntity.getUserId(), userEntity.getUserId());
        assertThat(udatedTaskEntity.getTaskId()).isEqualTo(taskEntity.getTaskId());
        assertTaskResponse(taskResponse, udatedTaskEntity, taskDTO, userEntity.getUsername());
    }

    @Test
    public void whenUpdateTask_shouldUpdateTaskWithDefaultValuesAndReturnTaskResponse() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, null);

        TaskResponse taskResponse = sendUpdateTaskRequest(userEntity, taskEntity.getTaskId(), taskDTO);

        TaskEntity actualTaskEntity = taskRepository.findAll().blockLast();
        assertTaskEntity(actualTaskEntity, taskDTO, userEntity.getUserId());
        assertThat(actualTaskEntity.getTaskId()).isEqualTo(taskEntity.getTaskId());
        assertTaskResponse(taskResponse, taskRepository.findAll().blockFirst(), taskDTO, userEntity.getUsername(), userEntity.getUsername());
    }

    @Test
    public void whenUpdateNotExistingTask_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        UUID taskId = UUID.randomUUID();
        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, UUID.randomUUID());

        assertNotFoundErrorResponse(sendUpdateTaskRequestWithErrors(userEntity, taskId, taskDTO), path, "Task with taskId: '" + taskId + "' not found.");
    }

    @Test
    public void whenUpdateTaskWithoutTaskId_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, null);

        GraphQlTester.Errors errors = httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(UPDATE_TASK)
                .variable("taskDTO", taskDTO)
                .execute()
                .errors();
        assertValidationErrorResponse(errors, new SourceLocation(1, 22),
                "Variable 'taskId' has an invalid value: Variable 'taskId' has coerced Null value for NonNull type 'UUID!'");
    }

    private TaskResponse sendUpdateTaskRequest(UserEntity userEntity, UUID taskId, TaskDTO taskDTO) {
        DoubleRequestVariable doubleRequestVariable = new DoubleRequestVariable("taskId", taskId, "taskDTO", taskDTO);
        return (TaskResponse) sendRequest(userEntity, document, path, doubleRequestVariable, TaskResponse.class, false);
    }

    private GraphQlTester.Errors sendUpdateTaskRequestWithErrors(UserEntity userEntity, UUID taskId, TaskDTO taskDTO) {
        return httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(UPDATE_TASK)
                .variable("taskId", taskId)
                .variable("taskDTO", taskDTO)
                .execute()
                .errors();
    }

    private void assertTaskResponse(TaskResponse taskResponse, TaskEntity taskEntity, TaskDTO taskDTO, String createdBy) {
        assertTaskResponse(taskResponse, taskEntity, taskDTO, createdBy, createdBy, createdBy, taskDTO.getStatus(), taskDTO.getPriority());
    }

    private void assertTaskResponse(TaskResponse taskResponse, TaskEntity taskEntity, TaskDTO taskDTO, String createdBy, String updatedBy) {
        assertTaskResponse(taskResponse, taskEntity, taskDTO, createdBy, updatedBy, updatedBy, TaskStatus.READY_TO_START, TaskPriority.LOW);
    }

    private void assertTaskResponse(TaskResponse taskResponse, TaskEntity taskEntity, TaskDTO taskDTO, String createdBy,
                                    String updatedBy, String assignedTo, TaskStatus status, TaskPriority priority) {
        assertBaseTaskResponse(taskResponse, taskEntity, taskDTO, createdBy, updatedBy, assignedTo, status, priority);
        assertThat(taskResponse.getSubtasks()).isNull();
    }

    private void assertTaskEntity(TaskEntity taskEntity, TaskDTO taskDTO, UUID createdBy) {
        assertTaskEntity(taskEntity, taskDTO, createdBy, createdBy, createdBy, TaskStatus.READY_TO_START, TaskPriority.LOW);
    }

    private void assertTaskEntity(TaskEntity taskEntity, TaskDTO taskDTO, UUID createdBy, UUID updatedBy, UUID assignedTo) {
        assertTaskEntity(taskEntity, taskDTO, createdBy, updatedBy, assignedTo, taskDTO.getStatus(), taskDTO.getPriority());
    }

    private void assertTaskEntity(TaskEntity taskEntity, TaskDTO taskDTO, UUID createdBy, UUID updatedBy, UUID assignedTo,
                                  TaskStatus taskStatus, TaskPriority taskPriority) {
        assertThat(taskEntity).isNotNull();
        assertThat(taskEntity.getTaskId()).isEqualTo(taskEntity.getTaskId());
        assertThat(taskEntity.getTitle()).isEqualTo(taskDTO.getTitle());
        assertThat(taskEntity.getStatus()).isEqualTo(taskStatus);
        assertThat(taskEntity.getPriority()).isEqualTo(taskPriority);
        assertThat(taskEntity.getTargetEndDate()).isEqualTo(taskDTO.getTargetEndDate());
        assertThat(taskEntity.getAssignedTo()).isEqualTo(assignedTo);
        assertThat(taskEntity.getCreatedOn()).isEqualTo(taskEntity.getCreatedOn());
        assertThat(taskEntity.getCreatedBy()).isEqualTo(createdBy);
        assertThat(taskEntity.getUpdatedOn()).isNotNull();
        assertThat(taskEntity.getUpdatedBy()).isEqualTo(updatedBy);
    }
}
