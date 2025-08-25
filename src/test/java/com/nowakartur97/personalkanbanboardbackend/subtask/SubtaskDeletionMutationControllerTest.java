package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskDeletionMutationControllerTest;
import com.nowakartur97.personalkanbanboardbackend.common.RequestVariable;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;

import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.DELETE_SUBTASK;

public class SubtaskDeletionMutationControllerTest extends BaseTaskDeletionMutationControllerTest {

    private final static String DELETE_SUBTASK_PATH = "deleteSubtask";

    public SubtaskDeletionMutationControllerTest() {
        super(DELETE_SUBTASK_PATH, DELETE_SUBTASK, new RequestVariable("subtaskId", UUID.randomUUID()), 25);
    }

    @Override
    protected void sendDeleteTaskRequest(UserEntity userEntity, UUID subtaskId) {
        RequestVariable reqVariable = new RequestVariable("subtaskId", subtaskId);
        sendRequest(userEntity, document, path, reqVariable, null, false);
    }
}
