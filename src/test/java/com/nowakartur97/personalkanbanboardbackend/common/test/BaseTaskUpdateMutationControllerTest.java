package com.nowakartur97.personalkanbanboardbackend.common.test;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskEntity;
import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskResponse;
import com.nowakartur97.personalkanbanboardbackend.common.TaskEventType;
import com.nowakartur97.personalkanbanboardbackend.common.request.DoubleRequestVariable;
import com.nowakartur97.personalkanbanboardbackend.common.request.RequestVariable;
import com.nowakartur97.personalkanbanboardbackend.task.TaskDTO;
import com.nowakartur97.personalkanbanboardbackend.task.TaskPriority;
import com.nowakartur97.personalkanbanboardbackend.task.TaskStatus;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class BaseTaskUpdateMutationControllerTest<E extends BaseTaskEntity, R extends BaseTaskResponse> extends TaskMutationTest<E, R> {

    private final String className;
    protected final String idFieldName;
    private final int taskIdErrorSourceLocationColumn;

    protected BaseTaskUpdateMutationControllerTest(String path, String document, RequestVariable requestVariable, int validationErrorSourceLocationColumn,
                                                   String subscriptionDocument, String subscriptionPath, Class<?> subscriptionEntityTyp,
                                                   String className, String idFieldName, int taskIdErrorSourceLocationColumn) {
        super(path, document, requestVariable, validationErrorSourceLocationColumn, subscriptionDocument, subscriptionPath, subscriptionEntityTyp);
        this.className = className;
        this.idFieldName = idFieldName;
        this.taskIdErrorSourceLocationColumn = taskIdErrorSourceLocationColumn;
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenUpdateTask_shouldReturnTaskResponse(UserRole role) {

        UserEntity updatedBy = createUser(role);
        UserEntity author = createUser("author", "author@domain.com");
        E taskEntity = createTask(author);
        UserEntity assignedTo = createUser("developer", "developer@domain.com");
        TaskDTO taskDTO = new TaskDTO("title", "description", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, LocalDate.now(), assignedTo.getUserId());

        assertTaskMutationAndSubscription(author, updatedBy, assignedTo, taskEntity, taskDTO);
    }

    @Test
    public void whenUpdateAuthorsOwnTask_shouldReturnTaskResponse() {

        UserEntity userEntity = createUser();
        E taskEntity = createTask(userEntity);
        TaskDTO taskDTO = new TaskDTO("title", "description", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, LocalDate.now(), userEntity.getUserId());

        assertTaskMutationAndSubscription(userEntity, userEntity, userEntity, taskEntity, taskDTO);
    }

    @Test
    public void whenUpdateTask_shouldUpdateTaskWithDefaultValuesAndReturnTaskResponse() {

        UserEntity userEntity = createUser();
        E taskEntity = createTask(userEntity);
        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, null);

        assertTaskMutationAndSubscription(userEntity, userEntity, userEntity, taskEntity, taskDTO);
    }

    @Test
    public void whenUpdateNotExistingTask_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        UUID taskId = UUID.randomUUID();
        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, UUID.randomUUID());
        DoubleRequestVariable doubleRequestVariable = new DoubleRequestVariable(idFieldName, taskId, requestVariable.getName(), taskDTO);

        assertNotFoundErrorResponse(sendRequestWithErrors(userEntity, document, doubleRequestVariable), path, className + " with " + idFieldName + ": '" + taskId + "' not found.");
    }

    @Test
    public void whenUpdateTaskWithoutTaskId_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, null);
        RequestVariable reqVariable = new RequestVariable(requestVariable.getName(), taskDTO);

        assertValidationErrorResponse(sendRequestWithErrors(userEntity, document, reqVariable), new SourceLocation(1, taskIdErrorSourceLocationColumn),
                "Variable '" + idFieldName + "' has an invalid value: Variable '" + idFieldName + "' has coerced Null value for NonNull type 'UUID!'");
    }

    protected abstract E createTask(UserEntity userEntity);

    protected abstract R sendUpdateTaskRequest(UserEntity userEntity, E taskEntity, TaskDTO taskDTO);

    private void assertTaskMutationAndSubscription(UserEntity author, UserEntity updatedBy, UserEntity assignedTo, E taskEntity, TaskDTO taskDTO) {
        assertTaskMutationAndSubscription(author, sendUpdateTaskRequest(updatedBy, taskEntity, taskDTO), TaskEventType.UPDATE,
                (updatedTaskEntity, taskResponse, taskEvent) -> {
                    assertTaskEntity(updatedTaskEntity, taskDTO, author, updatedBy, assignedTo);
                    assertTaskId(updatedTaskEntity, taskEntity);
                    assertTaskResponse(taskResponse, updatedTaskEntity, taskDTO, author.getUsername(), updatedBy.getUsername(), assignedTo.getUsername());
                    assertTaskEventResponse(taskEvent.getTask(), createExpectedSubscriptionResponse(updatedTaskEntity, taskDTO, author.getUsername(), updatedBy.getUsername(), assignedTo.getUsername()));
                });
    }

    protected abstract void assertTaskId(E updatedTaskEntity, E taskEntity);

    private void assertTaskEntity(E updatedTaskEntity, TaskDTO taskDTO, UserEntity createdBy, UserEntity updatedBy, UserEntity assignedTo) {
        assertTaskEntity(updatedTaskEntity, taskDTO, createdBy.getUserId(), updatedBy.getUserId(), assignedTo.getUserId());
    }

    private void assertTaskEntity(E taskEntity, TaskDTO taskDTO, UUID createdBy, UUID updatedBy, UUID assignedTo) {
        assertThat(taskEntity).isNotNull();
        assertThat(taskEntity.getTitle()).isEqualTo(taskDTO.getTitle());
        assertThat(taskEntity.getStatus()).isEqualTo(taskDTO.getStatus() != null ? taskDTO.getStatus() : TaskStatus.READY_TO_START);
        assertThat(taskEntity.getPriority()).isEqualTo(taskDTO.getPriority() != null ? taskDTO.getPriority() : TaskPriority.LOW);
        assertThat(taskEntity.getTargetEndDate()).isEqualTo(taskDTO.getTargetEndDate());
        assertThat(taskEntity.getAssignedTo()).isEqualTo(assignedTo);
        assertThat(taskEntity.getCreatedOn()).isEqualTo(taskEntity.getCreatedOn());
        assertThat(taskEntity.getCreatedBy()).isEqualTo(createdBy);
        assertThat(taskEntity.getUpdatedOn()).isNotNull();
        assertThat(taskEntity.getUpdatedBy()).isEqualTo(updatedBy);
    }

    protected abstract void assertTaskResponse(R taskResponse, E taskEntity, TaskDTO taskDTO,
                                               String createdBy, String updatedBy, String assignedTo);

    protected abstract R createExpectedSubscriptionResponse(E taskEntity, TaskDTO taskDTO, String createdBy, String updatedBy, String assignedTo);
}
