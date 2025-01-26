package com.zdrovi.form.google.config;

import com.zdrovi.form.config.FormConfig;
import com.zdrovi.form.google.DefaultGoogleCredentialService;
import com.zdrovi.form.google.GoogleCredentialsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;


@Configuration
public class GoogleCredentialsConfiguration {

    @Bean
    public GoogleCredentialsService googleCredentialService(final FormConfig formConfig) throws IOException {
        return new DefaultGoogleCredentialService(formConfig);
    }
}
