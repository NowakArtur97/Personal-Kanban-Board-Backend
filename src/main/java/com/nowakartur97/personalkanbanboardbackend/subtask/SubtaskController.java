package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskController;
import com.nowakartur97.personalkanbanboardbackend.task.TaskDTO;
import com.nowakartur97.personalkanbanboardbackend.task.TaskResponse;
import com.nowakartur97.personalkanbanboardbackend.user.UserService;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Controller
@PreAuthorize("hasAuthority('USER')")
public class SubtaskController extends BaseTaskController<SubtaskEntity, SubtaskResponse> {

    private final SubtaskService subtaskService;

    public SubtaskController(SubtaskService subtaskService, UserService userService, JWTUtil jwtUtil,
                             SubtaskMapper subtaskMapper, SubtaskValidator subtaskValidator) {
        super(subtaskService, userService, jwtUtil, subtaskMapper, subtaskValidator);
        this.subtaskService = subtaskService;
    }

    @SchemaMapping(typeName = "TaskResponse", field = "subtasks")
    public Flux<SubtaskResponse> subtasks(TaskResponse task) {
        Mono<List<SubtaskEntity>> subtasks = subtaskService.findAllByTaskId(task.getTaskId()).collectList();
        return mapToTasksResponse(subtasks);
    }

    @MutationMapping
    public Mono<SubtaskResponse> createSubtask(@Argument UUID taskId, @Argument @Valid TaskDTO subtaskDTO, DataFetchingEnvironment env) {
        return create(taskId, subtaskDTO, env);
    }

    @MutationMapping
    public Mono<SubtaskResponse> updateSubtask(@Argument UUID subtaskId, @Argument @Valid TaskDTO subtaskDTO, DataFetchingEnvironment env) {
        return update(subtaskId, subtaskDTO, env);
    }

    @MutationMapping
    public Mono<SubtaskResponse> updateUserAssignedToSubtask(@Argument UUID subtaskId, @Argument UUID assignedToId, DataFetchingEnvironment env) {
        return updateUserAssignedTo(subtaskId, assignedToId, env);
    }

    @MutationMapping
    public Mono<Void> deleteSubtask(@Argument UUID subtaskId) {
        return subtaskService.deleteById(subtaskId);
    }

    @MutationMapping
    public Mono<Void> deleteAllSubtasksByTaskId(@Argument UUID taskId) {
        return subtaskService.deleteAllByTaskId(taskId);
    }
}
