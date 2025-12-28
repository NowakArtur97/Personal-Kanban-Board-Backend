package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskEvent;
import com.nowakartur97.personalkanbanboardbackend.common.TaskEventType;
import com.nowakartur97.personalkanbanboardbackend.common.request.RequestVariable;
import com.nowakartur97.personalkanbanboardbackend.common.test.TaskSubscriptionIntegrationTest;
import com.nowakartur97.personalkanbanboardbackend.task.TaskEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.common.test.GraphQLQueries.DELETE_ALL_SUBTASKS_BY_TASK_ID;
import static com.nowakartur97.personalkanbanboardbackend.common.test.GraphQLQueries.SUBTASK_EVENT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AllSubtasksDeletionByTaskIdMutationControllerTest extends TaskSubscriptionIntegrationTest<SubtaskEntity> {

    public AllSubtasksDeletionByTaskIdMutationControllerTest() {
        super("deleteAllSubtasksByTaskId", DELETE_ALL_SUBTASKS_BY_TASK_ID,
                new RequestVariable("taskId", UUID.randomUUID()),
                SUBTASK_EVENT, "subtaskEvent", SubtaskEvent.class);
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenDeleteAllSubtasksByTaskId_shouldReturnEmptyResponse(UserRole role) {

        UserEntity userEntity = createUser(role);
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        createSubtask(taskEntity.getTaskId(), userEntity.getUserId());
        createSubtask(taskEntity.getTaskId(), userEntity.getUserId());

        assertAllTaskSubtasksDeletionAndSubscription(userEntity, taskEntity.getId(), 1L);
    }

    @Test
    public void whenDeleteAllSubtasksByTaskIdForNotExistingTask_shouldReturnEmptyResponse() {

        UserEntity userEntity = createUser();

        assertAllTaskSubtasksDeletionAndSubscription(userEntity, UUID.randomUUID(), 0L);
    }

    @Test
    public void whenDeleteAllSubtasksByTaskIdWithoutTaskId_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();

        assertValidationErrorResponse(sendRequestWithErrors(userEntity, document, null), new SourceLocation(1, 41),
                "Variable 'taskId' has an invalid value: Variable 'taskId' has coerced Null value for NonNull type 'UUID!'");
    }

    private void sendDeleteAllSubtasksByTaskIdRequest(UserEntity userEntity, UUID taskId) {
        RequestVariable reqVariable = new RequestVariable("taskId", taskId);
        sendRequest(userEntity, document, path, reqVariable, null, false);
    }

    protected void assertAllTaskSubtasksDeletionAndSubscription(UserEntity userEntity, UUID taskId,
                                                                Long expectedNumberOfTasks) {
        Flux<BaseTaskEvent> eventFlux = createEventFlux(userEntity);

        Mono<UUID> mutationMono = Mono.fromCallable(() -> {
            sendDeleteAllSubtasksByTaskIdRequest(userEntity, taskId);
            return taskId;
        });

        Flux<Tuple3<Long, Long, BaseTaskEvent>> combined = eventFlux
                .take(1)
                .zipWith(mutationMono)
                .flatMap(tuple ->
                        subtaskRepository.count()
                                .zipWith(taskRepository.count())
                                .map(count -> Tuples.of(count.getT1(), count.getT2(), tuple.getT1()))
                );
        StepVerifier.create(combined)
                .assertNext(tuple -> {
                    assertThat(tuple.getT1()).isZero();
                    assertThat(tuple.getT2()).isEqualTo(expectedNumberOfTasks);
                    assertThat(tuple.getT3().getTaskEventType()).isEqualTo(TaskEventType.DELETE_ALL_SUBTASKS_FOR_TASK);
                    assertThat(tuple.getT3().getTaskId()).isEqualTo(taskId);
                })
                .thenCancel()
                .verify(Duration.ofSeconds(5));
    }
}
