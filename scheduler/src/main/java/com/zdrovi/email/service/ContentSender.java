package com.zdrovi.email.service;

import com.zdrovi.domain.entity.Content;
import com.zdrovi.domain.entity.User;
import com.zdrovi.domain.entity.UserCourse;
import com.zdrovi.domain.repository.ContentRepository;
import com.zdrovi.domain.repository.UserCourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentSender {

    private final EmailSender emailSender;

    private final UserCourseRepository userCourseRepository;

    private final ContentRepository contentRepository;

    @Transactional
    public void findAndSendNewestContent(final User user) {
        Optional<UserCourse> topUnfinishedCourse = userCourseRepository.findTopUnfinishedCourseByUserId(user.getId());
        if (topUnfinishedCourse.isPresent()) {
            UserCourse userCourse = topUnfinishedCourse.get();
            log.info("Found top unfinished course for user: {}, with stage: {}", user.getEmail(), userCourse.getStage());
            Optional<Content> nextContent = contentRepository.findNextContentForUserCourse(userCourse.getId());
            if (nextContent.isPresent()) {
                emailSender.sendMailToUser(user, nextContent.get());
                userCourseRepository.incrementStage(userCourse.getId());
            }
        }
    }
}
