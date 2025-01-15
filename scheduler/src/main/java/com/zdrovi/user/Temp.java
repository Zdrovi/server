package com.zdrovi.user;

import com.zdrovi.domain.repository.UserRepository;
import com.zdrovi.user.config.UserConfig;
import com.zdrovi.user.google.GoogleFormsService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Temp implements CommandLineRunner {

private final GoogleFormsService googleFormsService;
private final UserConfig userConfig;

    @Override
    public void run(String... args) throws Exception {
        var responses2 = googleFormsService.getFormsService().forms().responses().list(userConfig.getGoogleFormId()).execute();
        System.out.println(responses2);
        var responses = googleFormsService.getFormsService().forms().get(userConfig.getGoogleFormId()).execute();
        System.out.println(responses);
    }
}