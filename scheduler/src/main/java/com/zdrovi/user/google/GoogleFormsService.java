package com.zdrovi.user.google;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.forms.v1.Forms;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.zdrovi.user.config.UserConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class GoogleFormsService {
    private final GoogleCredentialService googleCredentialService;
    private final UserConfig userConfig;

    public Forms getFormsService() throws IOException {
        GoogleCredentials credential = googleCredentialService.getCredential();
        return new Forms.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credential)
        )
                .setApplicationName(userConfig.getGoogleApplicationName())
                .build();
    }
}
