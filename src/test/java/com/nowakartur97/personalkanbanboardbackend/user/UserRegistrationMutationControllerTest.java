package com.nowakartur97.personalkanbanboardbackend.user;

import com.nowakartur97.personalkanbanboardbackend.integration.IntegrationTest;
import graphql.ErrorType;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.ResponseError;

import java.util.List;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.REGISTER_USER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserRegistrationMutationControllerTest extends IntegrationTest {

    private final static String REGISTER_USER_PATH = "registerUser";

    @Test
    public void whenRegisterUser_shouldReturnUserResponse() {

        UserDTO userDTO = new UserDTO("user123", "pass123", "user123@domain.com");

        UserResponse userResponse = httpGraphQlTester
                .document(REGISTER_USER)
                .variable("userDTO", userDTO)
                .execute()
                .errors()
                .verify()
                .path(REGISTER_USER_PATH)
                .entity(UserResponse.class)
                .get();

        assertThat(userResponse.username()).isEqualTo(userDTO.getUsername());
        assertTrue(bCryptPasswordEncoder.matches(userDTO.getPassword(), userResponse.password()));
        assertThat(userResponse.email()).isEqualTo(userDTO.getEmail());
        assertThat(userResponse.token()).isEqualTo(jwtUtil.generateToken(userDTO.getUsername(), UserRole.USER.name()));
        assertThat(userResponse.expirationTimeInMilliseconds()).isEqualTo(jwtConfigurationProperties.getExpirationTimeInMilliseconds());
        assertThat(userRepository.count().block()).isOne();
    }

    @Test
    public void whenRegisterUserWithoutUserData_shouldReturnGraphQLErrorResponse() {

        httpGraphQlTester
                .document(REGISTER_USER)
                .execute()
                .errors()
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertErrorResponse(responseError, ErrorType.ValidationError,
                                    "Variable 'userDTO' has an invalid value: Variable 'userDTO' has coerced Null value for NonNull type 'UserDTO!'");
                        });
    }

    @Test
    public void whenRegisterUserWithUsernameAlreadyTaken_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();

        httpGraphQlTester
                .document(REGISTER_USER)
                .variable("userDTO", new UserDTO(userEntity.getUsername(), "pass123", "email@domain.com"))
                .execute()
                .errors()
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertErrorResponse(responseError, "Username/email is already taken.");
                        });
    }

    @Test
    public void whenRegisterUserWithBlankUsername_shouldReturnGraphQLErrorResponse() {

        httpGraphQlTester
                .document(REGISTER_USER)
                .variable("userDTO", new UserDTO("", "pass123", "email@domain.com"))
                .execute()
                .errors()
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isEqualTo(2);
                            ResponseError firstResponseError = responseErrors.getFirst();
                            assertErrorResponse(firstResponseError, "Username cannot be empty.");
                            ResponseError secondResponseError = responseErrors.getLast();
                            assertErrorResponse(secondResponseError, "Username must be between 4 and 100 characters.");
                        });
    }

    @Test
    public void whenRegisterUserWithTooShortUsername_shouldReturnGraphQLErrorResponse() {

        httpGraphQlTester
                .document(REGISTER_USER)
                .variable("userDTO", new UserDTO("u", "pass123", "email@domain.com"))
                .execute()
                .errors()
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertErrorResponse(responseError, "Username must be between 4 and 100 characters.");
                        });
    }

    @Test
    public void whenRegisterUserWithBlankPassword_shouldReturnGraphQLErrorResponse() {

        httpGraphQlTester
                .document(REGISTER_USER)
                .variable("userDTO", new UserDTO("user", "", "email@domain.com"))
                .execute()
                .errors()
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertErrorResponse(responseError, "Password cannot be empty.");
                        });
    }

    @Test
    public void whenRegisterUserWithEmailAlreadyTaken_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();

        httpGraphQlTester
                .document(REGISTER_USER)
                .variable("userDTO", new UserDTO("user", "pass123", userEntity.getEmail()))
                .execute()
                .errors()
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertErrorResponse(responseError, "Username/email is already taken.");
                        });
    }

    @Test
    public void whenRegisterUserWithBlankEmail_shouldReturnGraphQLErrorResponse() {

        httpGraphQlTester
                .document(REGISTER_USER)
                .variable("userDTO", new UserDTO("user", "pass123", ""))
                .execute()
                .errors()
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertErrorResponse(responseError, "Email cannot be empty.");
                        });
    }

    @Test
    public void whenRegisterUserWithInvalidEmail_shouldReturnGraphQLErrorResponse() {

        httpGraphQlTester
                .document(REGISTER_USER)
                .variable("userDTO", new UserDTO("user", "pass123", "invalid.com"))
                .execute()
                .errors()
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertErrorResponse(responseError, "Email must be a valid email address.");
                        });
    }

    private void assertErrorResponse(ResponseError responseError, String message) {
        assertThat(responseError.getMessage()).isEqualTo(message);
        assertThat(responseError.getPath()).isEqualTo("registerUser");
        assertThat(responseError.getLocations()).isEqualTo(List.of(new SourceLocation(2, 3)));
    }

    private void assertErrorResponse(ResponseError responseError, graphql.ErrorType errorType, String message) {
        assertThat(responseError.getErrorType()).isEqualTo(errorType);
        assertThat(responseError.getMessage()).isEqualTo(message);
        assertThat(responseError.getPath()).isEqualTo("");
        assertThat(responseError.getLocations()).isEqualTo(List.of(new SourceLocation(1, 24)));
    }
}
