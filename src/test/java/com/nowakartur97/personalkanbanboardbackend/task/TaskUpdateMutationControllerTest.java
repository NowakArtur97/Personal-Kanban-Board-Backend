package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskResponse;
import com.nowakartur97.personalkanbanboardbackend.integration.IntegrationTest;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.UPDATE_TASK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TaskUpdateMutationControllerTest extends IntegrationTest {

    private final static String UPDATE_TASK_PATH = "updateTask";

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
    public void whenUpdateTaskOwnTask_shouldReturnTaskResponse() {

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

        sendUpdateTaskRequestWithErrors(userEntity, taskId, taskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertNotFoundErrorResponse(responseError, UPDATE_TASK_PATH, "Task with taskId: '" + taskId + "' not found.");
                        });
    }

    @Test
    public void whenUpdateTaskForNotExistingUserAssignedTo_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        UUID assignedTo = UUID.randomUUID();
        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, assignedTo);

        sendUpdateTaskRequestWithErrors(userEntity, taskEntity.getTaskId(), taskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertNotFoundErrorResponse(responseError, UPDATE_TASK_PATH, "User with userId: '" + assignedTo + "' not found.");
                        });
    }

    @Test
    public void whenUpdateTaskWithoutTaskId_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, null);

        httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(UPDATE_TASK)
                .variable("taskDTO", taskDTO)
                .execute()
                .errors()
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertValidationErrorResponse(responseError, new SourceLocation(1, 22),
                                    "Variable 'taskId' has an invalid value: Variable 'taskId' has coerced Null value for NonNull type 'UUID!'"
                            );
                        });
    }

    @Test
    public void whenUpdateTaskWithoutTaskData_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();

        httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(UPDATE_TASK)
                .variable("taskId", UUID.randomUUID())
                .execute()
                .errors()
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertValidationErrorResponse(responseError, new SourceLocation(1, 38),
                                    "Variable 'taskDTO' has an invalid value: Variable 'taskDTO' has coerced Null value for NonNull type 'TaskDTO!'"
                            );
                        });
    }

    @Test
    public void whenUpdateTaskWithoutTitle_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        TaskDTO taskDTO = new TaskDTO(null, null, null, null, null, null);

        sendUpdateTaskRequestWithErrors(userEntity, taskEntity.getTaskId(), taskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertValidationErrorResponse(responseError, new SourceLocation(1, 38),
                                    "Variable 'taskDTO' has an invalid value: Field 'title' has coerced Null value for NonNull type 'String!'");
                        });
    }

    @Test
    public void whenUpdateTaskWitBlankTitle_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        TaskDTO taskDTO = new TaskDTO("", null, null, null, null, null);

        sendUpdateTaskRequestWithErrors(userEntity, taskEntity.getTaskId(), taskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isEqualTo(2);
                            ResponseError firstResponseError = responseErrors.getFirst();
                            assertErrorResponse(firstResponseError, "Title cannot be empty.");
                            ResponseError secondResponseError = responseErrors.getLast();
                            assertErrorResponse(secondResponseError, "Title must be between 4 and 100 characters.");
                        });
    }

    @Test
    public void whenUpdateTaskWitTooShortTitle_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        TaskDTO taskDTO = new TaskDTO("ti", null, null, null, null, null);

        sendUpdateTaskRequestWithErrors(userEntity, taskEntity.getTaskId(), taskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getLast();
                            assertErrorResponse(responseError, "Title must be between 4 and 100 characters.");
                        });
    }

    @Test
    public void whenUpdateTaskWitTooLongTitle_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        TaskDTO taskDTO = new TaskDTO(StringUtils.repeat("t", 101), null, null, null, null, null);

        sendUpdateTaskRequestWithErrors(userEntity, taskEntity.getTaskId(), taskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getLast();
                            assertErrorResponse(responseError, "Title must be between 4 and 100 characters.");
                        });
    }

    @Test
    public void whenUpdateTaskWitTooLongDescription_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        TaskDTO taskDTO = new TaskDTO("title", StringUtils.repeat("d", 1001), null, null, null, null);

        sendUpdateTaskRequestWithErrors(userEntity, taskEntity.getTaskId(), taskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getLast();
                            assertErrorResponse(responseError, "Description must be between 0 and 1000 characters.");
                        });
    }

    @Test
    public void whenUpdateTaskWitTargetEndDateInThePast_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, LocalDate.of(2024, 1, 1), null);

        sendUpdateTaskRequestWithErrors(userEntity, taskEntity.getTaskId(), taskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getLast();
                            assertErrorResponse(responseError, "Target end date cannot be in the past.");
                        });
    }

    @Test
    public void whenUpdateTaskWithoutProvidingAuthorizationHeader_shouldReturnGraphQLErrorResponse() {

        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, null);

        runTestForSendingRequestWithoutProvidingAuthorizationHeader(UPDATE_TASK, UPDATE_TASK_PATH, "taskId", UUID.randomUUID(), "taskDTO", taskDTO);
    }

    @Test
    public void whenUpdateTaskWithExpiredToken_shouldReturnGraphQLErrorResponse() {

        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, null);

        runTestForSendingRequestWithExpiredToken(UPDATE_TASK, UPDATE_TASK_PATH, "taskId", UUID.randomUUID(), "taskDTO", taskDTO);
    }

    @Test
    public void whenUpdateTaskWithInvalidToken_shouldReturnGraphQLErrorResponse() {

        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, null);

        runTestForSendingRequestWithInvalidToken(UPDATE_TASK, UPDATE_TASK_PATH, "taskId", UUID.randomUUID(), "taskDTO", taskDTO);
    }

    @Test
    public void whenUpdateTaskWithDifferentTokenSignature_shouldReturnGraphQLErrorResponse() {

        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, null);

        runTestForSendingRequestWithDifferentTokenSignature(UPDATE_TASK, UPDATE_TASK_PATH, "taskId", UUID.randomUUID(), "taskDTO", taskDTO);
    }

    private TaskResponse sendUpdateTaskRequest(UserEntity userEntity, UUID taskId, TaskDTO taskDTO) {
        return httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(UPDATE_TASK)
                .variable("taskId", taskId)
                .variable("taskDTO", taskDTO)
                .execute()
                .errors()
                .verify()
                .path(UPDATE_TASK_PATH)
                .entity(TaskResponse.class)
                .get();
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

    private void assertTaskResponse(BaseTaskResponse taskResponse, TaskEntity taskEntity, TaskDTO taskDTO, String createdBy) {
        assertTaskResponse(taskResponse, taskEntity, taskDTO, createdBy, createdBy, createdBy, taskDTO.getStatus(), taskDTO.getPriority());
    }

    private void assertTaskResponse(BaseTaskResponse taskResponse, TaskEntity taskEntity, TaskDTO taskDTO, String createdBy, String updatedBy) {
        assertTaskResponse(taskResponse, taskEntity, taskDTO, createdBy, updatedBy, updatedBy, TaskStatus.READY_TO_START, TaskPriority.LOW);
    }

    private void assertTaskResponse(BaseTaskResponse taskResponse, TaskEntity taskEntity, TaskDTO taskDTO, String createdBy,
                                    String updatedBy, String assignedTo, TaskStatus status, TaskPriority priority) {
        assertThat(taskResponse).isNotNull();
        assertThat(taskResponse.getTaskId()).isEqualTo(taskEntity.getTaskId());
        assertThat(taskResponse.getTitle()).isEqualTo(taskDTO.getTitle());
        assertThat(taskResponse.getStatus()).isEqualTo(status);
        assertThat(taskResponse.getPriority()).isEqualTo(priority);
        assertThat(taskResponse.getTargetEndDate()).isEqualTo(taskDTO.getTargetEndDate());
        assertThat(taskResponse.getAssignedTo()).isEqualTo(assignedTo);
        assertThat(Instant.parse(taskResponse.getCreatedOn()).toEpochMilli()).isEqualTo(taskEntity.getCreatedOn().toEpochMilli());
        assertThat(taskResponse.getCreatedBy()).isEqualTo(createdBy);
        assertThat(Instant.parse(taskResponse.getUpdatedOn()).toEpochMilli()).isEqualTo(taskEntity.getUpdatedOn().toEpochMilli());
        assertThat(taskResponse.getUpdatedBy()).isEqualTo(updatedBy);
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

    private void assertErrorResponse(ResponseError responseError, String message) {
        assertErrorResponse(responseError, message, UPDATE_TASK_PATH, new SourceLocation(2, 3));
    }
}
