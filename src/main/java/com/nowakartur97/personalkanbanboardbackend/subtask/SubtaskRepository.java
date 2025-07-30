package com.nowakartur97.personalkanbanboardbackend.subtask;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SubtaskRepository extends R2dbcRepository<SubtaskEntity, UUID> {

}
