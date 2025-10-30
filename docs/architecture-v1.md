# Architecture v1 (MVP)

## Components
- **Frontend**: React app hosted on S3, served via CloudFront with ACM TLS.
- **API**: FastAPI app running in AWS Lambda (container) behind API Gateway (HTTP API).
- **Database**: Amazon RDS for PostgreSQL with PostGIS, accessed through RDS Proxy.
- **Storage**: Amazon S3 for static site and nightly raw snapshots (CSV/Parquet).
- **Ingestion**: EventBridge (scheduler) → Lambda (public subnet) → Socrata → Postgres + S3.
- **Observability**: CloudWatch (logs/metrics/alarms), X-Ray (traces).
- **Secrets & State**: Secrets Manager (DB creds), Parameter Store (high-water mark).
- **Networking**:
  - API Lambda in **private subnets** (no public internet), with **VPC endpoints** (S3, Secrets, Logs).
  - Ingestion Lambda in **public subnet** (internet egress to Socrata), **no DB security group access**.

## Simple Flow
User (browser) → CloudFront → (S3 for frontend)  
Frontend → API Gateway → Lambda (private subnet) → RDS Proxy → RDS Postgres(PostGIS)

Nightly: EventBridge → Lambda (public subnet) → Socrata → (UPSERT) RDS + (snapshot) S3

## Guardrails
- Max date window per request: 90 days
- Max page size: 100
- Bbox area capped (WGS84 / SRID 4326)
- API Gateway throttling; optional caching for hot GETs
