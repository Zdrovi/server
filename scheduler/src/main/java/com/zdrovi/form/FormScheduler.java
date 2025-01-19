package com.zdrovi.form;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static java.time.ZonedDateTime.now;

@Slf4j
@Component
@RequiredArgsConstructor
public class FormScheduler {

    private final ResponseProcessor responseProcessor;

    @Scheduled(cron = "${form.period}")
    void process() {
        log.info("Form Scheduler started at {}", now());
        responseProcessor.processResponses();
    }

}
