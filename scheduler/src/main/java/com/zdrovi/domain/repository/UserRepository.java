package com.zdrovi.domain.repository;

import com.zdrovi.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("""
        SELECT u
        FROM User u
        LEFT JOIN UserCourse uc ON u.id = uc.user.id
        LEFT JOIN Course c ON uc.course.id = c.id
        GROUP BY u.id
        HAVING COUNT(uc.id) = 0
           OR (COUNT(*) = SUM(CASE WHEN uc.stage = c.stages THEN 1 ELSE 0 END)
               AND SUM(CASE WHEN uc.stage = 0 THEN 1 ELSE 0 END) = 0)
        """)
    List<User> findAllWithoutPendingCourse();
}
