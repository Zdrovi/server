package com.zdrovi.domain.repository;


import com.zdrovi.domain.entity.User;
import com.zdrovi.domain.entity.UserCourse;
import com.zdrovi.domain.entity.UserLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCourseRepository extends JpaRepository<UserCourse, UUID> {

    @Query("""
            SELECT uc FROM UserCourse uc
            JOIN uc.course c
            WHERE uc.user.id = :userId
            AND uc.stage < c.stages
            ORDER BY uc.stage DESC
            LIMIT 1
            """)
    Optional<UserCourse> findTopUnfinishedCourseByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("""
            UPDATE UserCourse uc
            SET uc.stage = uc.stage + 1
            WHERE uc.id = :userCourseId
            """)
    void incrementStage(@Param("userCourseId") UUID userCourseId);

    List<UserCourse> findAllByUser(User user);
}
