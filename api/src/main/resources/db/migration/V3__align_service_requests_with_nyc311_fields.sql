-- Align table shape with NYC 311 field semantics while keeping the same table name.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'service_requests' AND column_name = 'external_id'
    ) THEN
        ALTER TABLE service_requests RENAME COLUMN external_id TO service_request_number;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'service_requests' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE service_requests RENAME COLUMN created_at TO created_date;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'service_requests' AND column_name = 'closed_at'
    ) THEN
        ALTER TABLE service_requests RENAME COLUMN closed_at TO closed_date;
    END IF;
END $$;

ALTER TABLE service_requests ADD COLUMN IF NOT EXISTS borough TEXT;
ALTER TABLE service_requests ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION;
ALTER TABLE service_requests ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;

ALTER TABLE service_requests ALTER COLUMN geom DROP NOT NULL;

-- Keep lat/lon synchronized for existing rows where only geom is populated.
UPDATE service_requests
SET longitude = ST_X(geom),
    latitude = ST_Y(geom)
WHERE geom IS NOT NULL
  AND (longitude IS NULL OR latitude IS NULL);

CREATE INDEX IF NOT EXISTS idx_sr_created_date ON service_requests(created_date);
CREATE INDEX IF NOT EXISTS idx_sr_complaint_type_date ON service_requests(complaint_type, created_date DESC);
CREATE INDEX IF NOT EXISTS idx_sr_borough_date ON service_requests(borough, created_date DESC);
