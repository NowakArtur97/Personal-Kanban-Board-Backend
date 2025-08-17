package com.nowakartur97.personalkanbanboardbackend.common;

import com.nowakartur97.personalkanbanboardbackend.task.TaskDTO;
import com.nowakartur97.personalkanbanboardbackend.task.TaskEntity;
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

public abstract class TaskMutationTest extends BasicIntegrationTest {

    private final String taskDTOVariableName;
    private final int sourceLocationColumn;

    protected TaskMutationTest(String path, String document, RequestVariable requestVariable,
                               String taskDTOVariableName, int sourceLocationColumn) {
        super(path, document, requestVariable);
        this.taskDTOVariableName = taskDTOVariableName;
        this.sourceLocationColumn = sourceLocationColumn;
    }

    @Test
    public void whenMutateTaskForNotExistingUserAssignedTo_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        UUID assignedTo = UUID.randomUUID();
        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, assignedTo);
        TaskEntity taskEntity = createTask(userEntity.getUserId());

        sendTaskRequestWithErrors(userEntity, taskDTO, taskEntity.getTaskId())
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertNotFoundErrorResponse(responseError, path, "User with userId: '" + assignedTo + "' not found.");
                        });
    }

    @Test
    public void whenMutateTaskWithoutTaskData_shouldReturnGraphQLErrorResponse() {

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
    public void whenMutateTaskWithoutTitle_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO(null, null, null, null, null, null);

        sendTaskRequestWithErrors(userEntity, taskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertValidationErrorResponse(responseError, new SourceLocation(1, sourceLocationColumn),
                                    "Variable '" + taskDTOVariableName + "' has an invalid value: Field 'title' has coerced Null value for NonNull type 'String!'");
                        });
    }

    @Test
    public void whenMutateTaskWitBlankTitle_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO("", null, null, null, null, null);

        sendTaskRequestWithErrors(userEntity, taskDTO)
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
    public void whenMutateTaskWithTooShortTitle_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO("ti", null, null, null, null, null);

        sendTaskRequestWithErrors(userEntity, taskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getLast();
                            assertErrorResponse(responseError, "Title must be between 4 and 100 characters.");
                        });
    }

    @Test
    public void whenMutateTaskWithTooLongTitle_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO(StringUtils.repeat("t", 101), null, null, null, null, null);

        sendTaskRequestWithErrors(userEntity, taskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getLast();
                            assertErrorResponse(responseError, "Title must be between 4 and 100 characters.");
                        });
    }

    @Test
    public void whenMutateTaskWithTooLongDescription_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO("title", StringUtils.repeat("d", 1001), null, null, null, null);

        sendTaskRequestWithErrors(userEntity, taskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getLast();
                            assertErrorResponse(responseError, "Description must be between 0 and 1000 characters.");
                        });
    }

    @Test
    public void whenMutateTaskWithTargetEndDateInThePast_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, LocalDate.of(2024, 1, 1), null);

        sendTaskRequestWithErrors(userEntity, taskDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getLast();
                            assertErrorResponse(responseError, "Target end date cannot be in the past.");
                        });
    }

    private GraphQlTester.Errors sendTaskRequestWithErrors(UserEntity userEntity, TaskDTO taskDTO) {
        return sendTaskRequestWithErrors(userEntity, taskDTO, UUID.randomUUID());
    }

    private GraphQlTester.Errors sendTaskRequestWithErrors(UserEntity userEntity, TaskDTO taskDTO, UUID taskId) {
        return httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(document)
                .variable("taskId", taskId)
                .variable(taskDTOVariableName, taskDTO)
                .execute()
                .errors();
    }

    private void assertErrorResponse(ResponseError responseError, String message) {
        assertErrorResponse(responseError, message, path, new SourceLocation(2, 3));
    }
}
