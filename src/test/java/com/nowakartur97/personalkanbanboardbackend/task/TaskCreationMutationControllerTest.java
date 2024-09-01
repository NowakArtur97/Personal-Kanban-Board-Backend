package com.nowakartur97.personalkanbanboardbackend.task;

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

import java.time.LocalDate;
import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.CREATE_TASK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TaskCreationMutationControllerTest extends IntegrationTest {

    private final static String CREATE_TASK_PATH = "createTask";

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

    @Test
    public void whenCreateTaskForNotExistingUserAssignedTo_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        UUID assignedTo = UUID.randomUUID();
        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, assignedTo);

        sendCreateTaskRequestWithErrors(userEntity, taskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertNotFoundErrorResponse(responseError, CREATE_TASK_PATH, "User with userId: '" + assignedTo + "' not found.");
                        });
    }

    @Test
    public void whenCreateTaskWithoutTaskData_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();

        httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(CREATE_TASK)
                .execute()
                .errors()
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertValidationErrorResponse(responseError, new SourceLocation(1, 22),
                                    "Variable 'taskDTO' has an invalid value: Variable 'taskDTO' has coerced Null value for NonNull type 'TaskDTO!'"
                            );
                        });
    }

    @Test
    public void whenCreateTaskWithoutTitle_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO(null, null, null, null, null, null);

        sendCreateTaskRequestWithErrors(userEntity, taskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertValidationErrorResponse(responseError, new SourceLocation(1, 22),
                                    "Variable 'taskDTO' has an invalid value: Field 'title' has coerced Null value for NonNull type 'String!'");
                        });
    }

    @Test
    public void whenCreateTaskWitBlankTitle_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO("", null, null, null, null, null);

        sendCreateTaskRequestWithErrors(userEntity, taskDTO)
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
    public void whenCreateTaskWitTooShortTitle_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO("ti", null, null, null, null, null);

        sendCreateTaskRequestWithErrors(userEntity, taskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getLast();
                            assertErrorResponse(responseError, "Title must be between 4 and 100 characters.");
                        });
    }

    @Test
    public void whenCreateTaskWitTooLongTitle_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO(StringUtils.repeat("t", 101), null, null, null, null, null);

        sendCreateTaskRequestWithErrors(userEntity, taskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getLast();
                            assertErrorResponse(responseError, "Title must be between 4 and 100 characters.");
                        });
    }

    @Test
    public void whenCreateTaskWitTooLongDescription_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO("title", StringUtils.repeat("d", 1001), null, null, null, null);

        sendCreateTaskRequestWithErrors(userEntity, taskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getLast();
                            assertErrorResponse(responseError, "Description must be between 0 and 1000 characters.");
                        });
    }

    @Test
    public void whenCreateTaskWitTargetEndDateInThePast_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, LocalDate.of(2024, 1, 1), null);

        sendCreateTaskRequestWithErrors(userEntity, taskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getLast();
                            assertErrorResponse(responseError, "Target end date cannot be in the past.");
                        });
    }

    @Test
    public void whenCreateTaskWithoutProvidingAuthorizationHeader_shouldReturnGraphQLErrorResponse() {

        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, null);

        runTestForSendingRequestWithoutProvidingAuthorizationHeader(CREATE_TASK, CREATE_TASK_PATH, "taskDTO", taskDTO);
    }

    @Test
    public void whenCreateTaskWithExpiredToken_shouldReturnGraphQLErrorResponse() {

        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, null);

        runTestForSendingRequestWithExpiredToken(CREATE_TASK, CREATE_TASK_PATH, "taskDTO", taskDTO);
    }

    @Test
    public void whenCreateTaskWithInvalidToken_shouldReturnGraphQLErrorResponse() {

        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, null);

        runTestForSendingRequestWithInvalidToken(CREATE_TASK, CREATE_TASK_PATH, "taskDTO", taskDTO);
    }

    @Test
    public void whenCreateTaskWithDifferentTokenSignature_shouldReturnGraphQLErrorResponse() {

        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, null);

        runTestForSendingRequestWithDifferentTokenSignature(CREATE_TASK, CREATE_TASK_PATH, "taskDTO", taskDTO);
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

    private GraphQlTester.Errors sendCreateTaskRequestWithErrors(UserEntity userEntity, TaskDTO taskDTO) {
        return httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(CREATE_TASK)
                .variable("taskDTO", taskDTO)
                .execute()
                .errors();
    }

    private void assertTaskResponse(TaskResponse taskResponse, TaskDTO taskDTO, String createdBy) {
        assertTaskResponse(taskResponse, taskDTO, createdBy, createdBy, TaskStatus.READY_TO_START, TaskPriority.LOW);
    }

    private void assertTaskResponse(TaskResponse taskResponse, TaskDTO taskDTO, String createdBy, String assignedTo,
                                    TaskStatus status, TaskPriority priority) {
        assertThat(taskResponse).isNotNull();
        assertThat(taskResponse.taskId()).isNotNull();
        assertThat(taskResponse.title()).isEqualTo(taskDTO.getTitle());
        assertThat(taskResponse.status()).isEqualTo(status);
        assertThat(taskResponse.priority()).isEqualTo(priority);
        assertThat(taskResponse.targetEndDate()).isEqualTo(taskDTO.getTargetEndDate());
        assertThat(taskResponse.assignedTo()).isEqualTo(assignedTo);
        assertThat(taskResponse.createdOn()).isNotNull();
        assertThat(taskResponse.createdBy()).isEqualTo(createdBy);
        assertThat(taskResponse.updatedOn()).isNull();
        assertThat(taskResponse.updatedBy()).isNull();
    }

    private void assertTaskEntity(TaskEntity taskEntity, TaskDTO taskDTO, UUID createdBy) {
        assertTaskEntity(taskEntity, taskDTO, createdBy, createdBy, TaskStatus.READY_TO_START, TaskPriority.LOW);
    }

    private void assertTaskEntity(TaskEntity taskEntity, TaskDTO taskDTO, UUID createdBy, UUID assignedTo) {
        assertTaskEntity(taskEntity, taskDTO, createdBy, assignedTo, taskDTO.getStatus(), taskDTO.getPriority());
    }

    private void assertTaskEntity(TaskEntity taskEntity, TaskDTO taskDTO, UUID createdBy, UUID assignedTo,
                                  TaskStatus taskStatus, TaskPriority taskPriority) {
        assertThat(taskEntity).isNotNull();
        assertThat(taskEntity.getTaskId()).isNotNull();
        assertThat(taskEntity.getTitle()).isEqualTo(taskDTO.getTitle());
        assertThat(taskEntity.getStatus()).isEqualTo(taskStatus);
        assertThat(taskEntity.getPriority()).isEqualTo(taskPriority);
        assertThat(taskEntity.getTargetEndDate()).isEqualTo(taskDTO.getTargetEndDate());
        assertThat(taskEntity.getAssignedTo()).isEqualTo(assignedTo);
        assertThat(taskEntity.getCreatedOn()).isNotNull();
        assertThat(taskEntity.getCreatedBy()).isEqualTo(createdBy);
        assertThat(taskEntity.getUpdatedOn()).isNull();
        assertThat(taskEntity.getUpdatedBy()).isNull();
    }

    private void assertErrorResponse(ResponseError responseError, String message) {
        assertErrorResponse(responseError, message, CREATE_TASK_PATH, new SourceLocation(2, 3));
    }
}
