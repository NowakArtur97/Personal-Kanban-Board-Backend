package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskController;
import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskEvent;
import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskValidator;
import com.nowakartur97.personalkanbanboardbackend.user.UserService;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Controller
@PreAuthorize("hasAuthority('USER')")
@Slf4j
public class TaskController extends BaseTaskController<TaskEntity, TaskResponse> {

    private final TaskService taskService;

    public TaskController(TaskService taskService, UserService userService, JWTUtil jwtUtil,
                          TaskMapper taskMapper, BaseTaskValidator baseTaskValidator) {
        super(taskService, userService, jwtUtil, taskMapper, baseTaskValidator);
        this.taskService = taskService;
    }

    @QueryMapping
    public Flux<TaskResponse> tasks() {
        Mono<List<TaskEntity>> allTasks = taskService.findAll().collectList();
        return mapToTasksResponse(allTasks);
    }

    @QueryMapping
    public Flux<TaskResponse> tasksAssignedTo(@Argument UUID assignedToId) {
        Mono<List<TaskEntity>> assignedToUserTasks = taskService.findAllByAssignedTo(assignedToId).collectList();
        return mapToTasksResponse(assignedToUserTasks);
    }

    @MutationMapping
    public Mono<TaskResponse> createTask(@Argument @Valid TaskDTO taskDTO, DataFetchingEnvironment env) {
        return create(null, taskDTO, env);
    }

    @MutationMapping
    // TODO: Add subtasks to response?
    public Mono<TaskResponse> updateTask(@Argument UUID taskId, @Argument @Valid TaskDTO taskDTO, DataFetchingEnvironment env) {
        return update(taskId, taskDTO, env);
    }

    @MutationMapping
    // TODO: Add subtasks to response?
    public Mono<TaskResponse> updateUserAssignedToTask(@Argument UUID taskId, @Argument UUID assignedToId, DataFetchingEnvironment env) {
        return updateUserAssignedTo(taskId, assignedToId, env);
    }

    @MutationMapping
    public Mono<Void> deleteTask(@Argument UUID taskId) {
        return deleteById(taskId);
    }

    @MutationMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Mono<Void> deleteAllTasks() {
        return taskService.deleteAll();
    }

    @SubscriptionMapping
    public Flux<BaseTaskEvent<TaskResponse>> taskEvent() {
        return sink.asFlux();
    }
}
