package com.zdrovi.domain.repository;

import com.zdrovi.domain.entity.CourseContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CourseContentRepository extends JpaRepository<CourseContent, UUID> {
}