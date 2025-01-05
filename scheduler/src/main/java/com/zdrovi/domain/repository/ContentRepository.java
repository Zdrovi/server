package com.zdrovi.domain.repository;


import com.zdrovi.domain.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContentRepository extends JpaRepository<Content, UUID> {

    @Query("""
            SELECT c
            FROM UserCourse uc
            JOIN CourseContent cc ON uc.course.id = cc.course.id
            JOIN cc.content c
            WHERE uc.id = :userCourseId
            AND cc.stage = uc.stage + 1
            """)
    Optional<Content> findNextContentForUserCourse(@Param("userCourseId") UUID userCourseId);

    @Query("""
            SELECT c
            FROM UserCourse uc
            JOIN CourseContent cc
                ON uc.course.id = cc.course.id
            JOIN cc.content c
            WHERE uc.user.id = :userId
            """)
    List<Content> findAllSeenByUser(@Param("userId") UUID userId);
}
