NYC OpenData Explorer

A web app that visualizes NYC 311 service requests on a map with filters and analytics.

Backend is Spring Boot (Gradle) on AWS EC2 behind an Application Load Balancer; data is in PostgreSQL + PostGIS on Amazon RDS; nightly ingestion pulls from Socrata via a cron job on EC2; static frontend is on S3 + CloudFront with ACM TLS.

Query & visualize NYC 311 requests with filters: date range, borough, complaint type, and map bounding box.

Fast, indexed spatial queries using PostGIS; pagination and guardrails to keep queries snappy.

Nightly ingestion pulls the latest records from NYC OpenData (Socrata), UPSERTs into Postgres, and writes raw snapshots to S3.

Production hygiene: CloudWatch dashboards/alarms, X-Ray tracing, Secrets Manager, least-privilege IAM, CI/CD, and IaC.

🏗 Architecture (High Level)

Frontend: React + Leaflet, built with Vite → S3 → CloudFront (ACM TLS)

API: FastAPI → Lambda (container) with Lambda Web Adapter → API Gateway (HTTP API)

DB: RDS PostgreSQL + PostGIS, reached through RDS Proxy

Ingestion: EventBridge (schedule) → Lambda (public subnet) → Socrata → UPSERT to Postgres + snapshot to S3

Networking: API Lambda in private subnets (no NAT); VPC Endpoints for S3/Secrets/Logs; ingestion Lambda in public subnet for internet egress (no DB SG access)

Ops: CloudWatch logs/metrics/dashboards/alarms, X-Ray traces, optional WAF

🧰 Tech Stack

Languages: Python, SQL, TypeScript/JavaScript
Backend: FastAPI, SQLAlchemy/psycopg, (optional) Pandas, Alembic
Frontend: React, Leaflet.js, Vite
AWS: Lambda (container), API Gateway (HTTP API), RDS Postgres + PostGIS, RDS Proxy, S3, CloudFront, ACM, EventBridge, SQS (DLQ), Secrets Manager, SSM Parameter Store, CloudWatch, X-Ray, VPC (subnets, endpoints), Route 53 (optional)
Infra/CI: AWS CDK or Terraform, Docker + ECR, GitHub Actions (OIDC)

🗂 Repo Structure (suggested)
.
├─ docs/
│  ├─ architecture-v1.md
│  └─ openapi.yaml
├─ db/
│  └─ schema.sql
├─ infra/             # CDK or Terraform
│  ├─ cdk.json / main.tf
│  └─ lib/ or modules/
├─ api/
│  ├─ app/            # FastAPI app
│  ├─ Dockerfile
│  └─ requirements.txt / pyproject.toml
├─ ingestor/
│  ├─ handler/        # ingestion Lambda
│  ├─ Dockerfile
│  └─ requirements.txt / pyproject.toml
├─ web/
│  ├─ src/
│  ├─ index.html
│  └─ package.json
└─ README.md

⚡ Quick Start (Local, No AWS Required Yet)

You can run through Day 1–2 locally to validate shape.

Clone

git clone https://github.com/YOUR-USER/nyc-opendata-explorer.git
cd nyc-opendata-explorer


Backend (local dev)

cd api
python -m venv .venv && source .venv/bin/activate  # Windows: .venv\Scripts\activate
pip install -r requirements.txt
uvicorn app.main:app --reload
# API at http://127.0.0.1:8000 , docs at /docs


Frontend (local dev)

cd web
npm install
npm run dev
# Visit the local Vite URL it prints (e.g., http://127.0.0.1:5173)


For Dockerized local runs, build the api/ and ingestor/ images and run them with docker run binding ports as needed.

🔐 Configuration (Environment Variables)

Create an .env file per component (never commit real secrets):

API (Lambda / local)

DB_HOST=<rds-proxy-endpoint>
DB_PORT=5432
DB_USER=<db-user>
DB_PASSWORD=<db-password>        # Prefer Secrets Manager in AWS
DB_NAME=nyc311
DB_SSLMODE=require
MAX_DATE_WINDOW_DAYS=90
MAX_LIMIT=100
MAX_BBOX_AREA_DEG2=1.0           # example guardrail
ENABLE_XRAY=true


Ingestor (Lambda / local)

SOCRATA_BASE=https://data.cityofnewyork.us
SOCRATA_APP_TOKEN=<token>
SOCRATA_DATASET_ID=<dataset-id>  # e.g., 'erm2-nwe9' (verify current dataset)
INGEST_OVERLAP_HOURS=48
S3_SNAPSHOT_BUCKET=<bucket-name>
S3_SNAPSHOT_PREFIX=snapshots
HWM_PARAM_NAME=/nyc311/ingest/high_water_mark
DB_* (same as API if this job writes to DB; in this project, ingestion writes to DB)


In AWS, store secrets in Secrets Manager and non-secret config (like high-water mark path) in SSM Parameter Store.

🗄 Database Schema (PostGIS)

See db/schema.sql (run via Alembic migration in practice):

CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE IF NOT EXISTS requests (
  service_request_number TEXT PRIMARY KEY,
  created_date           TIMESTAMPTZ NOT NULL,
  closed_date            TIMESTAMPTZ NULL,
  complaint_type         TEXT NOT NULL,
  descriptor             TEXT NULL,
  borough                TEXT NULL,
  latitude               DOUBLE PRECISION NULL,
  longitude              DOUBLE PRECISION NULL,
  geom                   geometry(Point, 4326),
  raw                    JSONB
);

