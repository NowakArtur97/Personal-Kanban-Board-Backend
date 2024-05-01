package com.nowakartur97.personalkanbanboardbackend.user;

import com.nowakartur97.personalkanbanboardbackend.integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
public class UserValidatorControllerTest extends IntegrationTest {

    private final static String USER_DATA_VALIDATOR_PATH = "/api/v1/user-data-validator";

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    public void whenCheckIfAvailableUsernameAndEmailAreTaken_shouldReturnTrue() {

        HttpEntity<String> entity = new HttpEntity<>(null, new HttpHeaders());

        Boolean response = restTemplate.exchange(
                createURL("notExistingUsername", "notExistingUsername@domain.com"),
                HttpMethod.GET, entity, Boolean.class).getBody();

        assertTrue(response);
    }

    @Test
    public void whenCheckIfAvailableUsernameAndTakenEmailAreAvailable_shouldReturnFalse() {

        UserEntity user = createUser();
        HttpEntity<String> entity = new HttpEntity<>(null, new HttpHeaders());

        Boolean response = restTemplate.exchange(
                createURL("notExistingUsername", user.getEmail()),
                HttpMethod.GET, entity, Boolean.class).getBody();

        assertFalse(response);
    }

    @Test
    public void whenCheckIfTakenUsernameAndAvailableEmailAreAvailable_shouldReturnFalse() {

        UserEntity user = createUser();
        HttpEntity<String> entity = new HttpEntity<>(null, new HttpHeaders());

        Boolean response = restTemplate.exchange(
                createURL(user.getUsername(), "notExistingUsername@domain.com"),
                HttpMethod.GET, entity, Boolean.class).getBody();

        assertFalse(response);
    }

    @Test
    public void whenCheckIfTakenUsernameAndEmailAreAvailable_shouldReturnFalse() {

        UserEntity user = createUser();
        HttpEntity<String> entity = new HttpEntity<>(null, new HttpHeaders());

        Boolean response = restTemplate.exchange(
                createURL(user.getUsername(), user.getEmail()),
                HttpMethod.GET, entity, Boolean.class).getBody();

        assertFalse(response);
    }

    private String createURL(String usernameParameter, String emailParameter) {

        if (StringUtils.isBlank(usernameParameter) && StringUtils.isBlank(emailParameter)) {
            return "http://localhost:" + port + USER_DATA_VALIDATOR_PATH;
        }

        String usernameParameterPart = "?username=" + usernameParameter;
        if (StringUtils.isNotBlank(usernameParameter) && StringUtils.isBlank(emailParameter)) {
            return "http://localhost:" + port + USER_DATA_VALIDATOR_PATH + usernameParameterPart;
        }

        String emailParameterPart = "email=" + emailParameter;
        if (StringUtils.isBlank(usernameParameter) && StringUtils.isNotBlank(emailParameter)) {
            return "http://localhost:" + port + USER_DATA_VALIDATOR_PATH + "?" + emailParameterPart;
        }

        return "http://localhost:" + port + USER_DATA_VALIDATOR_PATH + usernameParameterPart + "&" + emailParameterPart;
    }
}
