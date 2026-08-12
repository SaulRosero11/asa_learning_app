# ASA E-Learning — Backend

> **Plataforma de gestión de aprendizaje (LMS)** desarrollada para la ONG **Solidaridad y Acción (ASA)** como proyecto de vinculación con la comunidad de la Universidad de las Américas (UDLA).

## Descripción del proyecto

ASA E-Learning es un sistema de gestión de aprendizaje diseñado específicamente para digitalizar y optimizar el proceso formativo de los beneficiarios de la ONG Solidaridad y Acción. La organización trabaja con comunidades vulnerables brindando programas de capacitación en diversas áreas; sin embargo, la gestión manual de estos procesos generaba pérdida de información, dificulades de seguimiento y barreras de acceso para los participantes.

Este sistema resuelve esas necesidades al ofrecer:
- Gestión centralizada de programas de formación
- Inscripción de beneficiarios mediante invitaciones por correo
- Contenido multimedia organizado en módulos y lecciones
- Evaluaciones con calificación automática y manual
- Analítica en tiempo real del progreso de los beneficiarios
- Comunidad interna (foro y noticias) por programa

## Stack Tecnológico

| Componente | Tecnología | Versión |
|---|---|---|
| Framework | Spring Boot | 4.0.6 |
| Lenguaje | Java | 21 |
| Base de datos | PostgreSQL | 15+ |
| Migraciones | Flyway | 10+ |
| Seguridad | Spring Security + JWT | JJWT 0.12.6 |
| Autenticación social | Google OAuth2 | — |
| Email | Spring Mail (SMTP) | — |
| Documentación API | SpringDoc OpenAPI (Swagger) | 2.8.8 |
| Mapeo de objetos | MapStruct + Lombok | 1.6.3 |
| Validación | Jakarta Bean Validation | — |
| Testing | JUnit 5 + Testcontainers | — |
| Build | Maven | 3.9+ |

## Requisitos previos

- **Java 21** (JDK)
- **Maven 3.9+** o usar el wrapper incluido (`./mvnw`)
- **PostgreSQL 15+** corriendo localmente
- **SMTP** habilitado (Gmail App Password, Hostinger, etc.)
- **Google Cloud** proyecto con OAuth2 configurado (opcional para login social)

## Configuración

### 1. Base de datos

```sql
-- Crear la base de datos
CREATE DATABASE asa_elearning;
```

### 2. Variables de entorno

Crea el archivo `src/main/resources/application-local.properties` (ignorado por git) con tus valores:

```properties
# Base de datos
DB_URL=jdbc:postgresql://localhost:5432/asa_elearning
DB_USERNAME=postgres
DB_PASSWORD=tu_password

# JWT (mínimo 32 caracteres)
JWT_SECRET=cambia-este-secreto-seguro-minimo-32-caracteres

# Google OAuth2
GOOGLE_CLIENT_ID=tu-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=tu-client-secret

# Email SMTP
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu@email.com
MAIL_PASSWORD=tu-app-password
MAIL_FROM=noreply@solidaridadyaccion.org

# URLs
FRONTEND_URL=http://localhost:3000
CORS_ALLOWED_ORIGINS=http://localhost:3000

# Dominio autorizado para roles de administrador
ASA_AUTH_ALLOWED_ADMIN_DOMAIN=solidaridadyaccion.org
ASA_SEED_ADMIN_EMAIL=admin@solidaridadyaccion.org
```

### 3. Google OAuth2 (opcional)