CREATE INDEX IF NOT EXISTS idx_requests_geom
  ON requests USING GIST (geom);

CREATE INDEX IF NOT EXISTS idx_requests_created
  ON requests (created_date);

CREATE INDEX IF NOT EXISTS idx_requests_type_date
  ON requests (complaint_type, created_date DESC);

CREATE INDEX IF NOT EXISTS idx_requests_borough_date
  ON requests (borough, created_date DESC);

☁️ Cloud Initialization (IaC First)

Choose CDK or Terraform. CDK example steps below (TypeScript). Terraform users: mirror resources with modules.

Bootstrap & Synth

cd infra
npm install         # if using CDK with TypeScript
npx cdk bootstrap
npx cdk synth
npx cdk deploy  # confirm changes; creates VPC, subnets, endpoints, S3, CloudFront, RDS, RDS Proxy, Secrets, Parameter, API, Lambdas, etc.


Outputs to Note

CloudFront domain

API Gateway base URL

RDS Proxy endpoint

S3 bucket names (web, snapshots)

Parameter Store HWM path

Secrets ARNs

If using GitHub Actions + OIDC, set up a deploy role and trust policy. Store any non-secret config in repo variables; avoid plaintext secrets.

🚀 Deploy
1) Build & Push Containers (API and Ingestor)
# API image
cd api
aws ecr get-login-password | docker login --username AWS --password-stdin <ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com
docker build -t nyc311-api:latest .
docker tag nyc311-api:latest <ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/nyc311-api:latest
docker push <ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/nyc311-api:latest

# Ingestor image
cd ../ingestor
docker build -t nyc311-ingestor:latest .
docker tag nyc311-ingestor:latest <ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/nyc311-ingestor:latest
docker push <ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/nyc311-ingestor:latest


Your IaC should reference these ECR images and update the Lambda functions.

2) Frontend → S3 + CloudFront
cd web
npm ci
npm run build
aws s3 sync dist/ s3://YOUR-WEB-BUCKET/ --delete
aws cloudfront create-invalidation --distribution-id YOUR_DIST_ID --paths "/*"

3) Database Migrations

Use Alembic (or apply db/schema.sql once if you prefer):

cd api
alembic upgrade head


Ensure the function role (or a one-time admin role) can run migrations, then drop to least-privilege for runtime.

⏱ Ingestion (EventBridge → Lambda)

Schedule: Cron/rate in EventBridge (e.g., nightly).

High-Water Mark: stored in SSM Parameter Store (ISO-8601 timestamp).

Overlap: refetch last 48h to capture late updates, then UPSERT on primary key.

Error Handling: Automatic retries; failures to SQS DLQ; CloudWatch alarm on DLQ depth.

Snapshots: Write CSV/Parquet per day to s3://YOUR-SNAPSHOT-BUCKET/year=YYYY/month=MM/day=DD/.

🔭 Observability & Security

CloudWatch Dashboards/Alarms: API 5xx, p95 latency, Lambda errors, RDS CPU/connections, DLQ depth.

X-Ray: Enable tracing on API Gateway & Lambdas.

IAM: Least privilege; function-scoped S3 prefixes; read-only params for API; write for ingestor only.

Secrets Manager: Rotate DB credentials; use in Lambda via env injection.

Network: API Lambda in private subnets w/ VPC endpoints (S3/Secrets/Logs). Ingestor Lambda in public subnet; no DB SG access.

🧪 API Contract

See docs/openapi.yaml for parameters, responses, and examples.

Core endpoints:

GET /healthz

GET /api/311 (filters: date_from, date_to, borough[], complaint_type[], bbox, limit, next_token)

GET /api/311/stats (group_by in {complaint_type, borough, day} + same filters)

Guardrails

Max date window: 90 days

Max page size: 100

Bbox area capped; return 400 for excessive area

Add Cache-Control for cacheable GETs

🧪 CI/CD (GitHub Actions + OIDC)

Workflows:

infra: synth/deploy CDK/Terraform

api: build → push ECR → update Lambda

ingestor: build → push ECR → update Lambda

web: build → S3 sync → CloudFront invalidation

Use OIDC to assume an AWS role (no stored AWS keys).

💸 Cost Tips

Avoid NAT (use VPC endpoints + split-subnet pattern)

RDS will be the main fixed cost (use smallest instance; right-size storage; enable stop/start in non-prod)

Lambda + API Gateway + CloudFront + S3 usually <$5/mo at low traffic

Consider API caching and CloudFront for hot GETs

🧩 Troubleshooting

API times out / cold starts: Keep container minimal; consider provisioned concurrency; ensure RDS Proxy is used.

DB connection errors: Verify security groups, RDS Proxy target group, credentials in Secrets Manager, SSL mode.

No internet from API Lambda: By design (private subnets, no NAT). Use VPC endpoints; do internet calls only in ingestion Lambda (public subnet).

CORS issues: Set CORS on API Gateway; align with frontend origin. CloudFront can add headers if needed.

CloudFront not updating: Run an invalidation after deploys.

📄 License / Credits

Data from NYC OpenData (Socrata) — follow their terms of use and attribution guidance.

This project is for demonstration/educational purposes.

✅ Definition of Done (MVP)

Frontend reachable over HTTPS via CloudFront

API responding behind API Gateway with guardrails

RDS Postgres + PostGIS online via RDS Proxy

Nightly ingestion succeeds, snapshots written to S3

CloudWatch dashboards/alarms in place

IaC + CI/CD green end-to-end
