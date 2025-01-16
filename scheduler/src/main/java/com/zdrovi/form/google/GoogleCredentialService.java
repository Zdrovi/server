package com.zdrovi.form.google;

import com.google.api.services.forms.v1.FormsScopes;
import com.google.auth.oauth2.GoogleCredentials;
import com.zdrovi.form.config.FormConfig;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

@Service
public class GoogleCredentialService {
    final private GoogleCredentials credential;

    GoogleCredentialService(FormConfig formConfig) throws IOException {
        InputStream credentialsStream = this.getClass().
                getClassLoader().
                getResourceAsStream(formConfig.getGoogleCredentialsFile());

        this.credential = GoogleCredentials
                .fromStream(Objects.requireNonNull(credentialsStream))
                .createScoped(List.of(FormsScopes.FORMS_BODY_READONLY,FormsScopes.FORMS_RESPONSES_READONLY));

    }

    GoogleCredentials getCredential() throws IOException {
        credential.refreshIfExpired();
        return credential;
    }
}
