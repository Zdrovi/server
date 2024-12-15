package com.zdrovi.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "course")
@Getter
@Setter
@NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Integer stages;

    @OneToMany(mappedBy = "course")
    private Set<UserCourse> userCourses = new HashSet<>();

    @OneToMany(mappedBy = "course")
    private Set<CourseContent> courseContents = new HashSet<>();
}
