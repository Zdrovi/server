package com.zdrovi.email.service;

import com.google.common.base.Throwables;
import com.zdrovi.domain.entity.User;
import com.zdrovi.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailScheduler {

    private final UserRepository userRepository;

    private final ContentSender contentSender;

    @Scheduled(cron = "${email.send.period}")
    void process() {
        userRepository
                .findAll()
                .forEach(this::trySendContent);
    }

    private void trySendContent(final User user) {
        try {
            contentSender.findAndSendNewestContent(user);
        } catch (final Exception e) {
            log.error("Error sending email for user: {}, with error: {}, root cause: {}",
                    user.getId(),
                    e.getMessage(),
                    Throwables.getRootCause(e).getMessage());
        }
    }


}
