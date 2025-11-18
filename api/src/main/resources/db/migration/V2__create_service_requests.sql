-- Second action: Create the service_requests table to store 311 data

CREATE TABLE IF NOT EXISTS service_requests (
  id BIGSERIAL PRIMARY KEY,
  external_id TEXT UNIQUE NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  closed_at TIMESTAMPTZ,
  status TEXT NOT NULL,
  agency TEXT,
  complaint_type TEXT,
  descriptor TEXT,
  geom geometry(Point, 4326) NOT NULL,
  raw JSONB
);

-- Index by time for cheap date filters
CREATE INDEX IF NOT EXISTS idx_sr_created_at ON service_requests(created_at);

-- Spatial index for fast bbox queries
CREATE INDEX IF NOT EXISTS idx_sr_geom ON service_requests USING GIST (geom);
