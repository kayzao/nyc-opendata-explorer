# NYC OpenData Explorer

NYC 311 explorer backend using Spring Boot + PostgreSQL/PostGIS.

A web app that visualizes NYC 311 service requests on a map with filters and analytics.

Backend is Spring Boot (Gradle) on AWS EC2 behind an Application Load Balancer; data is in PostgreSQL + PostGIS on Amazon RDS; nightly ingestion pulls from Socrata via a cron job on EC2; static frontend is on S3 + CloudFront with ACM TLS.

## Prerequisites

- Docker Desktop
- Java 21 (for local Gradle API runs)


## Architecture:

- **Backend:** Spring Boot 3 (Java 21, Gradle) running in Docker on **AWS EC2**, optionally behind an Application Load Balancer  
- **Database:** PostgreSQL + PostGIS on **Amazon RDS**  
- **Frontend:** React + Leaflet, built with Vite and hosted on **S3 + CloudFront** (ACM TLS)  
- **Ingestion:** Nightly job on EC2 that pulls from NYC OpenData (Socrata) and upserts into RDS  


## Tech Stack

**Languages**

- Java 21 (backend)
- TypeScript / JavaScript (frontend)
- SQL (PostgreSQL/PostGIS)

**Backend**

- Spring Boot 3
- Gradle (build tool)
- springdoc OpenAPI (auto-generated API docs)
- Flyway (database migrations)
- Hibernate + PostGIS via `hibernate-spatial`
- AWS SDK (Secrets Manager, etc.)

**Database**

- PostgreSQL 16 + PostGIS (RDS in AWS, Postgres container locally)

**Frontend**

- React
- Leaflet.js
- Vite

**AWS (planned / in progress)**

- EC2 (Dockerized Spring Boot app)
- RDS Postgres + PostGIS
- S3 + CloudFront (static frontend)
- ACM (TLS certs)
- CloudWatch (logs/metrics)
- Secrets Manager (DB credentials)
- Route 53 (optional custom domain)

## Development (Recommended)

Run Postgres/PostGIS in Docker and run the API directly with Gradle for a faster edit/debug cycle.

All commands below run from repo root.

1. Start the database:
   - `docker compose -f docker-compose.dev.yaml up -d`
2. Start the API (Windows PowerShell/cmd):
   - `.\api\gradlew.bat bootRun`
3. Start the API (macOS/Linux/git-bash):
   - `./api/gradlew bootRun`
4. Verify health:
   - `http://localhost:8080/actuator/health`

### Dev connection details

- DB host: `localhost`
- DB port: `5432`
- DB name: `nyc311`
- DB user: `appuser`
- DB password: `appsecret`

The default API datasource in `api/src/main/resources/application.yml` already targets `localhost:5432`, so `bootRun` works with `docker-compose.dev.yaml` out of the box.

### Stop dev environment

- Stop API: `Ctrl+C` in the Gradle terminal
- Stop DB container: `docker compose -f docker-compose.dev.yaml down`
- Remove dev DB data volume too: `docker compose -f docker-compose.dev.yaml down -v`

## Production Run (Full Docker Backend)

Run API + DB both as containers.

All commands below run from repo root.

1. Start full backend:
   - `docker compose -f docker-compose.prod.yaml build backend`
   - `docker compose -f docker-compose.prod.yaml up -d`
2. View logs:
   - `docker compose -f docker-compose.prod.yaml logs -f backend`
3. Verify health:
   - `http://localhost:8080/actuator/health`

### Stop production stack

- `docker compose -f docker-compose.prod.yaml down`
- Remove DB data volume too: `docker compose -f docker-compose.prod.yaml down -v`

## Key files

- `docker-compose.dev.yaml`: dev database only
- `docker-compose.prod.yaml`: full backend (database + API container)
- `Dockerfile`: API container build
- `api/`: Spring Boot project (Gradle)

## API docs

- OpenAPI spec: `docs/openapi.yaml`
- Swagger UI (when app is running): `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON (when app is running): `http://localhost:8080/v3/api-docs`

## Implemented endpoints

- `GET /api/311`
- `GET /actuator/health`

`GET /api/311` returns NYC 311-shaped fields:
- `service_request_number`
- `created_date`
- `closed_date`
- `complaint_type`
- `descriptor`
- `borough`
- `latitude`
- `longitude`
