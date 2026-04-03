package com.devpath.course.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "order_number", nullable = false)
    private int orderNumber;

    @Column(nullable = false)
    private String title;

    @Column(name = "content_cache", columnDefinition = "TEXT")
    private String contentCache;

    @Column(name = "content_generated_at")
    private LocalDateTime contentGeneratedAt;
}
