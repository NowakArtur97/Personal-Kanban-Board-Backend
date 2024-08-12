package com.nowakartur97.personalkanbanboardbackend.integration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test-container")
public class PostgresTestContainerTestConfiguration implements PostgresStarter {
}
