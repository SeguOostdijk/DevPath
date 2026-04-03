package com.devpath.course.service;

import com.devpath.course.dto.request.CreateCourseRequest;
import com.devpath.course.dto.request.CreateLessonRequest;
import com.devpath.course.dto.request.UpdateCourseRequest;
import com.devpath.course.dto.response.*;
import com.devpath.course.exception.ResourceNotFoundException;
import com.devpath.course.model.Category;
import com.devpath.course.model.Course;
import com.devpath.course.model.Instructor;
import com.devpath.course.model.Lesson;
import com.devpath.course.model.Level;
import com.devpath.course.repository.CategoryRepository;
import com.devpath.course.repository.CourseRepository;
import com.devpath.course.repository.InstructorRepository;
import com.devpath.course.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final InstructorRepository instructorRepository;
    private final LessonRepository lessonRepository;

    public List<CourseSummaryResponse> getCourses(Long categoryId, String levelStr, String search) {
        Level level = null;
        if (levelStr != null && !levelStr.isBlank()) {
            try {
                level = Level.valueOf(levelStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Nivel inválido: " + levelStr + ". Use PRINCIPIANTE, INTERMEDIO o AVANZADO");
            }
        }
        return courseRepository.findWithFilters(categoryId, level, search).stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado con id: " + id));
        return toDetail(course);
    }

    public List<LessonResponse> getLessons(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Curso no encontrado con id: " + courseId);
        }
        return lessonRepository.findByCourseIdOrderByOrderNumberAsc(courseId).stream()
                .map(l -> LessonResponse.builder()
                        .id(l.getId())
                        .orderNumber(l.getOrderNumber())
                        .title(l.getTitle())
                        .build())
                .collect(Collectors.toList());
    }

    public LessonDetailResponse getLessonDetail(Long courseId, Long lessonId) {
        Lesson lesson = lessonRepository.findByIdAndCourseId(lessonId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Clase no encontrada con id: " + lessonId));
        return toLessonDetail(lesson);
    }

    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + request.getCategoryId()));
        Instructor instructor = instructorRepository.findById(request.getInstructorId())
                .orElseThrow(() -> new ResourceNotFoundException("Instructor no encontrado con id: " + request.getInstructorId()));

        Level level;
        try {
            level = Level.valueOf(request.getLevel().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Nivel inválido: " + request.getLevel());
        }

        Course course = new Course();
        course.setCategory(category);
        course.setInstructor(instructor);
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setLevel(level);

        return toDetail(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse updateCourse(Long id, UpdateCourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado con id: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + request.getCategoryId()));
        Instructor instructor = instructorRepository.findById(request.getInstructorId())
                .orElseThrow(() -> new ResourceNotFoundException("Instructor no encontrado con id: " + request.getInstructorId()));

        Level level;
        try {
            level = Level.valueOf(request.getLevel().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Nivel inválido: " + request.getLevel());
        }

        course.setCategory(category);
        course.setInstructor(instructor);
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setLevel(level);
        course.setActive(request.getActive());

        return toDetail(courseRepository.save(course));
    }

    @Transactional
    public LessonResponse addLesson(Long courseId, CreateLessonRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado con id: " + courseId));

        Lesson lesson = new Lesson();
        lesson.setCourse(course);
        lesson.setOrderNumber(request.getOrderNumber());
        lesson.setTitle(request.getTitle());

        Lesson saved = lessonRepository.save(lesson);

        course.setTotalLessons(course.getTotalLessons() + 1);
        courseRepository.save(course);

        return LessonResponse.builder()
                .id(saved.getId())
                .orderNumber(saved.getOrderNumber())
                .title(saved.getTitle())
                .build();
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private CourseSummaryResponse toSummary(Course course) {
        return CourseSummaryResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .level(course.getLevel().name())
                .totalLessons(course.getTotalLessons())
                .category(course.getCategory().getName())
                .instructor(course.getInstructor().getName())
                .build();
    }

    private CourseResponse toDetail(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .level(course.getLevel().name())
                .totalLessons(course.getTotalLessons())
                .category(CategoryResponse.builder()
                        .id(course.getCategory().getId())
                        .name(course.getCategory().getName())
                        .slug(course.getCategory().getSlug())
                        .build())
                .instructor(InstructorResponse.builder()
                        .id(course.getInstructor().getId())
                        .name(course.getInstructor().getName())
                        .bio(course.getInstructor().getBio())
                        .build())
                .active(course.isActive())
                .build();
    }

    private LessonDetailResponse toLessonDetail(Lesson lesson) {
        return LessonDetailResponse.builder()
                .id(lesson.getId())
                .orderNumber(lesson.getOrderNumber())
                .title(lesson.getTitle())
                .contentCache(lesson.getContentCache())
                .contentGeneratedAt(lesson.getContentGeneratedAt())
                .build();
    }
}
