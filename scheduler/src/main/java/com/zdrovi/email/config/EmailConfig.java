package com.zdrovi.email.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@RequiredArgsConstructor
@Data
@ConfigurationProperties("email.send")
public class EmailConfig {
    private String from;
    private String period;
}
