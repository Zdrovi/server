package com.zdrovi.algorithm.service;

import com.zdrovi.domain.entity.*;
import com.zdrovi.domain.repository.CourseContentRepository;
import com.zdrovi.domain.repository.CourseRepository;
import com.zdrovi.domain.repository.UserCourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Repository
@Transactional
@RequiredArgsConstructor
public class CourseRepositoryHelper {

    private final CourseRepository courseRepository;
    private final CourseContentRepository courseContentRepository;
    private final UserCourseRepository userCourseRepository;

    public Boolean hasOpenCourse(User user)
    {
        return userCourseRepository.findTopUnfinishedCourseByUserId(user.getId()).isPresent();
    }

    public void createCourseForUser(User user, List<Content> contents) {
        Course course = createCourseForUser(contents);
        createCourseContents(contents, course);
        createUserCourse(user, course);
        flushAll();
    }

    private Course createCourseForUser(final List<Content> contents) {
        Course course = new Course();
        course.setStages(contents.size());
        return courseRepository.save(course);
    }

    private void createCourseContents(final List<Content> contents, final Course course) {
        AtomicInteger currentStage = new AtomicInteger(1);
        List<CourseContent> courseContents = contents.stream()
                .map(content -> {
                    CourseContent cc = new CourseContent();
                    cc.setCourse(course);
                    cc.setContent(content);
                    cc.setStage(currentStage.getAndIncrement());
                    return cc;
                })
                .toList();
        courseContentRepository.saveAll(courseContents);
    }

    private void createUserCourse(final User user, final Course course) {
        UserCourse userCourse = new UserCourse();
        userCourse.setUser(user);
        userCourse.setCourse(course);
        userCourse.setStage(0);
        userCourseRepository.save(userCourse);
    }

    private void flushAll()
    {
        courseRepository.flush();
        courseContentRepository.flush();
        userCourseRepository.flush();
    }
}