En la [Google Cloud Console](https://console.cloud.google.com):
- Authorized redirect URIs: `http://localhost:8080/login/oauth2/code/google`
- Authorized JavaScript origins: `http://localhost:8080`

## Ejecución local

```bash
# Clonar repositorio
git clone <url-del-repo>
cd asa-e-learning

# Opción A — Maven wrapper (recomendado)
./mvnw spring-boot:run

# Opción B — Maven instalado
mvn spring-boot:run

# Opción C — JAR compilado
mvn package -Dmaven.test.skip=true
java -jar target/asa-e-learning-0.0.1-SNAPSHOT.jar
```

Al arrancar por primera vez, si no existe un `SUPER_ADMIN`, el sistema crea uno automáticamente con la dirección configurada en `ASA_SEED_ADMIN_EMAIL` y una contraseña aleatoria que se imprime en los logs. **Cambiar la contraseña inmediatamente.**

## Documentación del API

Con el servidor corriendo, accede a:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## Endpoints principales

### Autenticación
| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| POST | `/api/v1/auth/login` | Login email/contraseña | Público |
| GET | `/oauth2/authorization/google` | Login con Google | Público |
| POST | `/api/v1/auth/refresh` | Renovar token | Público |
| POST | `/api/v1/auth/logout` | Cerrar sesión | Autenticado |
| GET | `/api/v1/auth/me` | Usuario actual | Autenticado |
| POST | `/api/v1/auth/change-password` | Cambiar contraseña | Autenticado |
| POST | `/api/v1/auth/forgot-password` | Solicitar reset | Público |
| POST | `/api/v1/auth/reset-password` | Completar reset | Público |
| POST | `/api/v1/auth/register` | Registrar estudiante | Público |

### Programas
| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| GET | `/api/v1/programs` | Listar programas | Autenticado |
| POST | `/api/v1/programs` | Crear programa | Admin/Leader |
| GET | `/api/v1/programs/{id}` | Detalle programa | Autenticado |
| PUT | `/api/v1/programs/{id}` | Actualizar programa | Admin/Leader |
| PATCH | `/api/v1/programs/{id}/status` | Cambiar estado | Admin/Leader |
| POST | `/api/v1/programs/{id}/invitations` | Invitar estudiantes | Admin/Leader |
| POST | `/api/v1/invitations/redeem` | Canjear invitación | Público |

### Evaluaciones
| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| GET | `/api/v1/programs/{id}/assessments` | Listar evaluaciones | Autenticado |
| POST | `/api/v1/programs/{id}/assessments` | Crear evaluación | Admin/Leader |
| PUT | `/api/v1/assessments/{id}` | Editar evaluación | Admin/Leader |
| POST | `/api/v1/assessments/{id}/submit` | Enviar respuestas | Estudiante |
| GET | `/api/v1/assessments/{id}/results` | Ver resultados grupales | Admin/Leader |
| PUT | `/api/v1/responses/{id}/grade` | Calificar respuesta manual | Admin/Leader |

### Módulos y Lecciones
| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| GET | `/api/v1/programs/{id}/modules` | Listar módulos con progreso | Autenticado |
| POST | `/api/v1/programs/{id}/modules` | Crear módulo | Admin/Leader |
| POST | `/api/v1/modules/{id}/lessons` | Crear lección | Admin/Leader |
| POST | `/api/v1/lessons/{id}/complete` | Marcar lección completada | Estudiante |

### Analítica
| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| GET | `/api/v1/analytics/global` | KPIs globales | Admin/Leader |
| GET | `/api/v1/analytics/me/history` | Mi historial | Autenticado |
| GET | `/api/v1/analytics/me/pending` | Actividades pendientes | Autenticado |

## Roles del sistema

| Rol | Descripción |
|---|---|
| `STUDENT` | Beneficiario inscrito en programas |
| `PROGRAM_LEADER` | Coordinador que gestiona uno o más programas |
| `ADMIN` | Administrador con acceso a múltiples programas |
| `SUPER_ADMIN` | Administrador global (acceso total) |

Solo los correos del dominio configurado en `ASA_AUTH_ALLOWED_ADMIN_DOMAIN` pueden recibir roles elevados (LEADER, ADMIN, SUPER_ADMIN).

## Estructura del proyecto

```
src/main/java/com/springboot/asa/learning/
├── application/usecase/     # Casos de uso (lógica de negocio)
├── domain/                  # Modelos de dominio, enums, excepciones
├── infrastructure/
│   ├── config/              # Configuraciones (JWT, CORS, OpenAPI)
│   ├── email/               # Servicio de correo
│   ├── persistence/
│   │   ├── entity/          # Entidades JPA (21 entidades)
│   │   └── repository/      # Repositorios Spring Data
│   └── security/            # Filtros JWT, OAuth2, seed
└── presentation/
    ├── controller/          # REST Controllers (10 controladores)
    ├── dto/                 # Request y Response DTOs
    └── handler/             # Manejo global de excepciones

src/main/resources/
├── db/migration/            # Scripts Flyway (V1–V12)
└── application.properties   # Configuración base
```

## Migraciones de base de datos

Flyway gestiona automáticamente el esquema al arrancar:

| Versión | Descripción |
|---|---|
| V1 | Roles del sistema |
| V2 | Usuarios, perfiles, roles de usuario |
| V3 | Programas e inscripciones |
| V4 | Módulos, lecciones, progreso |
| V5 | Evaluaciones, preguntas, opciones, intentos, respuestas |
| V6 | Foros, posts, noticias |
| V7 | Refresh tokens, tokens de reset de contraseña |
| V8 | Preguntas de onboarding |
| V9–V12 | Tabla puente program_leaders |

## Testing

```bash
# Ejecutar tests (requiere Docker para Testcontainers)
./mvnw test

# Saltar tests en build
./mvnw package -Dmaven.test.skip=true
```

## Producción

Para producción, configura todas las variables de entorno del sistema operativo o del servicio de despliegue (Railway, Render, AWS, etc.) en lugar de usar el archivo properties. Variables críticas:

- `JWT_SECRET` — debe ser una cadena aleatoria segura de mínimo 32 caracteres
- `DB_PASSWORD` — contraseña segura de la base de datos
- `MAIL_PASSWORD` — contraseña de aplicación SMTP
- `GOOGLE_CLIENT_SECRET` — secreto de la app de Google Cloud
- `CORS_ALLOWED_ORIGINS` — URL de producción del frontend
- `FRONTEND_URL` — URL pública del frontend (para links en emails)
#   a s a _ l e a r n i n g _ a p p  
 