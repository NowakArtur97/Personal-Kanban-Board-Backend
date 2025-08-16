package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.integration.IntegrationTest;
import com.nowakartur97.personalkanbanboardbackend.task.TaskDTO;
import com.nowakartur97.personalkanbanboardbackend.task.TaskEntity;
import com.nowakartur97.personalkanbanboardbackend.task.TaskPriority;
import com.nowakartur97.personalkanbanboardbackend.task.TaskStatus;
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

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.CREATE_SUBTASK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SubtaskCreationMutationControllerTest extends IntegrationTest {

    private final static String CREATE_SUBTASK_PATH = "createSubtask";

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenCreateSubtask_shouldReturnSubtaskResponse(UserRole role) {

        UserEntity userEntity = createUser(role);
        UserEntity assignedTo = createUser("developer", "developer@domain.com");
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        TaskDTO subtaskDTO = new TaskDTO("title", "description", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, LocalDate.now(), assignedTo.getUserId());

        SubtaskResponse subtaskResponse = sendCreateSubtaskRequest(userEntity, taskEntity.getTaskId(), subtaskDTO);

        assertSubtaskEntity(subtaskRepository.findAll().blockLast(), taskEntity.getTaskId(), subtaskDTO, userEntity.getUserId(), assignedTo.getUserId());
        assertSubtaskResponse(subtaskResponse, taskEntity.getTaskId(), subtaskDTO, userEntity.getUsername(), assignedTo.getUsername(),
                subtaskDTO.getStatus(), subtaskDTO.getPriority());
    }

    @Test
    public void whenCreateSubtask_shouldCreateSubtaskWithDefaultValuesAndReturnSubtaskResponse() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        TaskDTO subtaskDTO = new TaskDTO("title", "description", null, null, null, null);

        SubtaskResponse subtaskResponse = sendCreateSubtaskRequest(userEntity, taskEntity.getTaskId(), subtaskDTO);

        assertSubtaskEntity(subtaskRepository.findAll().blockLast(), taskEntity.getTaskId(), subtaskDTO, userEntity.getUserId());
        assertSubtaskResponse(subtaskResponse, taskEntity.getTaskId(), subtaskDTO, userEntity.getUsername());
    }

    @Test
    public void whenCreateSubtaskForNotExistingTask_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        UUID taskId = UUID.randomUUID();
        TaskDTO subtaskDTO = new TaskDTO("title", "description", null, null, null, UUID.randomUUID());

        sendCreateSubtaskRequestWithErrors(userEntity, taskId, subtaskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertNotFoundErrorResponse(responseError, CREATE_SUBTASK_PATH, "Task with taskId: '" + taskId + "' not found.");
                        });
    }

    @Test
    public void whenCreateSubtaskForNotExistingUserAssignedTo_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        UUID assignedTo = UUID.randomUUID();
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        TaskDTO subtaskDTO = new TaskDTO("title", "description", null, null, null, assignedTo);

        sendCreateSubtaskRequestWithErrors(userEntity, taskEntity.getTaskId(), subtaskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertNotFoundErrorResponse(responseError, CREATE_SUBTASK_PATH, "User with userId: '" + assignedTo + "' not found.");
                        });
    }

    @Test
    public void whenCreateSubtaskWithoutTaskId_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO subtaskDTO = new TaskDTO("title", "description", null, null, null, null);

        httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(CREATE_SUBTASK)
                .variable("taskDTO", subtaskDTO)
                .execute()
                .errors()
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertValidationErrorResponse(responseError, new SourceLocation(1, 25),
                                    "Variable 'taskId' has an invalid value: Variable 'taskId' has coerced Null value for NonNull type 'UUID!'"
                            );
                        });
    }

    @Test
    public void whenCreateSubtaskWithoutSubtaskData_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity.getUserId());

        httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(CREATE_SUBTASK)
                .variable("taskId", taskEntity.getTaskId())
                .execute()
                .errors()
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertValidationErrorResponse(responseError, new SourceLocation(1, 41),
                                    "Variable 'subtaskDTO' has an invalid value: Variable 'subtaskDTO' has coerced Null value for NonNull type 'TaskDTO!'"
                            );
                        });
    }

    @Test
    public void whenCreateSubtaskWithoutTitle_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        TaskDTO subtaskDTO = new TaskDTO(null, null, null, null, null, null);

        sendCreateSubtaskRequestWithErrors(userEntity, taskEntity.getTaskId(), subtaskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertValidationErrorResponse(responseError, new SourceLocation(1, 41),
                                    "Variable 'subtaskDTO' has an invalid value: Field 'title' has coerced Null value for NonNull type 'String!'");
                        });
    }

    @Test
    public void whenCreateSubtaskWitBlankTitle_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        TaskDTO subtaskDTO = new TaskDTO("", null, null, null, null, null);

        sendCreateSubtaskRequestWithErrors(userEntity, taskEntity.getTaskId(), subtaskDTO)
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
    public void whenCreateSubtaskWitTooShortTitle_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        TaskDTO subtaskDTO = new TaskDTO("ti", null, null, null, null, null);

        sendCreateSubtaskRequestWithErrors(userEntity, taskEntity.getTaskId(), subtaskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getLast();
                            assertErrorResponse(responseError, "Title must be between 4 and 100 characters.");
                        });
    }

    @Test
    public void whenCreateSubtaskWitTooLongTitle_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        TaskDTO subtaskDTO = new TaskDTO(StringUtils.repeat("t", 101), null, null, null, null, null);

        sendCreateSubtaskRequestWithErrors(userEntity, taskEntity.getTaskId(), subtaskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getLast();
                            assertErrorResponse(responseError, "Title must be between 4 and 100 characters.");
                        });
    }

    @Test
    public void whenCreateSubtaskWitTooLongDescription_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        TaskDTO subtaskDTO = new TaskDTO("title", StringUtils.repeat("d", 1001), null, null, null, null);

        sendCreateSubtaskRequestWithErrors(userEntity, taskEntity.getTaskId(), subtaskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getLast();
                            assertErrorResponse(responseError, "Description must be between 0 and 1000 characters.");
                        });
    }

    @Test
    public void whenCreateSubtaskWitTargetEndDateInThePast_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        TaskDTO subtaskDTO = new TaskDTO("title", "description", null, null, LocalDate.of(2024, 1, 1), null);

        sendCreateSubtaskRequestWithErrors(userEntity, taskEntity.getTaskId(), subtaskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getLast();
                            assertErrorResponse(responseError, "Target end date cannot be in the past.");
                        });
    }

    @Test
    public void whenCreateSubtaskWithoutProvidingAuthorizationHeader_shouldReturnGraphQLErrorResponse() {

        TaskDTO subtaskDTO = new TaskDTO("title", "description", null, null, null, null);

        runTestForSendingRequestWithoutProvidingAuthorizationHeader(CREATE_SUBTASK, CREATE_SUBTASK_PATH, "taskId", UUID.randomUUID(), "subtaskDTO", subtaskDTO);
    }

    @Test
    public void whenCreateSubtaskWithExpiredToken_shouldReturnGraphQLErrorResponse() {

        TaskDTO subtaskDTO = new TaskDTO("title", "description", null, null, null, null);

        runTestForSendingRequestWithExpiredToken(CREATE_SUBTASK, CREATE_SUBTASK_PATH, "taskId", UUID.randomUUID(), "subtaskDTO", subtaskDTO);
    }

    @Test
    public void whenCreateSubtaskWithInvalidToken_shouldReturnGraphQLErrorResponse() {

        TaskDTO subtaskDTO = new TaskDTO("title", "description", null, null, null, null);

        runTestForSendingRequestWithInvalidToken(CREATE_SUBTASK, CREATE_SUBTASK_PATH, "taskId", UUID.randomUUID(), "subtaskDTO", subtaskDTO);
    }

    @Test
    public void whenCreateSubtaskWithDifferentTokenSignature_shouldReturnGraphQLErrorResponse() {

        TaskDTO subtaskDTO = new TaskDTO("title", "description", null, null, null, null);

        runTestForSendingRequestWithDifferentTokenSignature(CREATE_SUBTASK, CREATE_SUBTASK_PATH, "taskId", UUID.randomUUID(), "subtaskDTO", subtaskDTO);
    }

    private SubtaskResponse sendCreateSubtaskRequest(UserEntity userEntity, UUID taskId, TaskDTO subtaskDTO) {
        return httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(CREATE_SUBTASK)
                .variable("taskId", taskId)
                .variable("subtaskDTO", subtaskDTO)
                .execute()
                .errors()
                .verify()
                .path(CREATE_SUBTASK_PATH)
                .entity(SubtaskResponse.class)
                .get();
    }

    private GraphQlTester.Errors sendCreateSubtaskRequestWithErrors(UserEntity userEntity, UUID taskId, TaskDTO subtaskDTO) {
        return httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(CREATE_SUBTASK)
                .variable("taskId", taskId)
                .variable("subtaskDTO", subtaskDTO)
                .execute()
                .errors();
    }

    private void assertSubtaskResponse(SubtaskResponse subtaskResponse, UUID taskId, TaskDTO subtaskDTO, String createdBy) {
        assertSubtaskResponse(subtaskResponse, taskId, subtaskDTO, createdBy, createdBy, TaskStatus.READY_TO_START, TaskPriority.LOW);
    }

    private void assertSubtaskResponse(SubtaskResponse subtaskResponse, UUID taskId, TaskDTO subtaskDTO,
                                       String createdBy, String assignedTo,
                                       TaskStatus status, TaskPriority priority) {
        assertBaseTaskResponse(subtaskResponse, subtaskDTO, createdBy, assignedTo, status, priority);
        assertThat(subtaskResponse.getSubtaskId()).isNotNull();
        assertThat(subtaskResponse.getTaskId()).isEqualTo(taskId);
    }

    private void assertSubtaskEntity(SubtaskEntity subtaskEntity, UUID taskId, TaskDTO subtaskDTO, UUID createdBy) {
        assertSubtaskEntity(subtaskEntity, taskId, subtaskDTO, createdBy, createdBy, TaskStatus.READY_TO_START, TaskPriority.LOW);
    }

    private void assertSubtaskEntity(SubtaskEntity subtaskEntity, UUID taskId, TaskDTO subtaskDTO, UUID createdBy, UUID assignedTo) {
        assertSubtaskEntity(subtaskEntity, taskId, subtaskDTO, createdBy, assignedTo, subtaskDTO.getStatus(), subtaskDTO.getPriority());
    }

    private void assertSubtaskEntity(SubtaskEntity subtaskEntity, UUID taskId, TaskDTO subtaskDTO, UUID createdBy, UUID assignedTo,
                                     TaskStatus subtaskStatus, TaskPriority subtaskPriority) {
        assertBaseTaskEntity(subtaskEntity, subtaskDTO, createdBy, assignedTo, subtaskStatus, subtaskPriority);
        assertThat(subtaskEntity.getSubtaskId()).isNotNull();
        assertThat(subtaskEntity.getTaskId()).isEqualTo(taskId);
    }

    private void assertErrorResponse(ResponseError responseError, String message) {
        assertErrorResponse(responseError, message, CREATE_SUBTASK_PATH, new SourceLocation(2, 3));
    }
}
