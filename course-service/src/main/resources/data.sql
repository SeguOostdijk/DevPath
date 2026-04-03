-- ── Categorías ──────────────────────────────────────────────────────────────
INSERT INTO categories (name, slug) VALUES
('Backend', 'backend'),
('Frontend', 'frontend'),
('Mobile', 'mobile'),
('Data Science', 'data-science'),
('DevOps', 'devops');

-- ── Instructores ─────────────────────────────────────────────────────────────
INSERT INTO instructors (name, bio) VALUES
('Carlos Méndez', 'Ingeniero de software con 10 años de experiencia en Java y Spring Boot. Especialista en arquitectura de microservicios.'),
('Ana García', 'Desarrolladora frontend con expertise en React, TypeScript y diseño de interfaces modernas.'),
('Roberto López', 'Experto en desarrollo móvil y DevOps. Ha trabajado en startups y empresas Fortune 500.'),
('María Fernández', 'Científica de datos con doctorado en Machine Learning. Investigadora y docente universitaria.');

-- ── Cursos ───────────────────────────────────────────────────────────────────
INSERT INTO courses (category_id, instructor_id, title, description, level, total_lessons, active) VALUES
-- Backend
(1, 1, 'Java desde Cero',
 'Aprende Java desde los fundamentos hasta programación orientada a objetos. Ideal para quienes nunca programaron.',
 'PRINCIPIANTE', 8, true),
(1, 1, 'Spring Boot: APIs REST',
 'Construye APIs REST profesionales con Spring Boot 3, JPA, validaciones y manejo de errores.',
 'INTERMEDIO', 8, true),
(1, 1, 'Microservicios con Spring Cloud',
 'Diseña e implementa una arquitectura de microservicios con Eureka, Gateway, Config Server y Feign.',
 'AVANZADO', 8, true),
-- Frontend
(2, 2, 'JavaScript Moderno (ES6+)',
 'Domina JavaScript moderno: arrow functions, destructuring, promesas, async/await y módulos ES6.',
 'PRINCIPIANTE', 8, true),
(2, 2, 'React: Aplicaciones Web',
 'Crea SPAs con React, hooks, Context API, React Router y conexión a APIs REST.',
 'INTERMEDIO', 8, true),
(2, 2, 'TypeScript Avanzado',
 'Tipos avanzados, genéricos, decoradores y patrones de diseño en TypeScript para proyectos grandes.',
 'AVANZADO', 8, true),
-- Mobile
(3, 3, 'Flutter desde Cero',
 'Desarrolla apps iOS y Android con Flutter y Dart. De cero a tu primera app publicada.',
 'PRINCIPIANTE', 8, true),
(3, 3, 'Kotlin para Android',
 'Desarrollo Android nativo con Kotlin, Jetpack Compose, ViewModel y consumo de APIs.',
 'INTERMEDIO', 8, true),
-- Data Science
(4, 4, 'Python para Data Science',
 'Introducción a Python enfocada en análisis de datos con NumPy, Pandas y visualización con Matplotlib.',
 'PRINCIPIANTE', 8, true),
(4, 4, 'Machine Learning con Python',
 'Algoritmos supervisados y no supervisados, validación de modelos y deployment con Scikit-learn.',
 'INTERMEDIO', 8, true),
-- DevOps
(5, 3, 'Docker y Kubernetes',
 'Conteneriza aplicaciones con Docker y orquesta con Kubernetes. Incluye Docker Compose y Helm.',
 'INTERMEDIO', 8, true),
(5, 1, 'CI/CD con GitHub Actions',
 'Automatiza build, tests y deploy con GitHub Actions. Pipelines para proyectos Java y Node.js.',
 'AVANZADO', 8, true);

