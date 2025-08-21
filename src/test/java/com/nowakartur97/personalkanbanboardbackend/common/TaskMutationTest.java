package com.nowakartur97.personalkanbanboardbackend.common;

import com.nowakartur97.personalkanbanboardbackend.task.TaskDTO;
import com.nowakartur97.personalkanbanboardbackend.task.TaskPriority;
import com.nowakartur97.personalkanbanboardbackend.task.TaskStatus;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class TaskMutationTest extends TaskIntegrationTest {

    private final int sourceLocationColumn;

    protected TaskMutationTest(String path, String document, RequestVariable requestVariable, int sourceLocationColumn) {
        super(path, document, requestVariable);
        this.sourceLocationColumn = sourceLocationColumn;
    }

    @Test
    public void whenMutateTaskForNotExistingUserAssignedTo_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        UUID assignedTo = UUID.randomUUID();
        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, assignedTo);

        assertNotFoundErrorResponse(sendTaskRequestWithErrors(userEntity, taskDTO), path, "User with userId: '" + assignedTo + "' not found.");
    }

    @Test
    public void whenMutateTaskWithoutTaskData_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();

        assertValidationErrorResponse(sendTaskRequestWithErrors(userEntity, null), new SourceLocation(1, sourceLocationColumn),
                "Variable '" + requestVariable.getName() + "' has an invalid value: Variable '" + requestVariable.getName() + "' has coerced Null value for NonNull type 'TaskDTO!'");
    }

    @Test
    public void whenMutateTaskWithoutTitle_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO(null, null, null, null, null, null);

        assertValidationErrorResponse(sendTaskRequestWithErrors(userEntity, taskDTO), new SourceLocation(1, sourceLocationColumn),
                "Variable '" + requestVariable.getName() + "' has an invalid value: Field 'title' has coerced Null value for NonNull type 'String!'");
    }

    @Test
    public void whenMutateTaskWitBlankTitle_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO("", null, null, null, null, null);

        assertResponseErrors(sendTaskRequestWithErrors(userEntity, taskDTO), path, "Title cannot be empty.", "Title must be between 4 and 100 characters.");
    }

    @Test
    public void whenMutateTaskWithTooShortTitle_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO("ti", null, null, null, null, null);

        assertResponseErrors(sendTaskRequestWithErrors(userEntity, taskDTO), path, "Title must be between 4 and 100 characters.");
    }

    @Test
    public void whenMutateTaskWithTooLongTitle_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO(StringUtils.repeat("t", 101), null, null, null, null, null);

        assertResponseErrors(sendTaskRequestWithErrors(userEntity, taskDTO), path, "Title must be between 4 and 100 characters.");
    }

    @Test
    public void whenMutateTaskWithTooLongDescription_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO("title", StringUtils.repeat("d", 1001), null, null, null, null);

        assertResponseErrors(sendTaskRequestWithErrors(userEntity, taskDTO), path, "Description must be between 0 and 1000 characters.");
    }

    @Test
    public void whenMutateTaskWithTargetEndDateInThePast_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, LocalDate.of(2024, 1, 1), null);

        assertResponseErrors(sendTaskRequestWithErrors(userEntity, taskDTO), path, "Target end date cannot be in the past.");
    }

    protected abstract GraphQlTester.Errors sendTaskRequestWithErrors(UserEntity userEntity, TaskDTO taskDTO);

    protected void assertBaseTaskEntity(BaseTaskEntity taskEntity, TaskDTO taskDTO, UUID createdBy, UUID assignedTo,
                                        TaskStatus taskStatus, TaskPriority taskPriority) {
        assertThat(taskEntity).isNotNull();
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

    protected void assertBaseTaskResponse(BaseTaskResponse taskResponse, TaskDTO taskDTO, String createdBy, String assignedTo,
                                          TaskStatus status, TaskPriority priority) {
        assertThat(taskResponse).isNotNull();
        assertThat(taskResponse.getTaskId()).isNotNull();
        assertThat(taskResponse.getTitle()).isEqualTo(taskDTO.getTitle());
        assertThat(taskResponse.getStatus()).isEqualTo(status);
        assertThat(taskResponse.getPriority()).isEqualTo(priority);
        assertThat(taskResponse.getTargetEndDate()).isEqualTo(taskDTO.getTargetEndDate());
        assertThat(taskResponse.getAssignedTo()).isEqualTo(assignedTo);
        assertThat(taskResponse.getCreatedOn()).isNotNull();
        assertThat(taskResponse.getCreatedBy()).isEqualTo(createdBy);
        assertThat(taskResponse.getUpdatedOn()).isNull();
        assertThat(taskResponse.getUpdatedBy()).isNull();
    }

    protected void assertBaseTaskResponse(BaseTaskResponse taskResponse, BaseTaskEntity taskEntity, TaskDTO taskDTO, String createdBy,
                                          String updatedBy, String assignedTo, TaskStatus status, TaskPriority priority) {
        assertThat(taskResponse).isNotNull();
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
}
