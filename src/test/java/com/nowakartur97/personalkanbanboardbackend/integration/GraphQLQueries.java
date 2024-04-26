package com.nowakartur97.personalkanbanboardbackend.integration;

public class GraphQLQueries {

    public static final String GET_TASKS = """
            query TASKS_BY_USERNAME($username: String!) {
              tasks(username: $username) {
                taskId
                title
                description
                status
                priority
                targetEndDate
                assignedTo
                createdBy
                createdOn
                updatedBy
                updatedOn
              }
            }
            """;

    public static final String REGISTER_USER = """
            mutation REGISTER_USER($userDTO: UserDTO!) {
              registerUser(userDTO: $userDTO) {
                username
                email
                password
                token
                expirationTimeInMilliseconds
              }
            }
            """;
}