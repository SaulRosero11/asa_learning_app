# ASA E-Learning — Backend

Plataforma de gestión de aprendizaje para la ONG **Solidaridad y Acción (ASA)**.

**Stack:** Spring Boot 4 · Java 17 · PostgreSQL · Flyway · Spring Security · JWT · Google OAuth2

---

## Requisitos previos

- Java 17+
- Maven 3.8+
- PostgreSQL 15+ corriendo localmente
- Base de datos creada: `CREATE DATABASE asa_elearning;`

---

## Variables de entorno

Configurables en `application.properties` o como variables de entorno del sistema (sobreescriben el properties).

### Base de datos

| Variable de entorno | Properties key | Default local |
|---|---|---|
| `DB_URL` | `spring.datasource.url` | `jdbc:postgresql://localhost:5432/asa_elearning` |
| `DB_USERNAME` | `spring.datasource.username` | `postgres` |
| `DB_PASSWORD` | `spring.datasource.password` | `postgres` |

### JWT y Seguridad

| Variable de entorno | Descripción | Default |
|---|---|---|
| `JWT_SECRET` | Clave secreta para firmar JWT (mín. 32 chars) | ⚠️ Cambiar en producción |
| `ASA_AUTH_ALLOWED_ADMIN_DOMAIN` | Dominio elegible para roles LEADER/ADMIN | `solidaridadyaccion.org` |
| `ASA_SEED_ADMIN_EMAIL` | Email del administrador global inicial | `admin@solidaridadyaccion.org` |

### Google OAuth2

| Variable de entorno | Descripción |
|---|---|
| `GOOGLE_CLIENT_ID` | Client ID de la app en Google Cloud Console |
| `GOOGLE_CLIENT_SECRET` | Client Secret de la app en Google Cloud Console |

**Configurar en Google Cloud Console:**
- Authorized redirect URIs: `http://localhost:8080/login/oauth2/code/google`
- Authorized JavaScript origins: `http://localhost:8080`

### Email / SMTP

| Variable de entorno | Descripción | Default |
|---|---|---|
| `MAIL_HOST` | Servidor SMTP | `smtp.gmail.com` |
| `MAIL_PORT` | Puerto SMTP | `587` |
| `MAIL_USERNAME` | Cuenta de correo | — |
| `MAIL_PASSWORD` | App Password de Gmail | — |
| `MAIL_FROM` | Dirección remitente | `noreply@solidaridadyaccion.org` |
| `FRONTEND_URL` | URL del frontend (para links en emails) | `http://localhost:3000` |

### CORS

| Variable de entorno | Descripción | Default |
|---|---|---|
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos (separados por coma) | `http://localhost:3000` |

---

## Ejecución local

```bash
# 1. Clonar y entrar al directorio
cd asa-e-learning

# 2. Crear la base de datos PostgreSQL
psql -U postgres -c "CREATE DATABASE asa_elearning;"

# 3. Arrancar (Flyway aplica el esquema automáticamente)
./mvnw spring-boot:run
```

Al arrancar por primera vez, si no existe un `SUPER_ADMIN`, el sistema genera credenciales aleatorias y las imprime en los logs. **Cambiar la contraseña inmediatamente.**

---

## Endpoints principales

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/v1/auth/login` | Login con email/contraseña |
| GET | `/oauth2/authorization/google` | Iniciar flujo Google OAuth2 |
| POST | `/api/v1/auth/refresh` | Renovar sesión |
| POST | `/api/v1/auth/logout` | Cerrar sesión |
| GET | `/api/v1/auth/me` | Datos del usuario actual |
| POST | `/api/v1/auth/forgot-password` | Solicitar reset de contraseña |
| POST | `/api/v1/auth/reset-password` | Completar reset de contraseña |
| POST | `/api/v1/auth/change-password` | Cambiar contraseña (obligatorio primer login) |
| GET | `/swagger-ui.html` | Documentación interactiva del API |
