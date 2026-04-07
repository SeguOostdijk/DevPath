package com.devpath.ai_service.service;

import com.devpath.ai_service.client.AuthClient;
import com.devpath.ai_service.client.CourseClient;
import com.devpath.ai_service.client.EnrollmentClient;
import com.devpath.ai_service.dto.external.CourseDto;
import com.devpath.ai_service.dto.external.LearningPathDto;
import com.devpath.ai_service.dto.external.LessonDto;
import com.devpath.ai_service.dto.external.UserDto;
import com.devpath.ai_service.dto.request.ContentCacheRequest;
import com.devpath.ai_service.dto.request.RecommendRequest;
import com.devpath.ai_service.dto.request.SavePathRequest;
import com.devpath.ai_service.dto.response.ChatResponse;
import com.devpath.ai_service.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiService {

    private final ChatClient chatClient;
    private final CourseClient courseClient;
    private final EnrollmentClient enrollmentClient;
    private final AuthClient authClient;
    private final ObjectMapper objectMapper;

    public AiService(ChatClient.Builder chatClientBuilder,
                     CourseClient courseClient,
                     EnrollmentClient enrollmentClient,
                     AuthClient authClient) {
        this.chatClient = chatClientBuilder.build();
        this.courseClient = courseClient;
        this.enrollmentClient = enrollmentClient;
        this.authClient = authClient;
        this.objectMapper = new ObjectMapper();
    }

    public ChatResponse chat(String message) {
        String systemPrompt = """
                Eres un asistente de aprendizaje de DevPath, una plataforma de cursos de programación.
                Tu objetivo es ayudar al usuario a definir su ruta de aprendizaje personalizada.
                En la conversación debes descubrir:
                1. Su objetivo de aprendizaje (¿qué quiere aprender o lograr?)
                2. Su nivel actual (principiante, intermedio o avanzado)
                3. Cuántas horas por semana puede dedicar al estudio
                Cuando tengas esta información, resume los datos recolectados y dile al usuario que puede
                solicitar su ruta de aprendizaje personalizada.
                Sé amigable, conciso y orientado al mundo de la programación.
                """;

        String response = chatClient.prompt()
                .system(systemPrompt)
                .user(message)
                .call()
                .content();

        return new ChatResponse(response);
    }

    public LearningPathDto recommend(String userEmail, RecommendRequest request) {
        // 1. Get course catalog
        List<CourseDto> courses = courseClient.getAllCourses().getData();
        if (courses == null || courses.isEmpty()) {
            throw new ResourceNotFoundException("No hay cursos disponibles en el catálogo");
        }

        // 2. Build prompt with catalog context
        String catalog = courses.stream()
                .map(c -> String.format("ID: %d | Título: %s | Nivel: %s | Categoría: %s | Descripción: %s",
                        c.getId(), c.getTitle(), c.getLevel(), c.getCategory(), c.getDescription()))
                .collect(Collectors.joining("\n"));

        String prompt = String.format("""
                Eres un experto en rutas de aprendizaje de programación.

                El usuario quiere aprender: %s
                Nivel actual: %s
                Horas disponibles por semana: %s

                Catálogo de cursos disponibles:
                %s

                Selecciona los cursos más relevantes del catálogo para este usuario y ordénalos de menor a mayor dificultad.
                Responde ÚNICAMENTE con un JSON válido en este formato exacto, sin texto adicional:
                {"courseIds": [id1, id2, id3]}

                Solo incluye IDs de cursos del catálogo. Incluye entre 3 y 7 cursos.
                """,
                request.getGoal(),
                request.getLevel() != null ? request.getLevel() : "no especificado",
                request.getHoursPerWeek() != null ? request.getHoursPerWeek() : "no especificado",
                catalog);

        // 3. Call AI
        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // 4. Parse course IDs
        List<Long> courseIds = parseCourseIds(aiResponse);
        if (courseIds.isEmpty()) {
            throw new IllegalArgumentException("No se pudieron determinar cursos relevantes para tu objetivo");
        }

        // 5. Get user ID
        UserDto user = authClient.getUserByEmail(userEmail).getData();
        if (user == null) {
            throw new ResourceNotFoundException("Usuario no encontrado: " + userEmail);
        }

        // 6. Save and return path
        SavePathRequest saveRequest = new SavePathRequest();
        saveRequest.setUserId(user.getId());
        saveRequest.setGoal(request.getGoal());
        saveRequest.setLevel(request.getLevel());
        saveRequest.setHoursPerWeek(request.getHoursPerWeek());
        saveRequest.setCourseIds(courseIds);

        return enrollmentClient.savePath(saveRequest).getData();
    }

    public String generateLessonContent(Long courseId, Long lessonId) {
        // 1. Get lesson and check cache
        LessonDto lesson = courseClient.getLessonById(courseId, lessonId).getData();
        if (lesson == null) {
            throw new ResourceNotFoundException("Clase no encontrada con id: " + lessonId);
        }

        if (lesson.getContentCache() != null && !lesson.getContentCache().isBlank()) {
            return lesson.getContentCache();
        }

        // 2. Generate content
        String prompt = String.format("""
                Eres un instructor experto en programación. Genera el contenido educativo completo para esta clase:

                Título de la clase: %s

                El contenido debe incluir:
                - Explicación del concepto principal
                - Ejemplo práctico de código (si aplica)
                - Puntos clave a recordar

                Escribe de forma clara y didáctica, adecuada para estudiantes de programación.
                """, lesson.getTitle());

        String content = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // 3. Save to cache
        courseClient.updateContentCache(courseId, lessonId, new ContentCacheRequest(content));

        return content;
    }

    private List<Long> parseCourseIds(String aiResponse) {
        try {
            // Extract JSON from response (AI may include extra text)
            Pattern pattern = Pattern.compile("\\{[^{}]*\"courseIds\"[^{}]*\\}");
            Matcher matcher = pattern.matcher(aiResponse);

            String json = aiResponse.trim();
            if (matcher.find()) {
                json = matcher.group();
            }

            JsonNode root = objectMapper.readTree(json);
            JsonNode courseIdsNode = root.get("courseIds");

            if (courseIdsNode != null && courseIdsNode.isArray()) {
                List<Long> ids = new ArrayList<>();
                courseIdsNode.forEach(node -> ids.add(node.asLong()));
                return ids;
            }
        } catch (Exception e) {
            log.warn("Could not parse AI response as JSON: {}", aiResponse);
        }
        return List.of();
    }
}
