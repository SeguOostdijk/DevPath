# CLAUDE.md — DevPath

Este archivo le da contexto a Claude Code sobre el proyecto DevPath. Leelo antes de ayudar con cualquier tarea.

---

## Descripción del proyecto

DevPath es una plataforma de cursos de programación con rutas de aprendizaje personalizadas generadas por IA. El diferencial es que el usuario no elige cursos manualmente — la IA lo entrevista, entiende su objetivo y nivel, y genera una ruta ordenada de cursos a seguir. El contenido de cada clase también es generado por IA y cacheado.

Es un proyecto de práctica para consolidar conocimientos de Spring Boot, Spring Cloud, Spring Security y microservicios.

---

## Stack tecnológico

- **Backend**: Java 17, Spring Boot 3.2, Spring Cloud 2023.0.0, Spring Security + JWT
- **Base de datos**: MySQL 8.0 (una por microservicio)
- **Infraestructura**: Docker Compose, Eureka Server, Spring Cloud Gateway, Spring Cloud Config, Feign Client, Spring Boot Actuator
- **IA**: Spring AI con Groq (modelo `llama-3.3-70b-versatile`) — integrado en `ai-service` via `spring-ai-openai-spring-boot-starter`.
- **Frontend**: React (Fase 5)
- **IDE**: IntelliJ IDEA Community
- **Build**: Maven

---

## Repositorios y artifact IDs

Cada servicio tiene su propio repositorio en GitHub (multi-repo).

| Repositorio | groupId | artifactId | Puerto |
|---|---|---|---|
| `devpath-eureka-server` | `com.devpath` | `eureka-server` | 8761 |
| `devpath-config-server` | `com.devpath` | `config-server` | 8888 |
| `devpath-api-gateway` | `com.devpath` | `api-gateway` | 8080 |
| `devpath-auth-service` | `com.devpath` | `auth-service` | 8081 |
| `devpath-course-service` | `com.devpath` | `course-service` | 8082 |
| `devpath-enrollment-service` | `com.devpath` | `enrollment-service` | 8083 |
| `devpath-ai-service` | `com.devpath` | `ai-service` | 8084 |
| `devpath-frontend` | — | — | 3000 |
| `devpath-docker` | — | — | — |

El repo `devpath-docker` contiene el `docker-compose.yml` que levanta todo el sistema y el `README.md` con instrucciones.

---

## Arquitectura

Microservicios con una base de datos MySQL por servicio. El frontend nunca habla directamente con los microservicios — todo pasa por el API Gateway.

```
React App
    ↓
API Gateway (8080) — valida JWT, rutea
    ↓
┌──────────────────────────────────────────────────────────┐
│  auth-service  course-service  enrollment-service  ai-service  │
└──────────────────────────────────────────────────────────┘
       ↓               ↓                ↓
    auth_db        course_db       enrollment_db

ai-service → course-service (Feign, consulta catálogo)
ai-service → enrollment-service (Feign, persiste ruta)
ai-service → API Groq externa (HTTP)
enrollment-service → course-service (Feign, datos de cursos)
enrollment-service → auth-service (Feign, datos de usuario)
```

### Decisiones de arquitectura importantes

- **Sin `user-service`**: los datos del usuario viven en `auth-service`. Otros servicios que necesitan datos del usuario llaman a `auth-service` via Feign.
- **`ai-service` no persiste nada**: es un orquestador puro. Consulta el catálogo a `course-service`, llama a la API de Claude, y le pide a `enrollment-service` que persista la ruta.
- **Contenido de clases cacheado**: `ai-service` genera el contenido de cada clase y lo guarda en `lessons.content_cache` en `course_db`. No se regenera en cada visita — primero verifica si ya existe.
- **Rutas vs inscripciones son conceptos distintos**: una ruta es una sugerencia de la IA (no implica inscripción automática). Una inscripción es una decisión explícita del usuario. Ambas viven en `enrollment-service`.
- **`/api/paths` es INTERNAL**: solo lo llama `ai-service` internamente. Configurar en el gateway para que no sea accesible desde el exterior.
- **Sin FK entre bases de datos**: `user_id`, `course_id`, `lesson_id` en `enrollment_db` son referencias externas por ID — no foreign keys reales. Cada servicio resuelve los datos llamando al servicio dueño via Feign.

