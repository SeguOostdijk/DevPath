package com.devpath.course.controller;

import com.devpath.course.dto.request.CreateCourseRequest;
import com.devpath.course.dto.request.CreateLessonRequest;
import com.devpath.course.dto.request.UpdateContentCacheRequest;
import com.devpath.course.dto.request.UpdateCourseRequest;
import com.devpath.course.dto.response.*;
import com.devpath.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseSummaryResponse>>> getCourses(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success("Cursos obtenidos",
                courseService.getCourses(categoryId, level, search)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Curso obtenido", courseService.getCourseById(id)));
    }

    @GetMapping("/{id}/lessons")
    public ResponseEntity<ApiResponse<List<LessonResponse>>> getLessons(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Temario obtenido", courseService.getLessons(id)));
    }

    @GetMapping("/{id}/lessons/{lessonId}")
    public ResponseEntity<ApiResponse<LessonDetailResponse>> getLessonDetail(
            @PathVariable Long id,
            @PathVariable Long lessonId) {
        return ResponseEntity.ok(ApiResponse.success("Clase obtenida",
                courseService.getLessonDetail(id, lessonId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Curso creado", courseService.createCourse(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Curso actualizado", courseService.updateCourse(id, request)));
    }

    @PutMapping("/{id}/lessons/{lessonId}/cache")
    public ResponseEntity<ApiResponse<Void>> updateContentCache(
            @PathVariable Long id,
            @PathVariable Long lessonId,
            @Valid @RequestBody UpdateContentCacheRequest request) {
        courseService.updateContentCache(id, lessonId, request);
        return ResponseEntity.ok(ApiResponse.success("Caché de contenido actualizado", null));
    }

    @PostMapping("/{id}/lessons")
    public ResponseEntity<ApiResponse<LessonResponse>> addLesson(
            @PathVariable Long id,
            @Valid @RequestBody CreateLessonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Clase agregada", courseService.addLesson(id, request)));
    }
}
