package com.nowakartur97.personalkanbanboardbackend.common;

import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import graphql.language.SourceLocation;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.UUID;

public abstract class BaseUserAssignedToTaskUpdateMutationControllerTest<E extends BaseTaskEntity, R extends BaseTaskResponse> extends TaskIntegrationTest {

    private final String className;
    private final String idFieldName;
    private final int taskIdErrorSourceLocationColumn;
    private final int assignedToIdErrorSourceLocationColumn;

    @Setter
    private BaseTaskRepository<E> repository;

    public BaseUserAssignedToTaskUpdateMutationControllerTest(String path, String document, RequestVariable requestVariable,
                                                              String className, String idFieldName,
                                                              int taskIdErrorSourceLocationColumn, int assignedToIdErrorSourceLocationColumn) {
        super(path, document, requestVariable);
        this.className = className;
        this.idFieldName = idFieldName;
        this.taskIdErrorSourceLocationColumn = taskIdErrorSourceLocationColumn;
        this.assignedToIdErrorSourceLocationColumn = assignedToIdErrorSourceLocationColumn;
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenUpdateUserAssignedToTask_shouldReturnTaskResponse(UserRole role) {

        UserEntity userEntity = createUser(role);
        E taskEntity = createTask(userEntity);
        UserEntity assignedTo = createUser("developer", "developer@domain.com");

        R taskResponse = sendUpdateUserAssignedToTaskRequest(userEntity, taskEntity, assignedTo.getUserId());

        E updatedTaskEntity = repository.findAll().blockLast();
        assertTaskEntity(taskEntity, updatedTaskEntity, assignedTo.getUserId());
        assertTaskResponse(taskResponse, updatedTaskEntity, assignedTo, userEntity);
    }

    @Test
    public void whenUpdateUserAssignedToNotExistingTask_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        UUID taskId = UUID.randomUUID();

        assertNotFoundErrorResponse(sendUpdateUserAssignedToTaskRequestWithErrors(userEntity, taskId, userEntity.getUserId()),
                path, className + " with " + idFieldName + ": '" + taskId + "' not found.");
    }

    @Test
    public void whenUpdateUserAssignedToTaskForNotExistingUser_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        E taskEntity = createTask(userEntity);
        UUID assignedTo = UUID.randomUUID();

        assertNotFoundErrorResponse(sendUpdateUserAssignedToTaskRequestWithErrors(userEntity, taskEntity, assignedTo),
                path, "User with userId: '" + assignedTo + "' not found.");
    }

    @Test
    public void whenUpdateUserAssignedToTaskWithoutTaskId_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        RequestVariable reqVariable = new RequestVariable("assignedToId", UUID.randomUUID());

        assertValidationErrorResponse(sendRequestWithErrors(userEntity, document, reqVariable), new SourceLocation(1, taskIdErrorSourceLocationColumn),
                "Variable '" + requestVariable.getName() + "' has an invalid value: Variable '" + requestVariable.getName() + "' has coerced Null value for NonNull type 'UUID!'");
    }

    @Test
    public void whenUpdateUserAssignedToTaskWithoutAssignedToId_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        RequestVariable reqVariable = new RequestVariable(requestVariable.getName(), UUID.randomUUID());

        assertValidationErrorResponse(sendRequestWithErrors(userEntity, document, reqVariable), new SourceLocation(1, assignedToIdErrorSourceLocationColumn),
                "Variable 'assignedToId' has an invalid value: Variable 'assignedToId' has coerced Null value for NonNull type 'UUID!'");
    }

    protected abstract E createTask(UserEntity userEntity);

    protected abstract R sendUpdateUserAssignedToTaskRequest(UserEntity userEntity, E taskEntity, UUID assignedToId);

    protected GraphQlTester.Errors sendUpdateUserAssignedToTaskRequestWithErrors(UserEntity userEntity, UUID taskId, UUID assignedToId) {
        DoubleRequestVariable doubleRequestVariable = new DoubleRequestVariable(idFieldName, taskId, "assignedToId", assignedToId);
        return sendRequestWithErrors(userEntity, document, doubleRequestVariable);
    }

    protected abstract GraphQlTester.Errors sendUpdateUserAssignedToTaskRequestWithErrors(UserEntity userEntity, E taskEntity, UUID assignedToId);

    protected abstract void assertTaskEntity(E taskEntity, E taskEntityAfterUpdate, UUID assignedTo);

    protected abstract void assertTaskResponse(R taskResponse, E updatedTaskEntity, UserEntity assignedTo, UserEntity userEntity);
}
