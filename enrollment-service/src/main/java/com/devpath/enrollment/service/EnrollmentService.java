package com.devpath.enrollment.service;

import com.devpath.enrollment.client.AuthClient;
import com.devpath.enrollment.client.CourseClient;
import com.devpath.enrollment.dto.external.CourseDto;
import com.devpath.enrollment.dto.external.UserDto;
import com.devpath.enrollment.dto.request.EnrollmentRequest;
import com.devpath.enrollment.dto.response.EnrollmentResponse;
import com.devpath.enrollment.dto.response.LessonProgressResponse;
import com.devpath.enrollment.exception.ResourceNotFoundException;
import com.devpath.enrollment.model.Enrollment;
import com.devpath.enrollment.model.EnrollmentStatus;
import com.devpath.enrollment.model.LessonProgress;
import com.devpath.enrollment.repository.EnrollmentRepository;
import com.devpath.enrollment.repository.LessonProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final CourseClient courseClient;
    private final AuthClient authClient;

    @Transactional
    public EnrollmentResponse enroll(String userEmail, EnrollmentRequest request) {
        UserDto user = authClient.getUserByEmail(userEmail).getData();
        if (user == null) {
            throw new ResourceNotFoundException("User not found: " + userEmail);
        }

        CourseDto course = courseClient.getCourseById(request.getCourseId()).getData();
        if (course == null) {
            throw new ResourceNotFoundException("Course not found: " + request.getCourseId());
        }

        if (enrollmentRepository.existsByUserIdAndCourseId(user.getId(), request.getCourseId())) {
            throw new IllegalArgumentException("Already enrolled in course: " + request.getCourseId());
        }

        Enrollment enrollment = Enrollment.builder()
                .userId(user.getId())
                .courseId(request.getCourseId())
                .build();

        enrollment = enrollmentRepository.save(enrollment);
        return toResponse(enrollment, course.getTitle());
    }

    public List<EnrollmentResponse> getMyEnrollments(String userEmail) {
        UserDto user = authClient.getUserByEmail(userEmail).getData();
        if (user == null) {
            throw new ResourceNotFoundException("User not found: " + userEmail);
        }

        return enrollmentRepository.findByUserId(user.getId()).stream()
                .map(e -> {
                    CourseDto course = courseClient.getCourseById(e.getCourseId()).getData();
                    String title = course != null ? course.getTitle() : "Unknown";
                    return toResponse(e, title);
                })
                .collect(Collectors.toList());
    }

    public EnrollmentResponse getEnrollmentByCourse(String userEmail, Long courseId) {
        UserDto user = authClient.getUserByEmail(userEmail).getData();
        if (user == null) {
            throw new ResourceNotFoundException("User not found: " + userEmail);
        }

        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(user.getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found for course: " + courseId));

        CourseDto course = courseClient.getCourseById(courseId).getData();
        String title = course != null ? course.getTitle() : "Unknown";
        return toResponse(enrollment, title);
    }

    @Transactional
    public EnrollmentResponse completeLesson(String userEmail, Long courseId, Long lessonId) {
        UserDto user = authClient.getUserByEmail(userEmail).getData();
        if (user == null) {
            throw new ResourceNotFoundException("User not found: " + userEmail);
        }

        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(user.getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found for course: " + courseId));

        LessonProgress progress = lessonProgressRepository
                .findByEnrollmentIdAndLessonId(enrollment.getId(), lessonId)
                .orElseGet(() -> LessonProgress.builder()
                        .enrollment(enrollment)
                        .lessonId(lessonId)
                        .build());

        if (!progress.getCompleted()) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
            lessonProgressRepository.save(progress);
        }

        recalculateProgress(enrollment);
        enrollmentRepository.save(enrollment);

        CourseDto course = courseClient.getCourseById(courseId).getData();
        String title = course != null ? course.getTitle() : "Unknown";
        return toResponse(enrollment, title);
    }

    private void recalculateProgress(Enrollment enrollment) {
        CourseDto course = courseClient.getCourseById(enrollment.getCourseId()).getData();
        if (course == null || course.getTotalLessons() == null || course.getTotalLessons() == 0) {
            return;
        }

        long completed = lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(enrollment.getId());
        int pct = (int) ((completed * 100) / course.getTotalLessons());
        enrollment.setProgressPct(Math.min(pct, 100));

        if (enrollment.getProgressPct() == 100) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollment.setCompletedAt(LocalDateTime.now());
        }
    }

    private EnrollmentResponse toResponse(Enrollment enrollment, String courseTitle) {
        List<LessonProgressResponse> progresses = lessonProgressRepository
                .findByEnrollmentId(enrollment.getId()).stream()
                .map(lp -> LessonProgressResponse.builder()
                        .lessonId(lp.getLessonId())
                        .completed(lp.getCompleted())
                        .completedAt(lp.getCompletedAt())
                        .build())
                .collect(Collectors.toList());

        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .userId(enrollment.getUserId())
                .courseId(enrollment.getCourseId())
                .courseTitle(courseTitle)
                .status(enrollment.getStatus())
                .progressPct(enrollment.getProgressPct())
                .enrolledAt(enrollment.getEnrolledAt())
                .completedAt(enrollment.getCompletedAt())
                .lessonProgresses(progresses)
                .build();
    }
}
