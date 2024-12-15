package com.zdrovi.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "label")
@Getter
@Setter
@NoArgsConstructor
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "label")
    private Set<UserLabel> userLabels = new HashSet<>();

    @OneToMany(mappedBy = "label")
    private Set<ContentLabel> contentLabels = new HashSet<>();
}
