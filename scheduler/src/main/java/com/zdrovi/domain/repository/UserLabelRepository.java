package com.zdrovi.domain.repository;

import com.zdrovi.domain.entity.User;
import com.zdrovi.domain.entity.UserLabel;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserLabelRepository extends JpaRepository<UserLabel, UUID> {
    List<UserLabel> findAllByUser(User user, Sort sort);
}