package com.nowakartur97.personalkanbanboardbackend.user;

import com.nowakartur97.personalkanbanboardbackend.common.RequestVariable;
import com.nowakartur97.personalkanbanboardbackend.integration.IntegrationTest;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.graphql.test.tester.GraphQlTester;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.AUTHENTICATE_USER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AuthenticationQueryControllerTest extends IntegrationTest {

    private final static String AUTHENTICATE_USER_PATH = "loginUser";

    @Test
    public void whenLoginUser_shouldReturnUserResponse() {

        UserEntity userEntity = createUser();
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(userEntity.getUsername(), "pass1");
        RequestVariable requestVariable = new RequestVariable("authenticationRequest", authenticationRequest);

        UserResponse userResponse = (UserResponse) sendRequest(userEntity, AUTHENTICATE_USER, AUTHENTICATE_USER_PATH, requestVariable, UserResponse.class, false);

        assertThat(userResponse.userId()).isEqualTo(userEntity.getUserId());
        assertThat(userResponse.username()).isEqualTo(userEntity.getUsername());
        assertThat(userResponse.email()).isEqualTo(userEntity.getEmail());
        assertThat(userResponse.token()).isEqualTo(jwtUtil.generateToken(userEntity.getUsername(), userEntity.getRole().name()));
        assertThat(userResponse.expirationTimeInMilliseconds()).isEqualTo(jwtConfigurationProperties.getExpirationTimeInMilliseconds());
        assertThat(userResponse.role()).isEqualTo(userEntity.getRole());
    }

    @Test
    public void whenLoginUserWithoutAuthenticationData_shouldReturnGraphQLErrorResponse() {

        GraphQlTester.Errors errors = httpGraphQlTester
                .document(AUTHENTICATE_USER)
                .execute()
                .errors();
        assertValidationErrorResponse(errors, new SourceLocation(1, 25),
                "Variable 'authenticationRequest' has an invalid value: Variable 'authenticationRequest' has coerced Null value for NonNull type 'AuthenticationRequest!'");
    }

    @Test
    public void whenLoginAsNotExistingUser_shouldReturnGraphQLErrorResponse() {

        AuthenticationRequest authenticationRequest = new AuthenticationRequest("notExistingUser", "pass1");

        GraphQlTester.Errors errors = httpGraphQlTester
                .document(AUTHENTICATE_USER)
                .variable("authenticationRequest", authenticationRequest)
                .execute()
                .errors();
        assertNotFoundErrorResponse(errors, AUTHENTICATE_USER_PATH, "User with username/email: 'notExistingUser' not found.");
    }

    @ParameterizedTest
    @CsvSource({",password,usernameOrEmail", "username,,password"})
    public void whenLoginWithNullValues_shouldReturnGraphQLErrorResponse(String username, String password, String field) {

        AuthenticationRequest authenticationRequest = new AuthenticationRequest(username, password);

        GraphQlTester.Errors errors = httpGraphQlTester
                .document(AUTHENTICATE_USER)
                .variable("authenticationRequest", authenticationRequest)
                .execute()
                .errors();
        assertValidationErrorResponse(errors, new SourceLocation(1, 25),
                "Variable 'authenticationRequest' has an invalid value: Field '" + field + "' has coerced Null value for NonNull type 'String!'");
    }
}
