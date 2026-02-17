# NYC OpenData Explorer

NYC 311 service requests API with geospatial filtering, designed to run on an EC2 instance and be publicly reachable at `kayzao.dev`.

**Goal:** a single EC2 host running Dockerized Spring Boot + PostGIS, fronted by Traefik for TLS, accessible via `kayzao.dev`.

## What’s Currently Offered

- Spring Boot 3 API (Java 21) with a `GET /requests` endpoint.
- Postgres + PostGIS schema and Flyway migrations.
- Bounding-box filtering and `since` filtering.
- Docker Compose for local dev and for EC2 deployment.
- Health endpoint at `GET /actuator/health`.

API implementation lives in:
- `api/src/main/java/dev/kayzao/nyc311/web/ServiceRequestController.java`
- `api/src/main/java/dev/kayzao/nyc311/service/ServiceRequestService.java`
- `api/src/main/java/dev/kayzao/nyc311/repo/ServiceRequestRepo.java`

## Local Setup

**Prereqs**
- Docker Desktop
- Java 21
- Git

**Start**
1. Start Docker Desktop.
2. From repo root:
   - `docker-compose -f docker-compose.dev.yaml up --build`
3. API is available at `http://localhost:8080/requests`
4. Health check at `http://localhost:8080/actuator/health`

**Stop**
- `docker compose stop` (keeps data)
- `docker compose down -v` (removes data)

## EC2 Setup (Production Host)

This repo targets a single EC2 instance with Docker + Traefik and DNS pointed at `kayzao.dev`.

**Prereqs on EC2**
- Docker + Docker Compose
- DNS A record for `kayzao.dev` pointing to the EC2 public IP
- Ports 80/443 open in the EC2 security group

**Start**
1. Clone repo on EC2.
2. From repo root:
   - `docker-compose -f docker-compose.yaml up -d`
3. Traefik handles TLS and routes `kayzao.dev` to the backend container.
4. Backend should be reachable at `https://kayzao.dev/requests`

**Notes**
- `docker-compose.yaml` includes Traefik and Portainer in addition to DB + backend.
- The backend container uses `SPRING_PROFILES_ACTIVE=prod`.

## Final Goals

- Serve a React + Leaflet frontend at `https://kayzao.dev`.
- API endpoints expanded to match `docs/openapi.yaml`:
  - `/api/311` with pagination and guardrails
  - `/api/311/stats`
- Automated ingestion of NYC OpenData into PostGIS.
- Observability and operational hardening on the EC2 host.

## License / Credits

Data from NYC OpenData (Socrata) — follow their terms of use and attribution guidance.

This project is for demonstration/educational purposes.