---

## Estructura de paquetes (por servicio)

```
com.devpath.{servicio}/
    controller/
    service/
    repository/
    model/
    dto/
    config/
    security/       (solo auth-service y api-gateway)
    client/         (Feign clients — en servicios que llaman a otros)
    exception/
```

---

## Modelo de datos

### auth_db

```sql
users (
    id BIGINT PK AUTO_INCREMENT,
    email VARCHAR UK NOT NULL,
    password_hash VARCHAR NOT NULL,
    name VARCHAR NOT NULL,
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT NOW()
)

refresh_tokens (
    id BIGINT PK AUTO_INCREMENT,
    user_id BIGINT FK → users.id,
    token VARCHAR UK NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE
)
```

### course_db

```sql
categories (
    id BIGINT PK AUTO_INCREMENT,
    name VARCHAR UK NOT NULL,
    slug VARCHAR NOT NULL
)

instructors (
    id BIGINT PK AUTO_INCREMENT,
    name VARCHAR NOT NULL,
    bio TEXT
)

courses (
    id BIGINT PK AUTO_INCREMENT,
    category_id BIGINT FK → categories.id,
    instructor_id BIGINT FK → instructors.id,
    title VARCHAR NOT NULL,
    description TEXT,
    level ENUM('PRINCIPIANTE', 'INTERMEDIO', 'AVANZADO'),
    total_lessons INT DEFAULT 0,
    active BOOLEAN DEFAULT TRUE
)

lessons (
    id BIGINT PK AUTO_INCREMENT,
    course_id BIGINT FK → courses.id,
    order_number INT NOT NULL,
    title VARCHAR NOT NULL,
    content_cache TEXT,
    content_generated_at TIMESTAMP
)
```

### enrollment_db

```sql
enrollments (
    id BIGINT PK AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    status ENUM('IN_PROGRESS', 'COMPLETED') DEFAULT 'IN_PROGRESS',
    progress_pct INT DEFAULT 0,
    enrolled_at TIMESTAMP DEFAULT NOW(),
    completed_at TIMESTAMP
)

lesson_progress (
    id BIGINT PK AUTO_INCREMENT,
    enrollment_id BIGINT FK → enrollments.id,
    lesson_id BIGINT NOT NULL,
    completed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP
)

learning_paths (
    id BIGINT PK AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    goal VARCHAR NOT NULL,
    level VARCHAR,
    hours_per_week VARCHAR,
    created_at TIMESTAMP DEFAULT NOW()
)

path_courses (
    id BIGINT PK AUTO_INCREMENT,
    path_id BIGINT FK → learning_paths.id,
    course_id BIGINT NOT NULL,
    order_number INT NOT NULL
)
```

---

## Endpoints de la API

### auth-service (8081)

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| POST | /api/auth/register | — | Registro de usuario |
| POST | /api/auth/login | — | Login, devuelve JWT + refresh token |
| POST | /api/auth/refresh | — | Renueva JWT con refresh token |
| POST | /api/auth/logout | JWT | Revoca el refresh token |
| GET | /api/auth/me | JWT | Datos del usuario autenticado |
| PUT | /api/auth/me | JWT | Actualiza nombre / email |

### course-service (8082)

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| GET | /api/courses | — | Lista cursos (filtros: categoría, nivel, búsqueda) |
| GET | /api/courses/{id} | — | Detalle de un curso |
| GET | /api/courses/{id}/lessons | — | Temario del curso |
| GET | /api/courses/{id}/lessons/{lessonId} | JWT | Detalle de una clase |
| GET | /api/categories | — | Lista de categorías |
| POST | /api/courses | ADMIN | Crear curso |
| PUT | /api/courses/{id} | ADMIN | Editar curso |
| POST | /api/courses/{id}/lessons | ADMIN | Agregar clase |

