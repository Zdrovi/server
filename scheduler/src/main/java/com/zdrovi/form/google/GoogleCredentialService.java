package com.zdrovi.form.google;

import com.google.auth.oauth2.GoogleCredentials;
import com.zdrovi.form.config.FormConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import static com.google.api.services.forms.v1.FormsScopes.FORMS_BODY_READONLY;
import static com.google.api.services.forms.v1.FormsScopes.FORMS_RESPONSES_READONLY;

@Service
@Slf4j
public class GoogleCredentialService {

    final private GoogleCredentials credential;

    public GoogleCredentialService(final FormConfig formConfig) throws IOException {
        InputStream credentialsStream = this.getClass().
                getClassLoader().
                getResourceAsStream(formConfig.getGoogleCredentialsFile());

        this.credential = GoogleCredentials
                .fromStream(Objects.requireNonNull(credentialsStream))
                .createScoped(List.of(FORMS_BODY_READONLY, FORMS_RESPONSES_READONLY));

    }

    public GoogleCredentials getCredential() throws IOException {
        log.info("Asking for Google credentials");
        credential.refreshIfExpired();
        return credential;
    }
}
