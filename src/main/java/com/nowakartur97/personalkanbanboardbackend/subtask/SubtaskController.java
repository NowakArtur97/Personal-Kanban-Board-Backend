package com.nowakartur97.personalkanbanboardbackend.subtask;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('USER')")
public class SubtaskController {

    private final SubtaskService subtaskService;

    @MutationMapping
    public Mono<Void> deleteSubtask(@Argument UUID subtaskId) {
        return subtaskService.deleteById(subtaskId);
    }
}