-- ── Lecciones — Java desde Cero (curso 1) ────────────────────────────────────
INSERT INTO lessons (course_id, order_number, title) VALUES
(1, 1, 'Introducción a Java y configuración del entorno'),
(1, 2, 'Variables, tipos de datos y operadores'),
(1, 3, 'Estructuras de control: if, for, while'),
(1, 4, 'Arrays y colecciones básicas'),
(1, 5, 'Métodos y parámetros'),
(1, 6, 'Clases y objetos: fundamentos de OOP'),
(1, 7, 'Herencia, interfaces y polimorfismo'),
(1, 8, 'Manejo de excepciones y buenas prácticas');

-- ── Lecciones — Spring Boot: APIs REST (curso 2) ─────────────────────────────
INSERT INTO lessons (course_id, order_number, title) VALUES
(2, 1, 'Introducción a Spring Boot y creación del proyecto'),
(2, 2, 'Controladores REST y mapeo de rutas'),
(2, 3, 'DTOs, serialización JSON y validaciones'),
(2, 4, 'Spring Data JPA: entidades y repositorios'),
(2, 5, 'Relaciones entre entidades: OneToMany, ManyToOne'),
(2, 6, 'Manejo global de excepciones'),
(2, 7, 'Paginación, filtros y búsquedas'),
(2, 8, 'Seguridad básica con Spring Security y JWT');

-- ── Lecciones — Microservicios con Spring Cloud (curso 3) ────────────────────
INSERT INTO lessons (course_id, order_number, title) VALUES
(3, 1, 'Arquitectura de microservicios: conceptos y patrones'),
(3, 2, 'Eureka Server: registro y descubrimiento de servicios'),
(3, 3, 'Spring Cloud Config: configuración centralizada'),
(3, 4, 'Spring Cloud Gateway: routing y filtros'),
(3, 5, 'Feign Client: comunicación entre servicios'),
(3, 6, 'Circuit Breaker con Resilience4j'),
(3, 7, 'Distributed Tracing con Zipkin'),
(3, 8, 'Dockerización y orquestación con Docker Compose');

-- ── Lecciones — JavaScript Moderno (curso 4) ─────────────────────────────────
INSERT INTO lessons (course_id, order_number, title) VALUES
(4, 1, 'ES6: let, const, arrow functions y template literals'),
(4, 2, 'Destructuring, spread y rest operators'),
(4, 3, 'Módulos ES6: import y export'),
(4, 4, 'Promesas y manejo de asincronía'),
(4, 5, 'Async/Await: código asíncrono limpio'),
(4, 6, 'Fetch API y consumo de servicios REST'),
(4, 7, 'Clases y programación orientada a objetos en JS'),
(4, 8, 'Event Loop, closures y this en profundidad');

-- ── Lecciones — React: Aplicaciones Web (curso 5) ────────────────────────────
INSERT INTO lessons (course_id, order_number, title) VALUES
(5, 1, 'Fundamentos de React y JSX'),
(5, 2, 'Componentes funcionales y props'),
(5, 3, 'useState y useEffect: hooks esenciales'),
(5, 4, 'Context API y manejo de estado global'),
(5, 5, 'React Router: navegación en SPAs'),
(5, 6, 'Consumo de APIs con fetch y axios'),
(5, 7, 'Formularios controlados y validaciones'),
(5, 8, 'Optimización: useMemo, useCallback y lazy loading');

-- ── Lecciones — TypeScript Avanzado (curso 6) ────────────────────────────────
INSERT INTO lessons (course_id, order_number, title) VALUES
(6, 1, 'Tipos avanzados: union, intersection y literal types'),
(6, 2, 'Genéricos y restricciones de tipo'),
(6, 3, 'Utilidades de tipo: Partial, Pick, Omit, Record'),
(6, 4, 'Decoradores y metadata'),
(6, 5, 'Módulos y namespaces en proyectos grandes'),
(6, 6, 'Configuración de TypeScript para producción'),
(6, 7, 'Patrones de diseño en TypeScript'),
(6, 8, 'Testing con Jest y TypeScript');

