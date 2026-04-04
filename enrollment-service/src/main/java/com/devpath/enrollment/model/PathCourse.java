package com.devpath.enrollment.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "path_courses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PathCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "path_id", nullable = false)
    private LearningPath learningPath;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "order_number", nullable = false)
    private Integer orderNumber;
}
