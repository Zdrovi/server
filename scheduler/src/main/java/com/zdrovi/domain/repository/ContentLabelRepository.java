package com.zdrovi.domain.repository;

import com.zdrovi.domain.entity.Content;
import com.zdrovi.domain.entity.ContentLabel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContentLabelRepository extends JpaRepository<ContentLabel, UUID> {
    List<ContentLabel> findAllByContent(Content user);
}
