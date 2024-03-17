package com.nowakartur97.personalkanbanboardbackend.sst;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public interface PostgresStarter {

    String POSTGRES_DOCKER_IMAGE = "postgres:14.1-alpine";
    PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer(DockerImageName.parse(POSTGRES_DOCKER_IMAGE));

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.username", postgresContainer::getUsername);
        registry.add("spring.r2dbc.password", postgresContainer::getPassword);
        registry.add("spring.r2dbc.url", () ->
                postgresContainer.getJdbcUrl().replace("jdbc", "r2dbc"));
        registry.add("spring.flyway.user", postgresContainer::getUsername);
        registry.add("spring.flyway.password", postgresContainer::getPassword);
        registry.add("spring.flyway.url", postgresContainer::getJdbcUrl);
    }
}
