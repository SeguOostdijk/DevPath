package com.devpath.enrollment.repository;

import com.devpath.enrollment.model.PathCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PathCourseRepository extends JpaRepository<PathCourse, Long> {

    List<PathCourse> findByLearningPathIdOrderByOrderNumber(Long learningPathId);
}
