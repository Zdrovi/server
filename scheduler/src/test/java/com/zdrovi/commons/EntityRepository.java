package com.zdrovi.commons;

import com.zdrovi.domain.entity.*;
import com.zdrovi.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EntityRepository {

    public final UserRepository userRepository;

    public final CourseRepository courseRepository;

    public final ContentRepository contentRepository;

    public final UserCourseRepository userCourseRepository;

    public final CourseContentRepository courseContentRepository;

    public User createBasicUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    public record TestCourseSetup(User user, Content content1, Content content2) {
    }

    public TestCourseSetup setupCompleteCoursePath(String name, String email) {
        User user = createBasicUser(name, email);
        Course course = createCourse(2);
        Content content1 = createContent("First Stage", "Content for first stage");
        Content content2 = createContent("Second Stage", "Content for second stage");

        createUserCourse(user, course, 0);
        createCourseContent(course, content1, 1);
        createCourseContent(course, content2, 2);

        return new TestCourseSetup(user, content1, content2);
    }

    public Course createCourse(int stages) {
        Course course = new Course();
        course.setStages(stages);
        return courseRepository.save(course);
    }

    public Content createContent(String title, String mailContent) {
        Content content = new Content();
        content.setPath("/test/path/" + title.toLowerCase().replace(" ", "-"));
        content.setTitle(title);
        content.setMailContent(mailContent);
        return contentRepository.save(content);
    }

    public UserCourse createUserCourse(User user, Course course, int stage) {
        UserCourse userCourse = new UserCourse();
        userCourse.setUser(user);
        userCourse.setCourse(course);
        userCourse.setStage(stage);
        var result = userCourseRepository.save(userCourse);

        user.getUserCourses().add(userCourse);
        course.getUserCourses().add(userCourse);
        userRepository.save(user);
        courseRepository.save(course);

        return result;
    }

    public CourseContent createCourseContent(Course course, Content content, int stage) {
        CourseContent courseContent = new CourseContent();
        courseContent.setCourse(course);
        courseContent.setContent(content);
        courseContent.setStage(stage);
        var result = courseContentRepository.save(courseContent);

        course.getCourseContents().add(result);
        content.getCourseContents().add(result);
        courseRepository.save(course);
        contentRepository.save(content);

        return result;
    }

    public User setupUserWithCourseAndContent(String title,
                                              String name,
                                              String email,
                                              String rawContent) {
        User user = createBasicUser(name, email);
        Course course = createCourse(3);
        Content content = createContent(title, rawContent);

        UserCourse userCourse = createUserCourse(user, course, 1);
        CourseContent courseContent = createCourseContent(course, content, 2);

        user.getUserCourses().add(userCourse);
        userRepository.save(user);
        course.getCourseContents().add(courseContent);
        courseRepository.save(course);

        return user;
    }
}
