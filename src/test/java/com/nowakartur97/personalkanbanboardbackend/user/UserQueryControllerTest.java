package com.nowakartur97.personalkanbanboardbackend.user;

import com.nowakartur97.personalkanbanboardbackend.common.BasicIntegrationTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static com.nowakartur97.personalkanbanboardbackend.integration.GraphQLQueries.FIND_ALL_USERS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UserQueryControllerTest extends BasicIntegrationTest {

    private final static String FIND_ALL_USERS_PATH = "users";

    public UserQueryControllerTest() {
        super(FIND_ALL_USERS_PATH, FIND_ALL_USERS, null);
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenFindAllUsers_shouldReturnUsersResponse(UserRole role) {

        UserEntity userEntity = createUser(role);
        UserEntity userEntity2 = createUser("developer", "developer@domain.com");

        List<UserResponse> usersResponse = (List<UserResponse>) sendRequest(userEntity, document, path, null, UserResponse.class, true);

        assertThat(usersResponse.size()).isEqualTo(2);
        UserResponse firstUserResponse = usersResponse.getFirst();
        UserResponse secondUserResponse = usersResponse.getLast();
        assertUserResponse(firstUserResponse, userEntity);
        assertUserResponse(secondUserResponse, userEntity2);
    }

    private void assertUserResponse(UserResponse userResponse, UserEntity userEntity) {
        assertThat(userResponse.userId()).isEqualTo(userEntity.getUserId());
        assertThat(userResponse.username()).isEqualTo(userEntity.getUsername());
        assertThat(userResponse.email()).isEqualTo(userEntity.getEmail());
        assertThat(userResponse.token()).isNull();
        assertThat(userResponse.expirationTimeInMilliseconds()).isZero();
        assertThat(userResponse.role()).isNull();
    }
}
