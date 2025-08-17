package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.BasicIntegrationTest;
import com.nowakartur97.personalkanbanboardbackend.subtask.SubtaskEntity;
import com.nowakartur97.personalkanbanboardbackend.subtask.SubtaskResponse;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.GET_TASKS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TasksQueryControllerTest extends BasicIntegrationTest {

    private final static String TASKS_PATH = "tasks";

    public TasksQueryControllerTest() {
        super(TASKS_PATH, GET_TASKS, null);
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenGetAllTasks_shouldReturnAllTasks(UserRole role) {

        UserEntity userEntity = createUser(role);
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        UserEntity author = createUser("developer2", "developer2@domain.com");
        UserEntity assignedTo = createUser("developer3", "developer3@domain.com");
        UserEntity updatedBy = createUser("developer4", "developer4@domain.com");
        TaskEntity taskEntity2 = createTask(author.getUserId(), assignedTo.getUserId(), updatedBy.getUserId());

        List<TaskResponse> taskResponses = sendGetTasksRequest(userEntity);

        assertThat(taskResponses.size()).isEqualTo(2);
        TaskResponse taskResponse = taskResponses.getFirst();
        assertTaskResponse(taskResponse, taskEntity, userEntity.getUsername(), userEntity.getUsername(), null);
        TaskResponse taskResponse2 = taskResponses.getLast();
        assertTaskResponse(taskResponse2, taskEntity2, author.getUsername(), assignedTo.getUsername(), updatedBy.getUsername());
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenGetAllTasksWithSubtasks_shouldReturnAllTasksWithSubtasks(UserRole role) {

        UserEntity userEntity = createUser(role);
        TaskEntity taskEntity = createTask(userEntity.getUserId());
        SubtaskEntity subtaskEntity = createSubtask(taskEntity.getTaskId(), userEntity.getUserId());
        UserEntity author = createUser("developer2", "developer2@domain.com");
        UserEntity assignedTo = createUser("developer3", "developer3@domain.com");
        UserEntity updatedBy = createUser("developer4", "developer4@domain.com");
        TaskEntity taskEntity2 = createTask(author.getUserId(), assignedTo.getUserId(), updatedBy.getUserId());
        SubtaskEntity subtaskEntity2 = createSubtask(taskEntity2.getTaskId(), author.getUserId(), assignedTo.getUserId(), updatedBy.getUserId());

        List<TaskResponse> taskResponses = sendGetTasksRequest(userEntity);

        assertThat(taskResponses.size()).isEqualTo(2);
        TaskResponse taskResponse = taskResponses.getFirst();
        assertTaskResponse(taskResponse, taskEntity, userEntity.getUsername(), userEntity.getUsername(), null);
        SubtaskResponse subtaskResponse = taskResponse.getSubtasks().getFirst();
        assertSubtaskResponse(subtaskResponse, subtaskEntity, userEntity.getUsername(), userEntity.getUsername(), null);
        TaskResponse taskResponse2 = taskResponses.getLast();
        assertTaskResponse(taskResponse2, taskEntity2, author.getUsername(), assignedTo.getUsername(), updatedBy.getUsername());
        SubtaskResponse subtaskResponse2 = taskResponse2.getSubtasks().getFirst();
        assertSubtaskResponse(subtaskResponse2, subtaskEntity2, author.getUsername(), assignedTo.getUsername(), updatedBy.getUsername());
    }

    private List<TaskResponse> sendGetTasksRequest(UserEntity userEntity) {
        return httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(GET_TASKS)
                .execute()
                .errors()
                .verify()
                .path(TASKS_PATH)
                .entityList(TaskResponse.class)
                .get();
    }
}
