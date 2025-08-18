package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.common.DoubleRequestVariable;
import com.nowakartur97.personalkanbanboardbackend.common.TaskMutationTest;
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
import org.springframework.graphql.test.tester.GraphQlTester;

import java.time.LocalDate;
import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.CREATE_SUBTASK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SubtaskCreationMutationControllerTest extends TaskMutationTest {

    private final static String CREATE_SUBTASK_PATH = "createSubtask";

    public SubtaskCreationMutationControllerTest() {
        super(CREATE_SUBTASK_PATH, CREATE_SUBTASK,
                new DoubleRequestVariable("taskId", UUID.randomUUID(), "subtaskDTO", new TaskDTO("title", "description", null, null, null, null)),
                "subtaskDTO", 41);
    }

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

        GraphQlTester.Errors errors = httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(CREATE_SUBTASK)
                .variable("taskId", taskId)
                .variable("subtaskDTO", subtaskDTO)
                .execute()
                .errors();
        assertNotFoundErrorResponse(errors, path, "Task with taskId: '" + taskId + "' not found.");
    }

    @Test
    public void whenCreateSubtaskWithoutTaskId_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO subtaskDTO = new TaskDTO("title", "description", null, null, null, null);

        GraphQlTester.Errors errors = httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(CREATE_SUBTASK)
                .variable("taskDTO", subtaskDTO)
                .execute()
                .errors();
        assertValidationErrorResponse(errors, new SourceLocation(1, 25),
                "Variable 'taskId' has an invalid value: Variable 'taskId' has coerced Null value for NonNull type 'UUID!'");
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
}
