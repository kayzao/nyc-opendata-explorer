-- Enable PostGIS (spatial) features
CREATE EXTENSION IF NOT EXISTS postgis;

-- Main table for NYC 311 requests
CREATE TABLE IF NOT EXISTS requests (
  service_request_number TEXT PRIMARY KEY,
  created_date           TIMESTAMPTZ NOT NULL,
  closed_date            TIMESTAMPTZ NULL,
  complaint_type         TEXT NOT NULL,
  descriptor             TEXT NULL,
  borough                TEXT NULL,
  latitude               DOUBLE PRECISION NULL,
  longitude              DOUBLE PRECISION NULL,

  -- Spatial point in WGS84 (SRID 4326) built from lon/lat
  geom                   geometry(Point, 4326),

  -- Optional: Keep raw source fields for auditing/future use
  raw                    JSONB
);

-- Spatial index for map/bbox queries
CREATE INDEX IF NOT EXISTS idx_requests_geom
  ON requests USING GIST (geom);

-- Filter by time quickly
CREATE INDEX IF NOT EXISTS idx_requests_created
  ON requests (created_date);

-- Common filter: type + recent first
CREATE INDEX IF NOT EXISTS idx_requests_type_date
  ON requests (complaint_type, created_date DESC);

-- Optional: borough+date filters
CREATE INDEX IF NOT EXISTS idx_requests_borough_date
  ON requests (borough, created_date DESC);