-- ── Lecciones — Flutter desde Cero (curso 7) ─────────────────────────────────
INSERT INTO lessons (course_id, order_number, title) VALUES
(7, 1, 'Dart: fundamentos del lenguaje'),
(7, 2, 'Flutter: widgets y árbol de componentes'),
(7, 3, 'Layouts: Column, Row, Stack y Expanded'),
(7, 4, 'Navegación entre pantallas'),
(7, 5, 'Estado con setState y Provider'),
(7, 6, 'Consumo de APIs REST en Flutter'),
(7, 7, 'Almacenamiento local con SharedPreferences'),
(7, 8, 'Build y publicación en Play Store / App Store');

-- ── Lecciones — Kotlin para Android (curso 8) ────────────────────────────────
INSERT INTO lessons (course_id, order_number, title) VALUES
(8, 1, 'Kotlin: sintaxis, funciones y null safety'),
(8, 2, 'Jetpack Compose: UI declarativa'),
(8, 3, 'ViewModel y LiveData'),
(8, 4, 'Room: base de datos local'),
(8, 5, 'Retrofit: consumo de APIs REST'),
(8, 6, 'Coroutines y Flow para asincronía'),
(8, 7, 'Navegación con Navigation Component'),
(8, 8, 'Pruebas unitarias y de UI');

-- ── Lecciones — Python para Data Science (curso 9) ───────────────────────────
INSERT INTO lessons (course_id, order_number, title) VALUES
(9, 1, 'Python: tipos de datos, listas y diccionarios'),
(9, 2, 'NumPy: arrays multidimensionales y operaciones'),
(9, 3, 'Pandas: Series y DataFrames'),
(9, 4, 'Limpieza y transformación de datos'),
(9, 5, 'Visualización con Matplotlib y Seaborn'),
(9, 6, 'Análisis exploratorio de datos (EDA)'),
(9, 7, 'Estadística descriptiva e inferencial'),
(9, 8, 'Proyecto final: análisis de dataset real');

-- ── Lecciones — Machine Learning con Python (curso 10) ───────────────────────
INSERT INTO lessons (course_id, order_number, title) VALUES
(10, 1, 'Introducción al Machine Learning y tipos de aprendizaje'),
(10, 2, 'Regresión lineal y logística'),
(10, 3, 'Árboles de decisión y Random Forest'),
(10, 4, 'SVM y K-Nearest Neighbors'),
(10, 5, 'Clustering: K-Means y DBSCAN'),
(10, 6, 'Validación de modelos: cross-validation y métricas'),
(10, 7, 'Feature engineering y selección de variables'),
(10, 8, 'Deployment de modelos con FastAPI');

-- ── Lecciones — Docker y Kubernetes (curso 11) ───────────────────────────────
INSERT INTO lessons (course_id, order_number, title) VALUES
(11, 1, 'Docker: conceptos, imágenes y contenedores'),
(11, 2, 'Dockerfile: construir imágenes personalizadas'),
(11, 3, 'Docker Compose: orquestación local'),
(11, 4, 'Redes y volúmenes en Docker'),
(11, 5, 'Kubernetes: arquitectura y componentes'),
(11, 6, 'Pods, Deployments y Services en K8s'),
(11, 7, 'ConfigMaps, Secrets e Ingress'),
(11, 8, 'Helm: gestión de paquetes en Kubernetes');

-- ── Lecciones — CI/CD con GitHub Actions (curso 12) ──────────────────────────
INSERT INTO lessons (course_id, order_number, title) VALUES
(12, 1, 'Fundamentos de CI/CD y DevOps'),
(12, 2, 'GitHub Actions: workflows, jobs y steps'),
(12, 3, 'Triggers: push, pull_request y schedule'),
(12, 4, 'Pipeline para proyectos Java con Maven'),
(12, 5, 'Pipeline para proyectos Node.js'),
(12, 6, 'Build y push de imágenes Docker'),
(12, 7, 'Deploy automático a servidores y cloud'),
(12, 8, 'Secrets, environments y aprobaciones manuales');
