package com.zdrovi.domain.repository;

import com.zdrovi.domain.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LabelRepository extends JpaRepository<Label, UUID> {
    List<Label> findByName(String name);
    Optional<Label> findFirstByName(String name);
}
