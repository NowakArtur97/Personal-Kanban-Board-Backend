package com.nowakartur97.personalkanbanboardbackend.sst;

import com.nowakartur97.personalkanbanboardbackend.task.TaskRepository;
import com.nowakartur97.personalkanbanboardbackend.user.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.HttpGraphQlTester;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SST implements PostgresStarter {

    @Autowired
    protected TaskRepository taskRepository;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected HttpGraphQlTester httpGraphQlTester;

    @BeforeAll
    public static void startContainer() {
        postgresContainer.start();
    }

    @AfterEach
    public void cleanUpTables() {
        userRepository.deleteAll().block();
        taskRepository.deleteAll().block();
    }

    @AfterAll
    public static void stopContainer() {
        postgresContainer.stop();
    }
}
