package com.nowakartur97.personalkanbanboardbackend.user;

import com.nowakartur97.personalkanbanboardbackend.integration.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.FIND_ALL_USERS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UserQueryControllerTest extends IntegrationTest {

    private final static String FIND_ALL_USERS_PATH = "users";

    @Test
    public void whenFindAllUsers_shouldReturnUsersResponse() {

        UserEntity userEntity = createUser();
        UserEntity userEntity2 = createUser("developer", "developer@domain.com");

        List<UserResponse> usersResponse = httpGraphQlTester
                .mutate()
                .headers(headers -> addAuthorizationHeader(headers, userEntity))
                .build()
                .document(FIND_ALL_USERS)
                .execute()
                .errors()
                .verify()
                .path(FIND_ALL_USERS_PATH)
                .entityList(UserResponse.class)
                .get();

        assertThat(usersResponse.size()).isEqualTo(2);
        UserResponse firstUserResponse = usersResponse.getFirst();
        UserResponse secondUserResponse = usersResponse.getLast();
        assertUserResponse(firstUserResponse, userEntity);
        assertUserResponse(secondUserResponse, userEntity2);
    }

    @Test
    public void whenFindAllUsersWithoutProvidingAuthorizationHeader_shouldReturnGraphQLErrorResponse() {
        runTestForSendingRequestWithoutProvidingAuthorizationHeader(FIND_ALL_USERS, FIND_ALL_USERS_PATH);
    }

    @Test
    public void whenFindAllUsersWithExpiredToken_shouldReturnGraphQLErrorResponse() {
        runTestForSendingRequestWithExpiredToken(FIND_ALL_USERS, FIND_ALL_USERS_PATH);
    }

    @Test
    public void whenFindAllUsersWithInvalidToken_shouldReturnGraphQLErrorResponse() {
        runTestForSendingRequestWithInvalidToken(FIND_ALL_USERS, FIND_ALL_USERS_PATH);
    }

    @Test
    public void whenFindAllUsersWithDifferentTokenSignature_shouldReturnGraphQLErrorResponse() {
        runTestForSendingRequestWithDifferentTokenSignature(FIND_ALL_USERS, FIND_ALL_USERS_PATH);
    }

    private void assertUserResponse(UserResponse userResponse, UserEntity userEntity) {
        assertThat(userResponse.userId()).isEqualTo(userEntity.getUserId());
        assertThat(userResponse.username()).isEqualTo(userEntity.getUsername());
        assertThat(userResponse.email()).isEqualTo(userEntity.getEmail());
        assertThat(userResponse.expirationTimeInMilliseconds()).isEqualTo(jwtConfigurationProperties.getExpirationTimeInMilliseconds());
    }
}