### enrollment-service (8083)

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| POST | /api/enrollments | JWT | Inscribirse a un curso |
| GET | /api/enrollments/me | JWT | Mis cursos (en progreso y completados) |
| GET | /api/enrollments/me/{courseId} | JWT | Progreso en un curso |
| POST | /api/enrollments/me/{courseId}/lessons/{lessonId}/complete | JWT | Marcar clase como completada |
| GET | /api/paths/me | JWT | Mis rutas generadas |
| GET | /api/paths/me/{pathId} | JWT | Detalle de una ruta |
| POST | /api/paths | INTERNAL | Persistir ruta (solo llamado por ai-service) |

### ai-service (8084)

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| POST | /api/ai/chat | — | Mensaje al chat conversacional |
| POST | /api/ai/recommend | JWT | Genera y persiste ruta de aprendizaje |
| POST | /api/ai/lessons/{lessonId}/content | JWT | Genera contenido de clase (con cache) |

---

## Flujos importantes

### Generación de ruta
1. Frontend → `ai-service` POST `/api/ai/chat` (conversación sin auth)
2. `ai-service` → `course-service` via Feign (obtiene catálogo)
3. `ai-service` → API Groq externa (genera ruta con catálogo como contexto)
4. `ai-service` → `enrollment-service` POST `/api/paths` (persiste la ruta)
5. `ai-service` → Frontend (devuelve ruta generada)

### Contenido de clase
1. Frontend → `ai-service` POST `/api/ai/lessons/{lessonId}/content`
2. `ai-service` → `course-service` via Feign (verifica si hay `content_cache`)
3. Si existe → devuelve el cache directamente
4. Si no → llama a API Groq, guarda en `content_cache`, devuelve

### Inscripción a un curso
1. Usuario sin sesión puede explorar catálogo, ver cursos y generar rutas
2. Al hacer clic en "Comenzar curso" → aparece modal de registro/login
3. Una vez autenticado → Frontend → `enrollment-service` POST `/api/enrollments`

---

## Plan de implementación por fases

- **Fase 1**: `eureka-server` + `config-server` + `api-gateway` + `auth-service` + Docker Compose base
- **Fase 2**: `course-service` con CRUD, instructores, categorías y datos de prueba (`data.sql`)
- **Fase 3**: `enrollment-service` con inscripciones, progreso por clase y rutas generadas
- **Fase 4**: `ai-service` con chat conversacional, generación de rutas e integración con API Groq
- **Fase 5**: Frontend React consumiendo todos los servicios
- **Fase 6**: Manejo de errores global, validaciones, seguridad, README completo

---

## Dependencias base (pom.xml de cada microservicio)

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>

<properties>
    <java.version>17</java.version>
    <spring-cloud.version>2023.0.0</spring-cloud.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Todos los microservicios (auth, course, enrollment, ai) incluyen:
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `spring-boot-starter-actuator`
- `spring-cloud-starter-netflix-eureka-client`
- `spring-cloud-starter-config`
- `mysql-connector-j`
- `lombok`

JWT (solo auth-service y api-gateway):
- `io.jsonwebtoken:jjwt-api:0.11.5`
- `io.jsonwebtoken:jjwt-impl:0.11.5`
- `io.jsonwebtoken:jjwt-jackson:0.11.5`

---

## Configuración por servicio

Cada microservicio tiene un `application.yml` mínimo — solo nombre y conexión al config-server:

```yaml
server:
  port: XXXX

spring:
  application:
    name: nombre-del-servicio
  config:
    import: "optional:configserver:http://localhost:8888"
```

Todo lo demás (datasource, eureka, jwt, rutas del gateway, etc.) va en el archivo correspondiente dentro del config-server:

