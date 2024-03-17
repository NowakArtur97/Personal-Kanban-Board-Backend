package com.nowakartur97.personalkanbanboardbackend.sst;

public class GraphQLQueries {

    public static final String GET_TASKS_QUERY = """
            query TASKS_BY_USERNAME {
              tasks(username: "$username") {
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
}
