package com.nowakartur97.personalkanbanboardbackend.user;

import com.nowakartur97.personalkanbanboardbackend.integration.IntegrationTest;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.ResponseError;

import java.util.List;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.REGISTER_USER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UserRegistrationControllerTest extends IntegrationTest {

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

    private void assertErrorResponse(ResponseError responseError, String expectedMessage) {
        assertThat(responseError.getMessage()).isEqualTo(expectedMessage);
        assertThat(responseError.getPath()).isEqualTo("registerUser");
        assertThat(responseError.getLocations()).isEqualTo(List.of(new SourceLocation(2, 3)));
    }
}
