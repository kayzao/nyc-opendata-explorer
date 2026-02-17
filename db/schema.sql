-- Reference schema only.
-- Flyway migrations under api/src/main/resources/db/migration are the source of truth.

CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE IF NOT EXISTS service_requests (
  id BIGSERIAL PRIMARY KEY,
  service_request_number TEXT UNIQUE NOT NULL,
  created_date TIMESTAMPTZ NOT NULL,
  closed_date TIMESTAMPTZ,
  status TEXT NOT NULL,
  agency TEXT,
  complaint_type TEXT,
  descriptor TEXT,
  borough TEXT,
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  geom geometry(Point, 4326),
  raw JSONB
);

CREATE INDEX IF NOT EXISTS idx_sr_created_date ON service_requests(created_date);
CREATE INDEX IF NOT EXISTS idx_sr_complaint_type_date ON service_requests(complaint_type, created_date DESC);
CREATE INDEX IF NOT EXISTS idx_sr_borough_date ON service_requests(borough, created_date DESC);
CREATE INDEX IF NOT EXISTS idx_sr_geom ON service_requests USING GIST (geom);
