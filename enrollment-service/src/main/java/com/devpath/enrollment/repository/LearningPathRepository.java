package com.devpath.enrollment.repository;

import com.devpath.enrollment.model.LearningPath;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {

    List<LearningPath> findByUserIdOrderByCreatedAtDesc(Long userId);
}
