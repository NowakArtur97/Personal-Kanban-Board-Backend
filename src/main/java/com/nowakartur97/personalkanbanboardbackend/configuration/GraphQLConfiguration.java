package com.nowakartur97.personalkanbanboardbackend.configuration;

import graphql.scalars.ExtendedScalars;
import graphql.validation.rules.OnValidationErrorStrategy;
import graphql.validation.rules.ValidationRules;
import graphql.validation.schemawiring.ValidationSchemaWiring;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
public class GraphQLConfiguration {

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        ValidationRules validationRules = ValidationRules.newValidationRules()
                .onValidationErrorStrategy(OnValidationErrorStrategy.RETURN_NULL)
                .build();
        ValidationSchemaWiring validationSchemaWiring = new ValidationSchemaWiring(validationRules);

        return wiringBuilder -> wiringBuilder
                .scalar(ExtendedScalars.Date)
                .directiveWiring(validationSchemaWiring);
    }
}
