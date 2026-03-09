# Job Application Tracker

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.3-6DB33F?style=flat&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue-3.4-4FC08D?style=flat&logo=vue.js)](https://vuejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?style=flat&logo=typescript)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat&logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat&logo=docker)](https://www.docker.com/)

A full-stack web application for tracking job applications through every stage of the hiring process — from initial application to offer or rejection.

## Features

- **Authentication** — JWT-based register/login with access token (15 min) and refresh token (7 days); auto-refresh on expiry
- **Application Management** — Full CRUD for job applications with company, position, salary, location, URL, and notes
- **Advanced Filtering** — Filter by status, company name, and date range with paginated results
- **Status History** — Every status change is automatically logged with timestamps
- **Kanban Board** — Drag-and-drop board to visually manage applications across 8 status columns
- **Dashboard** — Chart.js visualizations: status breakdown (pie), timeline trends (line), and conversion funnel (bar)
- **Application Detail** — Comprehensive view with a vertical status-change timeline
- **API Documentation** — Swagger UI available at `/swagger-ui.html`

## Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Java 17, Spring Boot 3.2.3, Spring Security, Spring Data JPA |
| Auth | JWT (jjwt 0.11.5) — access token 15 min, refresh token 7 days |
| Database | PostgreSQL 16, Flyway migrations |
| Frontend | Vue 3.4 (Composition API), Vite 5, TypeScript |
| State | Pinia |
| Routing | Vue Router 4 (with navigation guards) |
| HTTP | Axios with JWT interceptor + auto-refresh |
| UI | Tailwind CSS 3, Chart.js via vue-chartjs |
| Containerization | Docker, Docker Compose (multi-stage builds) |
| API Docs | SpringDoc OpenAPI 2.3 (Swagger UI) |

## Quick Start with Docker

The easiest way to run the full stack is with Docker Compose:

```bash
# 1. Clone the repository
git clone https://github.com/hamamciserhad/job-application-tracker.git
cd job-application-tracker

# 2. (Optional) Copy and configure environment variables — defaults work out of the box
cp .env.example .env

# 3. Build and start all services
docker-compose up --build
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:80 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |

To stop: `docker-compose down`  
To reset the database: `docker-compose down -v`

## Local Development

### Prerequisites

- Java 17+
- Maven 3.9+
- Node.js 20+
- PostgreSQL 16 (or Docker for the database only)

### Backend

```bash
# Start only the database
docker-compose up -d postgres

# Run the backend
cd backend
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

The dev server runs at `http://localhost:5173` and proxies `/api` requests to `http://localhost:8080`.

### Backend Tests

```bash
cd backend
mvn test
```

35 tests: `ApplicationServiceTest` (11), `UserServiceTest` (8), `StatsServiceTest` (6), `ApplicationControllerTest` (9), `contextLoads` (1).

## Environment Variables

Copy `.env.example` to `.env` and adjust as needed:

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `jobtracker` | Database name |
| `DB_USER` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password |
| `JWT_SECRET` | *(see .env.example)* | JWT signing secret — **change in production** |
| `SERVER_PORT` | `8080` | Backend server port |

## API Reference

All endpoints except `/api/auth/**` require `Authorization: Bearer <access_token>`.

Full interactive documentation: **[Swagger UI](http://localhost:8080/swagger-ui.html)**

### Auth

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Login, returns access + refresh tokens |
| `POST` | `/api/auth/refresh` | Exchange refresh token for new access token |
| `GET` | `/api/auth/me` | Get current authenticated user |

### Applications

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/applications` | List applications (paginated, filterable) |
| `POST` | `/api/applications` | Create a new application |
| `GET` | `/api/applications/{id}` | Get application by ID |
| `PUT` | `/api/applications/{id}` | Update an application |
| `DELETE` | `/api/applications/{id}` | Delete an application |
| `PATCH` | `/api/applications/{id}/status` | Update application status |
| `GET` | `/api/applications/{id}/history` | Get status change history |

**Query params for `GET /api/applications`:** `status`, `companyName`, `fromDate`, `toDate`, `page`, `size`, `sort`

### Stats

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/stats/overview` | Application counts per status |
| `GET` | `/api/stats/timeline` | Weekly application counts (last 12 weeks) |
| `GET` | `/api/stats/conversion` | Funnel conversion rates |

## Application Statuses

```
APPLIED → PHONE_SCREEN → INTERVIEW → TECHNICAL_TEST → OFFER → ACCEPTED
                                                              ↘ REJECTED
                                                              ↘ WITHDRAWN
```

## Standard Response Formats

**Success:**
```json
{ "data": { "..." }, "timestamp": "2024-01-15T10:30:00" }
```

**Paginated:**
```json
{ "content": [...], "page": 0, "size": 10, "totalElements": 120, "totalPages": 12 }
```

**Error:**
```json
{ "error": "NOT_FOUND", "message": "Application not found", "status": 404, "timestamp": "..." }
```

## Project Structure

```
job-application-tracker/
├── backend/                        # Spring Boot application
│   ├── src/main/java/com/jobtracker/
│   │   ├── config/                 # OpenAPI config
│   │   ├── controller/             # REST controllers
│   │   ├── service/                # Business logic
│   │   ├── repository/             # JPA repositories
│   │   ├── entity/                 # JPA entities
│   │   ├── dto/                    # Request/Response DTOs
│   │   ├── mapper/                 # Entity ↔ DTO mappers
│   │   ├── security/               # JWT + Spring Security
│   │   └── exception/              # Global exception handling
│   └── src/main/resources/
│       ├── application.yml
│       └── db/migration/           # Flyway SQL migrations
├── frontend/                       # Vue 3 application
│   └── src/
│       ├── views/                  # Page-level components
│       ├── components/             # Reusable UI components
│       ├── stores/                 # Pinia stores
│       ├── api/                    # Axios instance + interceptors
│       └── router/                 # Vue Router configuration
├── docker-compose.yml
└── .env.example
```
