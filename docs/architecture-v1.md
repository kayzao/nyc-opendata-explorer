# Architecture v1 (Current)

## Components
- Frontend: React app hosted on S3 and served via CloudFront.
- API: Spring Boot service (Java 21) running as a Docker container on EC2.
- Database: PostgreSQL with PostGIS (RDS in AWS, PostGIS container for local development).
- Ingestion: Scheduled job that pulls NYC OpenData (Socrata) and upserts into Postgres.
- Observability: Spring Boot Actuator + CloudWatch in AWS deployments.

## Local Development Flow
Browser/Client -> `http://localhost:8080/api/311`

API (host via Gradle) -> Dockerized PostGIS (`localhost:5432`)

## Production-like Local Flow
Browser/Client -> API container (`localhost:8080`) -> DB container (`db:5432`)

## Guardrails Implemented
- Request limit is clamped to 1..100
- Bounding box format and coordinate ranges are validated
- Optional `since` filter on `created_date`