```
configurations/
    auth-service.yml
    api-gateway.yml
    course-service.yml
    enrollment-service.yml
    ai-service.yml
```

El config-server encuentra el archivo correcto usando el `spring.application.name` de cada servicio.

### ai-service

**Dependencia Spring AI:**
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
</dependency>
```

BOM de Spring AI en `dependencyManagement`:
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-bom</artifactId>
    <version>1.0.0-M6</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

**`configurations/ai-service.yml`** (en config-server):
```yaml
spring:
  ai:
    openai:
      api-key: ${GROQ_API_KEY}
      base-url: https://api.groq.com/openai
      chat:
        options:
          model: llama-3.3-70b-versatile
          max-tokens: 1000
```

**Uso en el servicio:**
```java
@Autowired
private ChatClient chatClient;

String respuesta = chatClient.prompt()
    .user(prompt)
    .call()
    .content();
```

**Variables de entorno** — agregar al `docker-compose.yml` en `ai-service`:
```yaml
environment:
  GROQ_API_KEY: ${GROQ_API_KEY}
```

**Flujo de generación de ruta:**
1. Frontend manda el objetivo y respuestas del chat a `POST /api/ai/recommend`
2. `ai-service` consulta el catálogo completo a `course-service` via Feign
3. Construye un prompt con el objetivo del usuario y el catálogo disponible
4. Llama a Groq via Spring AI y parsea la respuesta como lista ordenada de cursos
5. Llama a `enrollment-service` via Feign para persistir la ruta (`POST /api/paths`)
6. Devuelve la ruta generada al frontend

**Flujo de contenido de clase:**
1. Frontend llama a `POST /api/ai/lessons/{lessonId}/content`
2. `ai-service` consulta `course-service` via Feign para ver si hay `content_cache`
3. Si existe → devuelve el cache directamente sin llamar a Groq
4. Si no → genera el contenido con Spring AI, lo guarda en `content_cache` via Feign, devuelve

**Feign Client hacia `course-service`:**
```java
@FeignClient(name = "course-service")
public interface CourseClient {
    @GetMapping("/api/courses")
    ApiResponse<List<CourseDto>> getAllCourses();

    @GetMapping("/api/courses/{id}/lessons/{lessonId}")
    ApiResponse<LessonDto> getLessonById(@PathVariable Long id,
                                          @PathVariable Long lessonId);

    @PutMapping("/api/courses/{id}/lessons/{lessonId}/cache")
    ApiResponse<Void> updateContentCache(@PathVariable Long id,
                                          @PathVariable Long lessonId,
                                          @RequestBody ContentCacheRequest request);
}
```

**Feign Client hacia `enrollment-service`:**
```java
@FeignClient(name = "enrollment-service")
public interface EnrollmentClient {
    @PostMapping("/api/paths")
    ApiResponse<LearningPathDto> savePath(@RequestBody SavePathRequest request);
}
```

---



- Nombres de paquetes: `com.devpath.{servicio}` — ej: `com.devpath.auth`, `com.devpath.course`
- DTOs para todas las requests y responses — nunca exponer entidades JPA directamente
- Manejo de excepciones con `@ControllerAdvice` global en cada servicio (`GlobalExceptionHandler.java`)
- Validaciones con `@Valid` y anotaciones de Bean Validation (`@NotNull`, `@Email`, etc.)
- Respuestas siempre envueltas en un objeto estándar:

```java
{
    "status": "success" | "error",
    "message": "...",
    "data": { ... }
}
```

- Comunicación entre servicios siempre via Feign — nunca `RestTemplate` con URL hardcodeada
- URLs de servicios resueltas por Eureka — nunca hardcodeadas

---

## Variables de entorno y secretos

**Nunca hardcodear secretos en el código ni en los `application.yml` de cada servicio.**

### Flujo

```
.env → docker-compose → variable de entorno del contenedor
                              ↓
                      config-server lee ${JWT_SECRET}
                              ↓
                   auth-service y api-gateway reciben jwt.secret
