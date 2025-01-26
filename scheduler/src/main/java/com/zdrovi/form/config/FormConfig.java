package com.zdrovi.form.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@RequiredArgsConstructor
@Data
@ConfigurationProperties("form")
public class FormConfig {
    private boolean createLabelIfNotExist;
    private String decoderVersion;
    private String googleApplicationName;
    private String googleCredentialsFile;
    private String googleFormId;
    private String period;
}
