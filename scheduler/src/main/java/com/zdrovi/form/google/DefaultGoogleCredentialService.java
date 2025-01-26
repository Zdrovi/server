package com.zdrovi.form.google;

import com.google.auth.oauth2.GoogleCredentials;
import com.zdrovi.form.config.FormConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

@Slf4j
public class DefaultGoogleCredentialService implements GoogleCredentialsService {

    public static final String FORMS_BODY_READONLY = "https://www.googleapis.com/auth/forms.body.readonly";

    public static final String FORMS_RESPONSES_READONLY = "https://www.googleapis.com/auth/forms.responses.readonly";

    final private GoogleCredentials credential;

    public DefaultGoogleCredentialService(final FormConfig formConfig) throws IOException {
        InputStream credentialsStream = this.getClass().
                getClassLoader().
                getResourceAsStream(formConfig.getGoogleCredentialsFile());

        this.credential = GoogleCredentials
                .fromStream(Objects.requireNonNull(credentialsStream))
                .createScoped(List.of(FORMS_BODY_READONLY, FORMS_RESPONSES_READONLY));

    }

    private GoogleCredentials getCredential() throws IOException {
        log.info("Asking for Google credentials");
        credential.refreshIfExpired();
        return credential;
    }

    @Override
    public String getAccessToken() throws IOException {
        return getCredential().getAccessToken().getTokenValue();
    }

}
