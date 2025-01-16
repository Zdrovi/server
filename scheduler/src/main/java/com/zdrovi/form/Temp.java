package com.zdrovi.form;

import com.zdrovi.form.config.FormConfig;
import com.zdrovi.form.google.GoogleFormsService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Temp implements CommandLineRunner {

private final GoogleFormsService googleFormsService;
private final FormConfig formConfig;

    @Override
    public void run(String... args) throws Exception {
        var responses2 = googleFormsService.getAnswers();
        System.out.println(responses2);
    }
}