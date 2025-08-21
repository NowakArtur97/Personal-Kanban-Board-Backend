package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.common.DoubleRequestVariable;
import com.nowakartur97.personalkanbanboardbackend.common.RequestVariable;
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

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.UPDATE_SUBTASK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SubtaskUpdateMutationControllerTest extends TaskMutationTest {

    private final static String UPDATE_SUBTASK_PATH = "updateSubtask";

    public SubtaskUpdateMutationControllerTest() {
        super(UPDATE_SUBTASK_PATH, UPDATE_SUBTASK,
                new DoubleRequestVariable("subtaskDTO", new TaskDTO("title", "description", null, null, null, null),
                        "subtaskId", UUID.randomUUID()), 44);
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenUpdateSubtask_shouldReturnSubtaskResponse(UserRole role) {

        UserEntity userEntity = createUser(role);
        UserEntity author = createUser("author", "author@domain.com");
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        SubtaskEntity subtaskEntity = createSubtask(taskEntity.getTaskId(), author.getUserId());
        UserEntity assignedTo = createUser("developer", "developer@domain.com");
        TaskDTO subtaskDTO = new TaskDTO("title", "description", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, LocalDate.now(), assignedTo.getUserId());

        SubtaskResponse subtaskResponse = sendUpdateSubtaskRequest(userEntity, subtaskEntity.getSubtaskId(), subtaskDTO);

        SubtaskEntity udatedSubtaskEntity = subtaskRepository.findAll().blockLast();
        assertSubtaskEntity(udatedSubtaskEntity, subtaskDTO, author.getUserId(), userEntity.getUserId(), assignedTo.getUserId());
        assertThat(udatedSubtaskEntity.getSubtaskId()).isEqualTo(subtaskEntity.getSubtaskId());
        assertSubtaskResponse(subtaskResponse, udatedSubtaskEntity, subtaskDTO, author.getUsername(), userEntity.getUsername(), assignedTo.getUsername(),
                subtaskDTO.getStatus(), subtaskDTO.getPriority());
    }

    @Test
    public void whenUpdateAuthorsOwnSubtask_shouldReturnSubtaskResponse() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        SubtaskEntity subtaskEntity = createSubtask(taskEntity.getTaskId(), userEntity.getUserId());
        TaskDTO subtaskDTO = new TaskDTO("title", "description", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, LocalDate.now(), userEntity.getUserId());

        SubtaskResponse subtaskResponse = sendUpdateSubtaskRequest(userEntity, subtaskEntity.getSubtaskId(), subtaskDTO);

        SubtaskEntity udatedSubtaskEntity = subtaskRepository.findAll().blockLast();
        assertSubtaskEntity(udatedSubtaskEntity, subtaskDTO, userEntity.getUserId(), userEntity.getUserId(), userEntity.getUserId());
        assertThat(udatedSubtaskEntity.getSubtaskId()).isEqualTo(subtaskEntity.getSubtaskId());
        assertSubtaskResponse(subtaskResponse, udatedSubtaskEntity, subtaskDTO, userEntity.getUsername());
    }

    @Test
    public void whenUpdateSubtask_shouldUpdateSubtaskWithDefaultValuesAndReturnSubtaskResponse() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        SubtaskEntity subtaskEntity = createSubtask(taskEntity.getTaskId(), userEntity.getUserId());
        TaskDTO subtaskDTO = new TaskDTO("title", "description", null, null, null, null);

        SubtaskResponse subtaskResponse = sendUpdateSubtaskRequest(userEntity, subtaskEntity.getSubtaskId(), subtaskDTO);

        SubtaskEntity actualSubtaskEntity = subtaskRepository.findAll().blockLast();
        assertSubtaskEntity(actualSubtaskEntity, subtaskDTO, userEntity.getUserId());
        assertThat(actualSubtaskEntity.getSubtaskId()).isEqualTo(subtaskEntity.getSubtaskId());
        assertSubtaskResponse(subtaskResponse, subtaskRepository.findAll().blockFirst(), subtaskDTO, userEntity.getUsername(), userEntity.getUsername());
    }

    @Test
    public void whenUpdateNotExistingSubtask_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        UUID subtaskId = UUID.randomUUID();
        TaskDTO subtaskDTO = new TaskDTO("title", "description", null, null, null, UUID.randomUUID());
        DoubleRequestVariable doubleRequestVariable = new DoubleRequestVariable("subtaskId", subtaskId, "subtaskDTO", subtaskDTO);

        assertNotFoundErrorResponse(sendRequestWithErrors(userEntity, document, doubleRequestVariable), path, "Subtask with subtaskId: '" + subtaskId + "' not found.");
    }

    @Test
    public void whenUpdateSubtaskWithoutSubtaskId_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO subtaskDTO = new TaskDTO("title", "description", null, null, null, null);
        RequestVariable requestVariable = new RequestVariable("subtaskDTO", subtaskDTO);

        assertValidationErrorResponse(sendRequestWithErrors(userEntity, document, requestVariable), new SourceLocation(1, 25),
                "Variable 'subtaskId' has an invalid value: Variable 'subtaskId' has coerced Null value for NonNull type 'UUID!'");
    }

    private SubtaskResponse sendUpdateSubtaskRequest(UserEntity userEntity, UUID subtaskId, TaskDTO subtaskDTO) {
        DoubleRequestVariable doubleRequestVariable = new DoubleRequestVariable("subtaskId", subtaskId, "subtaskDTO", subtaskDTO);
        return (SubtaskResponse) sendRequest(userEntity, document, path, doubleRequestVariable, SubtaskResponse.class, false);
    }

    private void assertSubtaskResponse(SubtaskResponse subtaskResponse, SubtaskEntity subtaskEntity, TaskDTO subtaskDTO, String createdBy) {
        assertSubtaskResponse(subtaskResponse, subtaskEntity, subtaskDTO, createdBy, createdBy, createdBy, subtaskDTO.getStatus(), subtaskDTO.getPriority());
    }

    private void assertSubtaskResponse(SubtaskResponse subtaskResponse, SubtaskEntity subtaskEntity, TaskDTO subtaskDTO, String createdBy, String updatedBy) {
        assertSubtaskResponse(subtaskResponse, subtaskEntity, subtaskDTO, createdBy, updatedBy, updatedBy, TaskStatus.READY_TO_START, TaskPriority.LOW);
    }

    private void assertSubtaskResponse(SubtaskResponse subtaskResponse, SubtaskEntity subtaskEntity, TaskDTO subtaskDTO, String createdBy,
                                       String updatedBy, String assignedTo, TaskStatus status, TaskPriority priority) {
        assertBaseTaskResponse(subtaskResponse, subtaskEntity, subtaskDTO, createdBy, updatedBy, assignedTo, status, priority);
        assertThat(subtaskResponse.getSubtaskId()).isEqualTo(subtaskEntity.getSubtaskId());
        assertThat(subtaskResponse.getTaskId()).isEqualTo(subtaskEntity.getTaskId());
    }

    private void assertSubtaskEntity(SubtaskEntity subtaskEntity, TaskDTO subtaskDTO, UUID createdBy) {
        assertSubtaskEntity(subtaskEntity, subtaskDTO, createdBy, createdBy, createdBy, TaskStatus.READY_TO_START, TaskPriority.LOW);
    }

    private void assertSubtaskEntity(SubtaskEntity subtaskEntity, TaskDTO subtaskDTO, UUID createdBy, UUID updatedBy, UUID assignedTo) {
        assertSubtaskEntity(subtaskEntity, subtaskDTO, createdBy, updatedBy, assignedTo, subtaskDTO.getStatus(), subtaskDTO.getPriority());
    }

    private void assertSubtaskEntity(SubtaskEntity subtaskEntity, TaskDTO subtaskDTO, UUID createdBy, UUID updatedBy, UUID assignedTo,
                                     TaskStatus taskStatus, TaskPriority taskPriority) {
        assertThat(subtaskEntity).isNotNull();
        assertThat(subtaskEntity.getSubtaskId()).isEqualTo(subtaskEntity.getSubtaskId());
        assertThat(subtaskEntity.getTaskId()).isEqualTo(subtaskEntity.getTaskId());
        assertThat(subtaskEntity.getTitle()).isEqualTo(subtaskDTO.getTitle());
        assertThat(subtaskEntity.getStatus()).isEqualTo(taskStatus);
        assertThat(subtaskEntity.getPriority()).isEqualTo(taskPriority);
        assertThat(subtaskEntity.getTargetEndDate()).isEqualTo(subtaskDTO.getTargetEndDate());
        assertThat(subtaskEntity.getAssignedTo()).isEqualTo(assignedTo);
        assertThat(subtaskEntity.getCreatedOn()).isEqualTo(subtaskEntity.getCreatedOn());
        assertThat(subtaskEntity.getCreatedBy()).isEqualTo(createdBy);
        assertThat(subtaskEntity.getUpdatedOn()).isNotNull();
        assertThat(subtaskEntity.getUpdatedBy()).isEqualTo(updatedBy);
    }

    @Override
    protected GraphQlTester.Errors sendTaskRequestWithErrors(UserEntity userEntity, TaskDTO subtaskDTO) {
        UUID taskId = createTask(userEntity.getUserId()).getTaskId();
        UUID subtaskId = createSubtask(taskId, userEntity.getUserId()).getSubtaskId();
        RequestVariable reqVariable = new DoubleRequestVariable(requestVariable.getName(), subtaskDTO, "subtaskId", subtaskId);
        return sendRequestWithErrors(userEntity, document, reqVariable);
    }
}
