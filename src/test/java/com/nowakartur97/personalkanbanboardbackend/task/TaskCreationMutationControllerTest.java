package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.integration.IntegrationTest;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

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

    @Test
    public void whenCreateTaskForNotExistingUserAssignedTo_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        UUID assignedTo = UUID.randomUUID();
        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, assignedTo);

        makeCreateTaskRequestWithErrors(userEntity, taskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertNotFoundErrorResponse(responseError, "createTask", "User with userId: '" + assignedTo + "' not found.");
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
        TaskDTO taskDTO = new TaskDTO(null, null, null, null, null);

        makeCreateTaskRequestWithErrors(userEntity, taskDTO)
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
        TaskDTO taskDTO = new TaskDTO("", null, null, null, null);

        makeCreateTaskRequestWithErrors(userEntity, taskDTO)
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
        TaskDTO taskDTO = new TaskDTO("ti", null, null, null, null);

        makeCreateTaskRequestWithErrors(userEntity, taskDTO)
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
        TaskDTO taskDTO = new TaskDTO(StringUtils.repeat("t", 101), null, null, null, null);

        makeCreateTaskRequestWithErrors(userEntity, taskDTO)
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
        TaskDTO taskDTO = new TaskDTO("title", StringUtils.repeat("d", 101), null, null, null);

        makeCreateTaskRequestWithErrors(userEntity, taskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getLast();
                            assertErrorResponse(responseError, "Description must be between 0 and 100 characters.");
                        });
    }

    @Test
    public void whenCreateTaskWithoutProvidingAuthorizationHeader_shouldReturnGraphQLErrorResponse() {

        TaskDTO taskDTO = new TaskDTO("title", "description", TaskPriority.MEDIUM, LocalDate.of(2024, 8, 12), null);

        httpGraphQlTester
                .mutate()
                .build()
                .document(CREATE_TASK)
                .variable("taskDTO", taskDTO)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertUnauthorizedErrorResponse(responseError, "createTask", "Unauthorized");
                });
    }

    @Test
    public void whenCreateTaskWithExpiredToken_shouldReturnGraphQLErrorResponse() {

        TaskDTO taskDTO = new TaskDTO("title", "description", TaskPriority.MEDIUM, LocalDate.of(2024, 8, 12), null);
        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjpbIlVTRVIiXSwic3ViIjoidGVzdFVzZXIiLCJpYXQiOjE3MTEyNzg1ODAsImV4cCI6MTcxMTI3ODU4MH0.nouAgIkDaanTk0LX37HSRjM4SDZxqBqz1gDufnU2fzQ";

        httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, expiredToken))
                .build()
                .document(CREATE_TASK)
                .variable("taskDTO", taskDTO)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertUnauthorizedErrorResponse(responseError, "createTask", "JWT expired");
                });
    }

    @Test
    public void whenCreateTaskWithInvalidToken_shouldReturnGraphQLErrorResponse() {

        TaskDTO taskDTO = new TaskDTO("title", "description", TaskPriority.MEDIUM, LocalDate.of(2024, 8, 12), null);
        String invalidToken = "invalid";

        httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, invalidToken))
                .build()
                .document(CREATE_TASK)
                .variable("taskDTO", taskDTO)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertUnauthorizedErrorResponse(responseError, "createTask", "Invalid compact JWT string: Compact JWSs must contain exactly 2 period characters, and compact JWEs must contain exactly 4.  Found: 0");
                });
    }

    @Test
    public void whenCreateTaskWithDifferentTokenSignature_shouldReturnGraphQLErrorResponse() {

        TaskDTO taskDTO = new TaskDTO("title", "description", TaskPriority.MEDIUM, LocalDate.of(2024, 8, 12), null);
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjpbIlVTRVIiXSwic3ViIjoidXNlciIsImlhdCI6MTcxMTIwOTEzMiwiZXhwIjoxNzExMjE5OTMyfQ.n-h8vIdov2voZhwNdqbmgiO44XjeCdAMzf7ddqufoXc";

        httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, invalidToken))
                .build()
                .document(CREATE_TASK)
                .variable("taskDTO", taskDTO)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size()).isOne();
                    ResponseError responseError = responseErrors.getFirst();
                    assertUnauthorizedErrorResponse(responseError, "createTask",
                            "JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.");
                });
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

    private GraphQlTester.Errors makeCreateTaskRequestWithErrors(UserEntity userEntity, TaskDTO taskDTO) {
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

    private void assertErrorResponse(ResponseError responseError, String message) {
        assertErrorResponse(responseError, message, "createTask", new SourceLocation(2, 3));
    }
}
