package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.sst.SST;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.execution.ErrorType;

import java.time.LocalDate;
import java.util.List;

import static com.nowakartur97.personalkanbanboardbackend.sst.GraphQLQueries.GET_TASKS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TaskControllerTest extends SST {

    private final static String TASKS_PATH = "tasks";

    @Test
    public void whenGetTasksByUsername_shouldReturnTasksForSpecifiedUser() {

        UserEntity userEntity = createUser();
        TaskEntity taskEntity = createTask(userEntity);

        List<TaskResponse> taskResponses = httpGraphQlTester
                .document(GET_TASKS)
                .variable("username", userEntity.getUsername())
                .execute()
                .errors()
                .verify()
                .path(TASKS_PATH)
                .entityList(TaskResponse.class)
                .get();

        assertThat(taskResponses.size()).isOne();
        TaskResponse taskResponse = taskResponses.getFirst();
        assertTask(taskResponse, taskEntity, userEntity.getUsername());
    }

    @Test
    public void whenGetTasksByNotExistingUsername_shouldReturnGraphQLErrorResponse() {

        httpGraphQlTester
                .document(GET_TASKS)
                .variable("username", "notExistingUser")
                .execute()
                .errors()
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertThat(responseError.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
                            assertThat(responseError.getMessage()).isEqualTo("User with username: notExistingUser not found.");
                            assertThat(responseError.getPath()).isEqualTo("tasks");
                            assertThat(responseError.getLocations()).isEqualTo(List.of(new SourceLocation(2, 3)));
                        });
    }

    private void assertTask(TaskResponse taskResponse, TaskEntity taskEntity, String username) {
        assertThat(taskResponse.taskId()).isEqualTo(taskEntity.getTaskId());
        assertThat(taskResponse.title()).isEqualTo(taskEntity.getTitle());
        assertThat(taskResponse.status()).isEqualTo(taskEntity.getStatus());
        assertThat(taskResponse.priority()).isEqualTo(taskEntity.getPriority());
        assertThat(taskResponse.targetEndDate()).isEqualTo(taskEntity.getTargetEndDate());
        assertThat(taskResponse.assignedTo()).isEqualTo(username);
        assertThat(taskResponse.createdOn()).isEqualTo(taskEntity.getCreatedOn());
        assertThat(taskResponse.createdBy()).isEqualTo(username);
        assertThat(taskResponse.updatedOn()).isEqualTo(taskEntity.getUpdatedOn());
        assertThat(taskResponse.updatedBy()).isEqualTo(username);
    }

    private UserEntity createUser() {
        UserEntity user = new UserEntity();
        user.setUsername("testUser");
        user.setPassword("pass1");
        user.setEmail("testUser@domain.com");

        return userRepository.save(user).block();
    }

    private TaskEntity createTask(UserEntity user) {
        TaskEntity task = new TaskEntity();
        task.setTitle("task1");
        task.setDescription("desc1");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setPriority(TaskPriority.MEDIUM);
        task.setTargetEndDate(LocalDate.now());
        task.setAssignedTo(user.getUserId());
        task.setCreatedOn(LocalDate.now());
        task.setCreatedBy(user.getUserId());
        task.setUpdatedOn(LocalDate.now());
        task.setUpdatedBy(user.getUserId());

        return taskRepository.save(task).block();
    }
}
