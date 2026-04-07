package com.devpath.ai_service.controller;

import com.devpath.ai_service.dto.external.LearningPathDto;
import com.devpath.ai_service.dto.request.ChatRequest;
import com.devpath.ai_service.dto.request.RecommendRequest;
import com.devpath.ai_service.dto.response.ApiResponse;
import com.devpath.ai_service.dto.response.ChatResponse;
import com.devpath.ai_service.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = aiService.chat(request.getMessage());
        return ResponseEntity.ok(ApiResponse.success("Respuesta generada", response));
    }

    @PostMapping("/recommend")
    public ResponseEntity<ApiResponse<LearningPathDto>> recommend(
            @RequestHeader("X-User-Id") String userEmail,
            @Valid @RequestBody RecommendRequest request) {
        LearningPathDto path = aiService.recommend(userEmail, request);
        return ResponseEntity.ok(ApiResponse.success("Ruta de aprendizaje generada", path));
    }

    @PostMapping("/lessons/{lessonId}/content")
    public ResponseEntity<ApiResponse<String>> generateContent(
            @PathVariable Long lessonId,
            @RequestParam Long courseId) {
        String content = aiService.generateLessonContent(courseId, lessonId);
        return ResponseEntity.ok(ApiResponse.success("Contenido generado", content));
    }
}
