package com.nowakartur97.personalkanbanboardbackend.integration;

public class GraphQLQueries {

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
                subtasks {
                  subtaskId
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
            }
            """;

    public static final String GET_TASKS_ASSIGNED_TO = """
            query TASKS_ASSIGNED_TO($assignedToId: UUID!) {
              tasksAssignedTo(assignedToId: $assignedToId) {
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
                subtasks {
                  subtaskId
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
            }
            """;

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

    public static final String UPDATE_TASK = """
            mutation UPDATE_TASK($taskId: UUID!, $taskDTO: TaskDTO!) {
              updateTask(taskId: $taskId, taskDTO: $taskDTO) {
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

    public static final String UPDATE_USER_ASSIGNED_TO_TASK = """
            mutation UPDATE_USER_ASSIGNED_TO_TASK($taskId: UUID!, $assignedToId: UUID!) {
              updateUserAssignedToTask(taskId: $taskId, assignedToId: $assignedToId) {
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

    public static final String DELETE_TASK = """
            mutation DELETE_TASK($taskId: UUID!) {
              deleteTask(taskId: $taskId)
            }
            """;
    public static final String DELETE_ALL_TASKS = """
            mutation DELETE_ALL_TASKS {
              deleteAllTasks
            }
            """;
    public static final String CREATE_SUBTASK = """
            mutation CREATE_SUBTASK($taskId: UUID!, $subtaskDTO: TaskDTO!) {
              createSubtask(taskId: $taskId, subtaskDTO: $subtaskDTO) {
                subtaskId
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

    public static final String UPDATE_SUBTASK = """
            mutation UPDATE_SUBTASK($subtaskId: UUID!, $subtaskDTO: TaskDTO!) {
              updateSubtask(subtaskId: $subtaskId, subtaskDTO: $subtaskDTO) {
                subtaskId
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

    public static final String DELETE_SUBTASK = """
            mutation DELETE_SUBTASK($subtaskId: UUID!) {
              deleteSubtask(subtaskId: $subtaskId)
            }
            """;

    public static final String DELETE_ALL_SUBTASKS_BY_TASK_ID = """
            mutation DELETE_ALL_SUBTASKS_BY_TASK_ID($taskId: UUID!) {
              deleteAllSubtasksByTaskId(taskId: $taskId)
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
                role
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
                role
              }
            }
            """;

    public static final String FIND_ALL_USERS = """
            query USERS {
              users {
                userId
                username
                email
              }
            }
            """;
}
