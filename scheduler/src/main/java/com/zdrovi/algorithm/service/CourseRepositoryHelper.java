package com.zdrovi.algorithm.service;

import com.zdrovi.domain.entity.*;
import com.zdrovi.domain.repository.CourseContentRepository;
import com.zdrovi.domain.repository.CourseRepository;
import com.zdrovi.domain.repository.UserCourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
@Transactional(isolation = Isolation.SERIALIZABLE)
@RequiredArgsConstructor
public class CourseRepositoryHelper {

    final CourseRepository courseRepository;
    final CourseContentRepository courseContentRepository;
    final UserCourseRepository userCourseRepository;

    public Boolean hasOpenCourse(User user)
    {
        return userCourseRepository.findTopUnfinishedCourseByUserId(user.getId()).isPresent();
    }

    public void save(User user, List<Content> contents) {
        Course course = new Course();
        course.setStages(contents.size());
        course = courseRepository.save(course);

        int i = 1;
        List<CourseContent> courseContents = new ArrayList<>();
        for (Content content : contents) {
            CourseContent cc = new CourseContent();
            cc.setCourse(course);
            cc.setContent(content);
            cc.setStage(i++);
            courseContents.add(cc);
        }
        courseContentRepository.saveAll(courseContents);

        UserCourse userCourse = new UserCourse();
        userCourse.setUser(user);
        userCourse.setCourse(course);
        userCourse.setStage(0);
        userCourseRepository.save(userCourse);

        courseRepository.flush();
        courseContentRepository.flush();
        userCourseRepository.flush();
    }
}
