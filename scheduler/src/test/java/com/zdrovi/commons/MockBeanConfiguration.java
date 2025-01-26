package com.zdrovi.commons;

import com.zdrovi.form.google.GoogleCredentialsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MockBeanConfiguration {

    @Bean
    public GoogleCredentialsService googleCredentialsService() {
        return () -> "token";
    }
}
