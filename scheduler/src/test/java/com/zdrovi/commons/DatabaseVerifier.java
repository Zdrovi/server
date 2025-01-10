package com.zdrovi.commons;

import com.zdrovi.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Component
@RequiredArgsConstructor
public class DatabaseVerifier {

    public enum Repositories
    {
        User, Course, Content, UserCourse, ContentCourse
    }

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ContentRepository contentRepository;
    private final UserCourseRepository userCourseRepository;
    private final CourseContentRepository courseContentRepository;

    private Map<Repositories, Long> initialCounts;
    private Set<UUID> initialUserIds;
    private Set<UUID> initialCourseIds;
    private Set<UUID> initialContentIds;
    private Set<UUID> initialUserCourseIds;
    private Set<UUID> initialCourseContentIds;

    public void captureInitialState() {
        initialCounts = captureTableCounts();
        initialUserIds = captureIds(userRepository.findAll());
        initialCourseIds = captureIds(courseRepository.findAll());
        initialContentIds = captureIds(contentRepository.findAll());
        initialUserCourseIds = captureIds(userCourseRepository.findAll());
        initialCourseContentIds = captureIds(courseContentRepository.findAll());
    }

    @Transactional
    public void verifyDatabaseIntegrity()
    {
        verifyDatabaseIntegrity(Arrays.stream(Repositories.values()).toList());
    }

    @Transactional
    public void verifyDatabaseIntegrity(List<Repositories> included) {
        // Verify counts
        Map<Repositories, Long> currentCounts = captureTableCounts();

        for (Map.Entry<Repositories, Long> entry : currentCounts.entrySet()) {
            Repositories repositories = entry.getKey();
            Long count = entry.getValue();
            if (included.contains(repositories)) {
                assertThat(initialCounts.containsKey(repositories)).isTrue();
                assertThat(initialCounts.get(repositories)).isEqualTo(count);
            }
        }

        // Verify no entities were removed
        if (included.contains(Repositories.User)) {
            assertThat(captureIds(userRepository.findAll())).containsAll(initialUserIds);
        }
        if (included.contains(Repositories.Course)) {
            assertThat(captureIds(courseRepository.findAll())).containsAll(initialCourseIds);
        }
        if (included.contains(Repositories.Content)) {
            assertThat(captureIds(contentRepository.findAll())).containsAll(initialContentIds);
        }
        if (included.contains(Repositories.UserCourse)) {
            assertThat(captureIds(userCourseRepository.findAll())).containsAll(initialUserCourseIds);
        }
        if (included.contains(Repositories.ContentCourse)) {
            assertThat(captureIds(courseContentRepository.findAll())).containsAll(initialCourseContentIds);
        }

        // Verify relationships
        verifyRelationships();
    }

    private Map<Repositories, Long> captureTableCounts() {
        Map<Repositories, Long> counts = new HashMap<>();
        counts.put(Repositories.User, userRepository.count());
        counts.put(Repositories.Course, courseRepository.count());
        counts.put(Repositories.Content, contentRepository.count());
        counts.put(Repositories.UserCourse, userCourseRepository.count());
        counts.put(Repositories.ContentCourse, courseContentRepository.count());
        return counts;
    }

    private <T> Set<UUID> captureIds(List<T> entities) {
        return entities.stream()
                .map(entity -> {
                    try {
                        Method getId = entity.getClass().getMethod("getId");
                        return (UUID) getId.invoke(entity);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to get ID", e);
                    }
                })
                .collect(Collectors.toSet());
    }

    private void verifyRelationships() {
        // Verify User relationships
        userRepository.findAll().forEach(user -> {
            assertThat(user.getUserLabels()).isNotNull();
            assertThat(user.getUserCourses()).isNotNull();
            user.getUserCourses().forEach(userCourse -> {
                assertThat(userCourse.getUser()).isEqualTo(user);
                assertThat(userCourse.getCourse()).isNotNull();
            });
        });

        // Verify Course relationships
        courseRepository.findAll().forEach(course -> {
            assertThat(course.getUserCourses()).isNotNull();
            assertThat(course.getCourseContents()).isNotNull();
            course.getUserCourses().forEach(userCourse -> {
                assertThat(userCourse.getCourse()).isEqualTo(course);
                assertThat(userCourse.getUser()).isNotNull();
            });
            course.getCourseContents().forEach(courseContent -> {
                assertThat(courseContent.getCourse()).isEqualTo(course);
                assertThat(courseContent.getContent()).isNotNull();
            });
        });

        // Verify Content relationships
        contentRepository.findAll().forEach(content -> {
            assertThat(content.getContentLabels()).isNotNull();
            assertThat(content.getCourseContents()).isNotNull();
            content.getCourseContents().forEach(courseContent -> {
                assertThat(courseContent.getContent()).isEqualTo(content);
                assertThat(courseContent.getCourse()).isNotNull();
            });
        });
    }
}