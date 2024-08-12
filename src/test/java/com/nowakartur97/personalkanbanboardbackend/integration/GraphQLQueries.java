package com.nowakartur97.personalkanbanboardbackend.integration;

public class GraphQLQueries {

    public static final String CREATE_TASK = """
            mutation CREATE_TASK($taskDTO: TaskDTO!) {
              createTask(taskDTO: $taskDTO) {
                taskId
                title
                description
                priority
                status
                targetEndDate
                assignedTo
                createdOn
                createdBy
                updatedOn
                updatedBy
              }
            }
            """;

    public static final String GET_TASKS = """
            query TASKS {
              tasks {
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
                userId
                username
                email
                token
                expirationTimeInMilliseconds
              }
            }
            """;

    public static final String AUTHENTICATE_USER = """
            query AUTHENTICATE_USER($authenticationRequest: AuthenticationRequest!) {
              loginUser(authenticationRequest: $authenticationRequest) {
                userId
                username
                email
                token
                expirationTimeInMilliseconds
              }
            }
            """;
}
