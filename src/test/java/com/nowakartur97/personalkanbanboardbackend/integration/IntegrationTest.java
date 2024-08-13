package com.nowakartur97.personalkanbanboardbackend.integration;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTConfigurationProperties;
import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
import com.nowakartur97.personalkanbanboardbackend.task.TaskRepository;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRepository;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
// TODO: test with Postgres test container
//@ActiveProfiles("test-container")
public class IntegrationTest {

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
    @Autowired
    protected BCryptPasswordEncoder bCryptPasswordEncoder;

//    @BeforeAll
//    public static void startContainer() {
//        postgresContainer.start();
//    }

    @AfterEach
    public void cleanUpTables() {
        taskRepository.deleteAll().block();
        userRepository.deleteAll().block();
    }

//    @AfterAll
//    public static void stopContainer() {
//        postgresContainer.stop();
//    }

    protected UserEntity createUser() {
        return userRepository.save(createUser("testUser", "testUser@domain.com")).block();
    }

    protected UserEntity createUser(String username, String email) {
        UserEntity user = UserEntity.builder()
                .username(username)
                .password(bCryptPasswordEncoder.encode("pass1"))
                .email(email)
                .role(UserRole.USER)
                .build();
        return userRepository.save(user).block();
    }

    protected void addAuthorizationHeader(HttpHeaders headers, String token) {
        String authHeader = jwtConfigurationProperties.getAuthorizationType() + " " + token;
        headers.add(jwtConfigurationProperties.getAuthorizationHeader(), authHeader);
    }

    protected void addAuthorizationHeader(HttpHeaders headers, UserEntity userEntity) {
        String token = jwtUtil.generateToken(userEntity.getUsername(), userEntity.getRole().name());
        String authHeader = jwtConfigurationProperties.getAuthorizationType() + " " + token;
        headers.add(jwtConfigurationProperties.getAuthorizationHeader(), authHeader);
    }

    protected void assertErrorResponse(ResponseError responseError, String message, String path, SourceLocation sourceLocation) {
        assertThat(responseError.getMessage()).contains(message);
        assertThat(responseError.getPath()).isEqualTo(path);
        assertThat(responseError.getLocations()).isEqualTo(List.of(sourceLocation));
    }

    protected void assertErrorResponse(ResponseError responseError, String path, ErrorType errorType, String message) {
        assertThat(responseError.getErrorType()).isEqualTo(errorType);
        assertErrorResponse(responseError, message, path, new SourceLocation(2, 3));
    }

    protected void assertValidationErrorResponse(ResponseError responseError, SourceLocation sourceLocation, String message) {
        assertThat(responseError.getErrorType()).isEqualTo(graphql.ErrorType.ValidationError);
        assertErrorResponse(responseError, message, "", sourceLocation);
    }

    protected void assertUnauthorizedErrorResponse(ResponseError responseError, String path, String message) {
        assertThat(responseError.getErrorType()).isEqualTo(ErrorType.UNAUTHORIZED);
        assertErrorResponse(responseError, message, path, new SourceLocation(2, 3));
    }

    protected void assertNotFoundErrorResponse(ResponseError responseError, String path, String message) {
        assertThat(responseError.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        assertErrorResponse(responseError, message, path, new SourceLocation(2, 3));
    }
}
