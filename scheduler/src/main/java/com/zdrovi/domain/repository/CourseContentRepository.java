package com.zdrovi.domain.repository;

import com.zdrovi.domain.entity.Course;
import com.zdrovi.domain.entity.CourseContent;
import com.zdrovi.domain.entity.User;
import com.zdrovi.domain.entity.UserCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CourseContentRepository extends JpaRepository<CourseContent, UUID> {
    List<CourseContent> findAllByCourse(Course course);
}
