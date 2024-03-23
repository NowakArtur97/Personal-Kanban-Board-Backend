package com.nowakartur97.personalkanbanboardbackend.integration;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTConfigurationProperties;
import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
import com.nowakartur97.personalkanbanboardbackend.task.TaskRepository;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRepository;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class IntegrationTest implements PostgresStarter {

    @Autowired
    protected TaskRepository taskRepository;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected HttpGraphQlTester httpGraphQlTester;
    @Autowired
    protected JWTUtil jwtUtil;
    @Autowired
    protected JWTConfigurationProperties jwtConfigurationProperties;

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

    protected UserEntity createUser() {
        UserEntity user = new UserEntity("testUser", "pass1", "testUser@domain.com", UserRole.USER);
        return userRepository.save(user).block();
    }
}