```

### Archivos

**`.env`** (en `devpath/`, en `.gitignore` — nunca se sube a GitHub):
```env
JWT_SECRET=mi-clave-secreta-super-larga-de-al-menos-256-bits
MYSQL_ROOT_PASSWORD=root
GROQ_API_KEY=gsk_...
```

**`.env.example`** (en `devpath/`, sí se sube a GitHub):
```env
# JWT
JWT_SECRET=

# MySQL
MYSQL_ROOT_PASSWORD=

# Groq
GROQ_API_KEY=
```

**`configurations/auth-service.yml`** (en config-server):
```yaml
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000
  refresh-expiration: 604800000
```

**`configurations/api-gateway.yml`** (en config-server):
```yaml
jwt:
  secret: ${JWT_SECRET}
```

**`docker-compose.yml`**:
```yaml
auth-service:
  environment:
    JWT_SECRET: ${JWT_SECRET}

api-gateway:
  environment:
    JWT_SECRET: ${JWT_SECRET}
```

### Regla importante
`JWT_SECRET` debe ser la **misma clave** en `auth-service` y `api-gateway` — `auth-service` la usa para firmar el token y `api-gateway` para validarlo.

---

## Detalles de implementación por servicio

### enrollment-service

**Cómo obtener el usuario autenticado:**
El gateway propaga el email del usuario como header `X-User-Id`. En los controllers del `enrollment-service` se obtiene así:

```java
@PostMapping
public ResponseEntity<ApiResponse<EnrollmentDto>> enroll(
        @RequestHeader("X-User-Id") String userEmail,
        @RequestBody EnrollmentRequest request) {
    return ResponseEntity.ok(enrollmentService.enroll(userEmail, request));
}
```

El `X-User-Id` contiene el **email** del usuario (el `subject` del JWT), no un ID numérico.

**Feign Client hacia `course-service`:**
```java
@FeignClient(name = "course-service")
public interface CourseClient {
    @GetMapping("/api/courses/{id}")
    ApiResponse<CourseDto> getCourseById(@PathVariable Long id);

    @GetMapping("/api/courses/{id}/lessons/{lessonId}")
    ApiResponse<LessonDto> getLessonById(@PathVariable Long id,
                                          @PathVariable Long lessonId);
}
```

**Feign Client hacia `auth-service`:**
```java
@FeignClient(name = "auth-service")
public interface AuthClient {
    @GetMapping("/api/auth/me")
    ApiResponse<UserDto> getUserByEmail(
        @RequestHeader("X-User-Id") String email
    );
}
```

**Flujo de inscripción:**
1. Controller recibe `userEmail` del header `X-User-Id` y `courseId` del body
2. Llama a `course-service` via Feign para verificar que el curso existe
3. Si existe, crea la inscripción en `enrollment_db`
4. Devuelve el enrollment enriquecido con datos del curso

**Flujo de progreso:**
- `progress_pct` en `enrollments` se recalcula cada vez que se completa una clase
- Fórmula: `(clases completadas / total_lessons del curso) * 100`
- Cuando `progress_pct` llega a 100, `status` cambia a `COMPLETED`

**Endpoint INTERNAL `/api/paths`:**
- Solo lo llama `ai-service` — no está expuesto al frontend
- El gateway debe configurarse para bloquearlo desde el exterior
- Recibe la ruta generada por la IA y la persiste en `learning_paths` + `path_courses`

---

## Lo que NO hacer

- No exponer entidades JPA directamente en los controllers — siempre DTOs
- No crear FK entre bases de datos de distintos servicios — son referencias externas por ID
- No llamar entre servicios sin Feign
- No hardcodear URLs de servicios — usar Eureka
- No regenerar contenido de clases si ya existe en `content_cache`
- No inscribir automáticamente al usuario cuando se genera una ruta — son acciones independientes
- No usar ni crear `user-service` — no existe en este proyecto, los datos del usuario están en `auth-service`
