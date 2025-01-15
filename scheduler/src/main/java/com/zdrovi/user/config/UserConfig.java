package com.zdrovi.user.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@RequiredArgsConstructor
@Data
@ConfigurationProperties("user")
public class UserConfig {
    private String googleApplicationName;
    private String googleCredentialsFile;
    private String googleFormId;
}
