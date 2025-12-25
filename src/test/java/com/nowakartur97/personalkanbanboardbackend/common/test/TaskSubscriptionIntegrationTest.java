package com.nowakartur97.personalkanbanboardbackend.common.test;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskEntity;
import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskEvent;
import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskResponse;
import com.nowakartur97.personalkanbanboardbackend.common.request.RequestVariable;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import reactor.core.publisher.Flux;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class TaskSubscriptionIntegrationTest<E extends BaseTaskEntity> extends TaskIntegrationTest<E> {

    protected final String subscriptionDocument;
    protected final String subscriptionPath;
    protected final Class<?> subscriptionEntityType;

    public TaskSubscriptionIntegrationTest(String path, String document, RequestVariable requestVariable,
                                           String subscriptionDocument, String subscriptionPath, Class<?> subscriptionEntityType) {
        super(path, document, requestVariable);
        this.subscriptionDocument = subscriptionDocument;
        this.subscriptionPath = subscriptionPath;
        this.subscriptionEntityType = subscriptionEntityType;
    }

    protected Flux<BaseTaskEvent> createEventFlux(UserEntity userEntity) {
        return createWebSocketGraphQlTester(userEntity)
                .document(subscriptionDocument)
                .executeSubscription().toFlux()
                .map(r -> (BaseTaskEvent) r.path(subscriptionPath).entity(subscriptionEntityType).get());
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
