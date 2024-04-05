package com.nowakartur97.personalkanbanboardbackend.task;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @QueryMapping
    public Flux<TaskResponse> tasks(@Argument String username) {
        return taskService.getAllTasksForUser(username);
    }
}
