package com.zdrovi.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "content")
@Getter
@Setter
@NoArgsConstructor
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private String title;

    @Column(name = "mail_content", nullable = false)
    private String mailContent;

    @OneToMany(mappedBy = "content")
    private Set<ContentLabel> contentLabels = new HashSet<>();

    @OneToMany(mappedBy = "content")
    private Set<CourseContent> courseContents = new HashSet<>();
}
