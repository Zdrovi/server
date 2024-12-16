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

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ContentRepository contentRepository;
    private final UserCourseRepository userCourseRepository;
    private final CourseContentRepository courseContentRepository;

    private Map<String, Long> initialCounts;
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
    public void verifyDatabaseIntegrity() {
        // Verify counts
        Map<String, Long> currentCounts = captureTableCounts();
        assertThat(currentCounts).isEqualTo(initialCounts);

        // Verify no entities were removed
        assertThat(captureIds(userRepository.findAll())).containsAll(initialUserIds);
        assertThat(captureIds(courseRepository.findAll())).containsAll(initialCourseIds);
        assertThat(captureIds(contentRepository.findAll())).containsAll(initialContentIds);
        assertThat(captureIds(userCourseRepository.findAll())).containsAll(initialUserCourseIds);
        assertThat(captureIds(courseContentRepository.findAll())).containsAll(initialCourseContentIds);

        // Verify relationships
        verifyRelationships();
    }

    private Map<String, Long> captureTableCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("users", userRepository.count());
        counts.put("courses", courseRepository.count());
        counts.put("contents", contentRepository.count());
        counts.put("userCourses", userCourseRepository.count());
        counts.put("courseContents", courseContentRepository.count());
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