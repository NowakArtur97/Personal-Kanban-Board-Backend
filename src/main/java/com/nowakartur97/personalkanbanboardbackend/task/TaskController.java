package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserService;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.nowakartur97.personalkanbanboardbackend.auth.AuthorizationHeaderInterceptor.TOKEN_IN_CONTEXT;

@Controller
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;
    private final JWTUtil jwtUtil;
    private final TaskMapper taskMapper;

    @QueryMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public Flux<TaskResponse> tasks(DataFetchingEnvironment env) {
        Mono<List<TaskEntity>> allTasks = taskService.findAll().collectList();
        return allTasks
                .map(tasks -> Stream.of(
                                getUuidsFromTasksByProperty(tasks, TaskEntity::getCreatedBy),
                                getUuidsFromTasksByProperty(tasks, TaskEntity::getUpdatedBy),
                                getUuidsFromTasksByProperty(tasks, TaskEntity::getAssignedTo))
                        .flatMap(Collection::stream)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()))
                .flatMap(userIds -> userService.findAllByIds(userIds.stream().toList()).collectList())
                .zipWith(allTasks)
                .flatMapIterable(tuple -> tuple.getT2().stream()
                        .map(task -> taskMapper.mapToResponse(task, tuple.getT1()))
                        .toList());
    }

    @MutationMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public Mono<TaskResponse> createTask(@Argument @Valid TaskDTO taskDTO, DataFetchingEnvironment env) {
        String username = jwtUtil.extractUsername(env.getGraphQlContext().get(TOKEN_IN_CONTEXT));
        Mono<UserEntity> author = userService.findByUsername(username);
        if (taskDTO.getAssignedTo() == null) {
            return author
                    .flatMap(user -> Mono.just(taskMapper.mapToEntity(taskDTO, user.getUserId()))
                            .flatMap(taskService::save)
                            .map(task -> taskMapper.mapToResponse(task, user.getUsername())));
        }
        Mono<UserEntity> assignedTo = userService.findById(taskDTO.getAssignedTo());
        return Mono.zip(author, assignedTo)
                .flatMap(tuple -> Mono.just(taskMapper.mapToEntity(taskDTO, tuple.getT1().getUserId(), tuple.getT2().getUserId()))
                        .flatMap(taskService::save)
                        .map(task -> taskMapper.mapToResponse(task, tuple.getT1().getUsername(), tuple.getT2().getUsername())));
    }

    @MutationMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public Mono<TaskResponse> updateTask(@Argument UUID taskId, @Argument @Valid TaskDTO taskDTO, DataFetchingEnvironment env) {
        String username = jwtUtil.extractUsername(env.getGraphQlContext().get(TOKEN_IN_CONTEXT));
        Mono<TaskEntity> taskById = taskService.findById(taskId);
        Mono<UserEntity> author = userService.findByUsername(username);
        if (taskDTO.getAssignedTo() == null) {
            return Mono.zip(taskById, author)
                    .flatMap(tuple -> Mono.just(taskMapper.updateEntity(tuple.getT1(), taskDTO, tuple.getT2().getUserId()))
                            .flatMap(taskService::update)
                            .map(task -> taskMapper.mapToResponse(task, username, username, tuple.getT2().getUsername())));
        }
        Mono<UserEntity> assignedTo = userService.findById(taskDTO.getAssignedTo());
        return Mono.zip(taskById, author, assignedTo)
                .flatMap(tuple -> Mono.just(taskMapper.updateEntity(tuple.getT1(), taskDTO, tuple.getT2().getUserId(), tuple.getT3().getUserId()))
                        .flatMap(taskService::update)
                        .map(task -> taskMapper.mapToResponse(task, tuple.getT2().getUsername(), username, tuple.getT3().getUsername())));
    }

    @MutationMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public Mono<Void> deleteTask(@Argument UUID taskId) {
        return taskService.deleteById(taskId);
    }

    private List<UUID> getUuidsFromTasksByProperty(List<TaskEntity> tasks, Function<TaskEntity, UUID> byProperty) {
        return tasks.stream()
                .map(byProperty)
                .toList();
    }
}
