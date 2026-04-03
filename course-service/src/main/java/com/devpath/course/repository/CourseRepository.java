package com.devpath.course.repository;

import com.devpath.course.model.Course;
import com.devpath.course.model.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("SELECT c FROM Course c WHERE c.active = true " +
            "AND (:categoryId IS NULL OR c.category.id = :categoryId) " +
            "AND (:level IS NULL OR c.level = :level) " +
            "AND (:search IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Course> findWithFilters(
            @Param("categoryId") Long categoryId,
            @Param("level") Level level,
            @Param("search") String search
    );
}
