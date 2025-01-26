package com.zdrovi.form.google;

import java.io.IOException;

public interface GoogleCredentialsService {

    String getAccessToken() throws IOException;
}
