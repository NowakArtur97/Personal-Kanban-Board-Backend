package com.nowakartur97.personalkanbanboardbackend.configuration;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
class ValidationMessagesSourceConfiguration {

    @Bean
    public LocalValidatorFactoryBean getValidator() {
        LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
        localValidatorFactoryBean.setValidationMessageSource(getMessageSource());
        return localValidatorFactoryBean;
    }

    @Bean
    public MessageSource getMessageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:/ValidationMessages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
