package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.common.request.RequestVariable;
import com.nowakartur97.personalkanbanboardbackend.common.test.BaseTaskDeletionMutationControllerTest;
import com.nowakartur97.personalkanbanboardbackend.task.TaskEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;

import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.common.test.GraphQLQueries.DELETE_SUBTASK;
import static com.nowakartur97.personalkanbanboardbackend.common.test.GraphQLQueries.SUBTASK_EVENT;

public class SubtaskDeletionMutationControllerTest extends BaseTaskDeletionMutationControllerTest<SubtaskEntity> {

    public SubtaskDeletionMutationControllerTest() {
        super("deleteSubtask", DELETE_SUBTASK, new RequestVariable("subtaskId", UUID.randomUUID()), 25,
                SUBTASK_EVENT, "subtaskEvent", SubtaskEvent.class);
    }

    @Override
    protected SubtaskEntity createTask(UserEntity userEntity) {
        TaskEntity taskEntity = createTask(userEntity.getUserId(), userEntity.getUserId(), userEntity.getUserId());
        return createSubtask(taskEntity.getTaskId(), userEntity.getUserId(), userEntity.getUserId(), userEntity.getUserId());
    }

    @Override
    protected void sendDeleteTaskRequest(UserEntity userEntity, UUID subtaskId) {
        RequestVariable reqVariable = new RequestVariable("subtaskId", subtaskId);
        sendRequest(userEntity, document, path, reqVariable, null, false);
    }
}
