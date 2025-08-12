package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
import com.nowakartur97.personalkanbanboardbackend.task.TaskDTO;
import com.nowakartur97.personalkanbanboardbackend.task.TaskService;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserService;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.nowakartur97.personalkanbanboardbackend.auth.AuthorizationHeaderInterceptor.TOKEN_IN_CONTEXT;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('USER')")
public class SubtaskController {

    private final SubtaskService subtaskService;
    private final TaskService taskService;
    private final UserService userService;
    private final JWTUtil jwtUtil;
    private final SubtaskMapper subtaskMapper;
    private final SubtaskValidator subtaskValidator;

    @MutationMapping
    public Mono<SubtaskResponse> createSubtask(@Argument UUID taskId, @Argument @Valid TaskDTO subtaskDTO, DataFetchingEnvironment env) {
        String username = jwtUtil.extractUsername(env.getGraphQlContext().get(TOKEN_IN_CONTEXT));
        Mono<UserEntity> createdBy = userService.findByUsername(username);
        if (subtaskDTO.getAssignedTo() == null) {
            return subtaskValidator.validate(taskId)
                    .flatMap(__ -> createdBy)
                    .flatMap(user -> Mono.just(subtaskMapper.mapToEntity(taskId, subtaskDTO, user.getUserId()))
                            .flatMap(subtaskService::save)
                            .map(subtask -> subtaskMapper.mapToResponse(subtask, user.getUsername())));
        }
        Mono<UserEntity> assignedTo = userService.findById(subtaskDTO.getAssignedTo());
        return subtaskValidator.validate(taskId)
                .flatMap(__ -> Mono.zip(createdBy, assignedTo))
                .flatMap(tuple -> Mono.just(subtaskMapper.mapToEntity(taskId, subtaskDTO, tuple.getT1().getUserId(), tuple.getT2().getUserId()))
                        .flatMap(subtaskService::save)
                        .map(subtask -> subtaskMapper.mapToResponse(subtask, tuple.getT1().getUsername(), tuple.getT2().getUsername())));
    }

    @MutationMapping
    public Mono<Void> deleteSubtask(@Argument UUID subtaskId) {
        return subtaskService.deleteById(subtaskId);
    }
}
