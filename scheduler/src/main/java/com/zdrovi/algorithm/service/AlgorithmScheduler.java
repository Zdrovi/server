package com.zdrovi.algorithm.service;

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
public class AlgorithmScheduler {

    private final UserRepository userRepository;

    private final CourseBuilder courseBuilder;

    @Scheduled(cron = "${algorithm.period}")
    void process() {
        log.info("Algorithm scheduler started");
        userRepository
                .findAllWithoutPendingCourse()
                .forEach(this::createCourse);
    }

    private void createCourse(User user) {
        log.debug("Creating course for {}", user.getId());
        try {
            courseBuilder.prepareCourse(user);
        } catch (final Exception e) {
            log.error("Error preparing course for user: {}, with error: {}, root cause: {}",
                    user.getId(),
                    e.getMessage(),
                    Throwables.getRootCause(e).getMessage());
        }
    }

}
