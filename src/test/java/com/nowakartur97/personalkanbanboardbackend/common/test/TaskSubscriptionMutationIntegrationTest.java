package com.nowakartur97.personalkanbanboardbackend.common.test;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskEntity;
import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskEvent;
import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskResponse;
import com.nowakartur97.personalkanbanboardbackend.common.TaskEventType;
import com.nowakartur97.personalkanbanboardbackend.common.request.RequestVariable;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import org.apache.logging.log4j.util.TriConsumer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.time.Duration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class TaskSubscriptionMutationIntegrationTest<E extends BaseTaskEntity, R extends BaseTaskResponse> extends TaskSubscriptionIntegrationTest<E> {

    public TaskSubscriptionMutationIntegrationTest(String path, String document, RequestVariable requestVariable,
                                                   String subscriptionDocument, String subscriptionPath, Class<?> subscriptionEntityType) {
        super(path, document, requestVariable, subscriptionDocument, subscriptionPath, subscriptionEntityType);
    }

    protected void assertTaskMutationAndSubscription(UserEntity userEntity, R request, TaskEventType taskEventType,
                                                     TriConsumer<E, R, BaseTaskEvent> assertions) {
        Flux<BaseTaskEvent> eventFlux = createEventFlux(userEntity);

        Mono<R> mutationMono = Mono.fromCallable(() -> request);

        Flux<Tuple3<E, R, BaseTaskEvent>> combined = eventFlux
                .take(1)
                .zipWith(mutationMono)
                .flatMap(tuple ->
                        repository.findById(tuple.getT2().getId())
                                .map(entity -> Tuples.of(entity, tuple.getT2(), tuple.getT1()))
                );
        StepVerifier.create(combined)
                .assertNext(tuple -> {
                    E taskEntity = tuple.getT1();
                    R taskResponse = tuple.getT2();
                    BaseTaskEvent baseTaskEvent = tuple.getT3();
                    assertThat(baseTaskEvent.getTaskEventType()).isEqualTo(taskEventType);
                    assertions.accept(taskEntity, taskResponse, baseTaskEvent);
                })
                .thenCancel()
                .verify(Duration.ofSeconds(5));
    }
}
