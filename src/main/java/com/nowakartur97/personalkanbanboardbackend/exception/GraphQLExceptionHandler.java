package com.nowakartur97.personalkanbanboardbackend.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {

    private final static String CONSTRAINT_VALIDATION_EXCEPTION_MESSAGE_SEPARATOR = ",";
    private final static String CONSTRAINT_VALIDATION_EXCEPTION_MESSAGE_PREFIX = ": ";

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof ResourceNotFoundException) {
            return createGraphQLError(ErrorType.NOT_FOUND, ex.getMessage(), env);
        }
        return null;
    }

    @Override
    protected List<GraphQLError> resolveToMultipleErrors(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof ConstraintViolationException) {
            return mapToGraphQLErrors(ErrorType.BAD_REQUEST, ex, env);
        } else if (ex instanceof UserAlreadyExistsException) {
            return List.of(createGraphQLError(ErrorType.BAD_REQUEST, ex.getMessage(), env));
        } else if (ex instanceof MalformedJwtException
                || ex instanceof SignatureException
                || ex instanceof ExpiredJwtException
                || ex instanceof BadCredentialsException) {
            return List.of(createGraphQLError(ErrorType.UNAUTHORIZED, ex.getMessage(), env));
        } else if (ex instanceof ResourceNotFoundException) {
            return List.of(createGraphQLError(ErrorType.NOT_FOUND, ex.getMessage(), env));
        }
        return null;
    }

    private List<GraphQLError> mapToGraphQLErrors(ErrorType errorType,
                                                  Throwable ex,
                                                  DataFetchingEnvironment env) {
        return Arrays.stream(ex.getMessage().split(CONSTRAINT_VALIDATION_EXCEPTION_MESSAGE_SEPARATOR))
                .map(String::trim)
                .map(msg -> msg.substring(msg.indexOf(CONSTRAINT_VALIDATION_EXCEPTION_MESSAGE_PREFIX)
                        + CONSTRAINT_VALIDATION_EXCEPTION_MESSAGE_PREFIX.length()))
                .map(msg -> createGraphQLError(errorType, msg, env))
                .toList();
    }

    private GraphQLError createGraphQLError(ErrorType errorType, String message, DataFetchingEnvironment env) {
        return GraphqlErrorBuilder.newError()
                .errorType(errorType)
                .message(message)
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }
}
