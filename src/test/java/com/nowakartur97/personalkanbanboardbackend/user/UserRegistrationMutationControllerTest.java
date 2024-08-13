package com.nowakartur97.personalkanbanboardbackend.user;

import com.nowakartur97.personalkanbanboardbackend.integration.IntegrationTest;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.test.tester.GraphQlTester;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.REGISTER_USER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

// TODO: Add more tests: null or too long values
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

        assertThat(userResponse).isNotNull();
        assertThat(userResponse.userId()).isNotNull();
        assertThat(userResponse.username()).isEqualTo(userDTO.getUsername());
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
                            assertValidationErrorResponse(responseError, new SourceLocation(1, 24),
                                    "Variable 'userDTO' has an invalid value: Variable 'userDTO' has coerced Null value for NonNull type 'UserDTO!'"
                            );
                        });
    }

    @Test
    public void whenRegisterUserWithUsernameAlreadyTaken_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        UserDTO userDTO = new UserDTO(userEntity.getUsername(), "pass123", "email@domain.com");

        makeRegisterUserRequestWithErrors(userDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertErrorResponse(responseError, "Username/email is already taken.");
                        });
    }

    @Test
    public void whenRegisterUserWithBlankUsername_shouldReturnGraphQLErrorResponse() {

        UserDTO userDTO = new UserDTO("", "pass123", "email@domain.com");

        makeRegisterUserRequestWithErrors(userDTO)
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

        UserDTO userDTO = new UserDTO("u", "pass123", "email@domain.com");

        makeRegisterUserRequestWithErrors(userDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertErrorResponse(responseError, "Username must be between 4 and 100 characters.");
                        });
    }

    @Test
    public void whenRegisterUserWithBlankPassword_shouldReturnGraphQLErrorResponse() {

        UserDTO userDTO = new UserDTO("user", "", "email@domain.com");

        makeRegisterUserRequestWithErrors(userDTO)
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
        UserDTO userDTO = new UserDTO("user", "pass123", userEntity.getEmail());

        makeRegisterUserRequestWithErrors(userDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertErrorResponse(responseError, "Username/email is already taken.");
                        });
    }

    @Test
    public void whenRegisterUserWithBlankEmail_shouldReturnGraphQLErrorResponse() {

        UserDTO userDTO = new UserDTO("user", "pass123", "");

        makeRegisterUserRequestWithErrors(userDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertErrorResponse(responseError, "Email cannot be empty.");
                        });
    }

    @Test
    public void whenRegisterUserWithInvalidEmail_shouldReturnGraphQLErrorResponse() {

        UserDTO userDTO = new UserDTO("user", "pass123", "invalid.com");

        makeRegisterUserRequestWithErrors(userDTO)
                .satisfy(
                        responseErrors -> {
                            assertThat(responseErrors.size()).isOne();
                            ResponseError responseError = responseErrors.getFirst();
                            assertErrorResponse(responseError, "Email must be a valid email address.");
                        });
    }

    private GraphQlTester.Errors makeRegisterUserRequestWithErrors(UserDTO userDTO) {
        return httpGraphQlTester
                .document(REGISTER_USER)
                .variable("userDTO", userDTO)
                .execute()
                .errors();
    }

    private void assertErrorResponse(ResponseError responseError, String message) {
        assertErrorResponse(responseError, message, "registerUser", new SourceLocation(2, 3));
    }
}
