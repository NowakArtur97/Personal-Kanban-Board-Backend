package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskEvent;
import com.nowakartur97.personalkanbanboardbackend.common.TaskEventType;
import com.nowakartur97.personalkanbanboardbackend.common.test.TaskSubscriptionIntegrationTest;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.common.test.GraphQLQueries.DELETE_ALL_TASKS;
import static com.nowakartur97.personalkanbanboardbackend.common.test.GraphQLQueries.TASK_EVENT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AllTasksDeletionMutationControllerTest extends TaskSubscriptionIntegrationTest<TaskEntity> {

    public AllTasksDeletionMutationControllerTest() {
        super("deleteAllTasks", DELETE_ALL_TASKS, null,
                TASK_EVENT, "taskEvent", TaskEvent.class);
    }

    @Test
    public void whenDeleteAllTasksByAdmin_shouldReturnEmptyResponse() {

        UserEntity admin = createUser("admin", "admin@domain.com", UserRole.ADMIN);
        UserEntity userEntity = createUser("developer", "developer@domain.com");
        createTask(admin.getUserId());
        createTask(userEntity.getUserId());

        assertAllTasksDeletionAndSubscription(admin);
    }

    @Test
    public void whenDeleteAllTasksWhenThereAreNoExistingTasks_shouldReturnEmptyResponse() {

        UserEntity admin = createUser("testAdmin", "testAdmin@domain.com", UserRole.ADMIN);

        assertAllTasksDeletionAndSubscription(admin);
    }

    @Test
    public void whenDeleteAllTasksByNonAdminUser_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();

        asserForbiddenErrorResponse(sendRequestWithErrors(userEntity, document, null), "deleteAllTasks");
    }

    private void sendDeleteAllTasksRequest(UserEntity userEntity) {
        sendRequest(userEntity, document, path, null, null, false);
    }

    private void assertAllTasksDeletionAndSubscription(UserEntity userEntity) {
        Flux<BaseTaskEvent> eventFlux = createEventFlux(userEntity);

        Mono<UUID> mutationMono = Mono.fromCallable(() -> {
            sendDeleteAllTasksRequest(userEntity);
            return UUID.randomUUID();
        });

        Flux<Tuple2<Long, BaseTaskEvent>> combined = eventFlux
                .take(1)
                .zipWith(mutationMono)
                .flatMap(tuple ->
                        repository.count()
                                .map(count -> Tuples.of(count, tuple.getT1()))
                );
        StepVerifier.create(combined)
                .assertNext(tuple -> {
                    assertThat(tuple.getT1()).isZero();
                    assertThat(tuple.getT2().getTaskEventType()).isEqualTo(TaskEventType.DELETE_ALL);
                })
                .thenCancel()
                .verify(Duration.ofSeconds(5));
    }
}
