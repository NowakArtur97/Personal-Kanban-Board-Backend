package com.nowakartur97.personalkanbanboardbackend.common;

import com.nowakartur97.personalkanbanboardbackend.task.TaskDTO;
import com.nowakartur97.personalkanbanboardbackend.task.TaskPriority;
import com.nowakartur97.personalkanbanboardbackend.task.TaskStatus;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import graphql.language.SourceLocation;
import org.apache.logging.log4j.util.TriConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class TaskMutationTest<E extends BaseTaskEntity, R extends BaseTaskResponse> extends TaskSubscriptionIntegrationTest<E> {

    private final int validationErrorSourceLocationColumn;
    @Autowired
    public TaskEventPublisher<R> taskEventPublisher;

    protected TaskMutationTest(String path, String document, RequestVariable requestVariable, int validationErrorSourceLocationColumn,
                               String subscriptionDocument, String subscriptionPath, Class<?> subscriptionEntityType) {
        super(path, document, requestVariable, subscriptionDocument, subscriptionPath, subscriptionEntityType);
        this.validationErrorSourceLocationColumn = validationErrorSourceLocationColumn;
    }

    @BeforeEach
    public void resetSink() {
        // TODO: Remove?
        taskEventPublisher.resetSink();
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

        assertValidationErrorResponse(sendTaskRequestWithErrors(userEntity, null), new SourceLocation(1, validationErrorSourceLocationColumn),
                "Variable '" + requestVariable.getName() + "' has an invalid value: Variable '" + requestVariable.getName() + "' has coerced Null value for NonNull type 'TaskDTO!'");
    }

    @Test
    public void whenMutateTaskWithoutTitle_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO(null, null, null, null, null, null);

        assertValidationErrorResponse(sendTaskRequestWithErrors(userEntity, taskDTO), new SourceLocation(1, validationErrorSourceLocationColumn),
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

    protected void assertTaskMutationAndSubscription(UserEntity userEntity, R request, TaskEventType taskEventType, TriConsumer<E, R, BaseTaskEvent<R>> assertions) {
        Flux<BaseTaskEvent<R>> eventFlux = createWebSocketGraphQlTester(userEntity)
                .document(subscriptionDocument)
                .executeSubscription().toFlux()
                .map(r -> (BaseTaskEvent<R>) r.path(subscriptionPath).entity(subscriptionEntityType).get());

        Mono<R> mutationMono = Mono.fromCallable(() -> request);

        Flux<Tuple3<BaseTaskEvent<R>, R, E>> combined = eventFlux
                .take(1)
                .zipWith(mutationMono)
                .flatMap(tuple ->
                        repository.findById(tuple.getT2().getId())
                                .map(entity -> Tuples.of(tuple.getT1(), tuple.getT2(), entity))
                );
        StepVerifier.create(combined)
                .assertNext(tuple -> {
                    BaseTaskEvent<R> taskEvent = tuple.getT1();
                    R taskResponse = tuple.getT2();
                    E updatedTaskEntity = tuple.getT3();
                    assertThat(taskEvent.getTaskEventType()).isEqualTo(taskEventType);
                    assertions.accept(updatedTaskEntity, taskResponse, taskEvent);
                })
                .thenCancel()
                .verify(Duration.ofSeconds(5));
    }

    protected abstract GraphQlTester.Errors sendTaskRequestWithErrors(UserEntity userEntity, TaskDTO taskDTO);

    protected void assertBaseTaskEntity(BaseTaskEntity taskEntity, TaskDTO taskDTO, UUID createdBy, UUID assignedTo) {
        assertThat(taskEntity).isNotNull();
        assertThat(taskEntity.getTitle()).isEqualTo(taskDTO.getTitle());
        assertThat(taskEntity.getStatus()).isEqualTo(taskDTO.getStatus() != null ? taskDTO.getStatus() : TaskStatus.READY_TO_START);
        assertThat(taskEntity.getPriority()).isEqualTo(taskDTO.getPriority() != null ? taskDTO.getPriority() : TaskPriority.LOW);
        assertThat(taskEntity.getTargetEndDate()).isEqualTo(taskDTO.getTargetEndDate());
        assertThat(taskEntity.getAssignedTo()).isEqualTo(assignedTo);
        assertThat(taskEntity.getCreatedOn()).isNotNull();
        assertThat(taskEntity.getCreatedBy()).isEqualTo(createdBy);
        assertThat(taskEntity.getUpdatedOn()).isNull();
        assertThat(taskEntity.getUpdatedBy()).isNull();
    }

    protected void assertBaseTaskResponse(BaseTaskResponse taskResponse, TaskDTO taskDTO, String createdBy, String assignedTo) {
        assertThat(taskResponse).isNotNull();
        assertThat(taskResponse.getTaskId()).isNotNull();
        assertThat(taskResponse.getTitle()).isEqualTo(taskDTO.getTitle());
        assertThat(taskResponse.getStatus()).isEqualTo(taskDTO.getStatus() != null ? taskDTO.getStatus() : TaskStatus.READY_TO_START);
        assertThat(taskResponse.getPriority()).isEqualTo(taskDTO.getPriority() != null ? taskDTO.getPriority() : TaskPriority.LOW);
        assertThat(taskResponse.getTargetEndDate()).isEqualTo(taskDTO.getTargetEndDate());
        assertThat(taskResponse.getAssignedTo()).isEqualTo(assignedTo);
        assertThat(taskResponse.getCreatedOn()).isNotNull();
        assertThat(taskResponse.getCreatedBy()).isEqualTo(createdBy);
        assertThat(taskResponse.getUpdatedOn()).isNull();
        assertThat(taskResponse.getUpdatedBy()).isNull();
    }

    protected void assertBaseTaskResponse(BaseTaskResponse taskResponse, BaseTaskEntity taskEntity, TaskDTO taskDTO,
                                          String createdBy, String updatedBy, String assignedTo) {
        assertThat(taskResponse).isNotNull();
        assertThat(taskResponse.getTitle()).isEqualTo(taskDTO.getTitle());
        assertThat(taskResponse.getStatus()).isEqualTo(taskDTO.getStatus() != null ? taskDTO.getStatus() : TaskStatus.READY_TO_START);
        assertThat(taskResponse.getPriority()).isEqualTo(taskDTO.getPriority() != null ? taskDTO.getPriority() : TaskPriority.LOW);
        assertThat(taskResponse.getTargetEndDate()).isEqualTo(taskDTO.getTargetEndDate());
        assertThat(taskResponse.getAssignedTo()).isEqualTo(assignedTo);
        assertThat(Instant.parse(taskResponse.getCreatedOn()).toEpochMilli()).isEqualTo(taskEntity.getCreatedOn().toEpochMilli());
        assertThat(taskResponse.getCreatedBy()).isEqualTo(createdBy);
        assertThat(Instant.parse(taskResponse.getUpdatedOn()).toEpochMilli()).isEqualTo(taskEntity.getUpdatedOn().toEpochMilli());
        assertThat(taskResponse.getUpdatedBy()).isEqualTo(updatedBy);
    }

    protected void assertBaseTaskResponse(BaseTaskResponse mutationTaskResponse, BaseTaskResponse subscriptionTaskResponse) {
        assertThat(subscriptionTaskResponse).isNotNull();
        assertThat(subscriptionTaskResponse.getTaskId()).isEqualTo(mutationTaskResponse.getTaskId());
        assertThat(subscriptionTaskResponse.getTitle()).isEqualTo(mutationTaskResponse.getTitle());
        assertThat(subscriptionTaskResponse.getStatus()).isEqualTo(mutationTaskResponse.getStatus());
        assertThat(subscriptionTaskResponse.getPriority()).isEqualTo(mutationTaskResponse.getPriority());
        assertThat(subscriptionTaskResponse.getTargetEndDate()).isEqualTo(mutationTaskResponse.getTargetEndDate());
        assertThat(subscriptionTaskResponse.getAssignedTo()).isEqualTo(mutationTaskResponse.getAssignedTo());
        assertThat(Instant.parse(subscriptionTaskResponse.getCreatedOn()).toEpochMilli()).isEqualTo(Instant.parse(mutationTaskResponse.getCreatedOn()).toEpochMilli());
        assertThat(subscriptionTaskResponse.getCreatedBy()).isEqualTo(mutationTaskResponse.getCreatedBy());
        if (subscriptionTaskResponse.getUpdatedBy() != null) {
            assertThat(Instant.parse(subscriptionTaskResponse.getUpdatedOn()).toEpochMilli()).isEqualTo(Instant.parse(mutationTaskResponse.getUpdatedOn()).toEpochMilli());
        } else {
            assertThat(subscriptionTaskResponse.getUpdatedOn()).isNull();
        }
        assertThat(subscriptionTaskResponse.getUpdatedBy()).isEqualTo(subscriptionTaskResponse.getUpdatedBy());
    }
}
