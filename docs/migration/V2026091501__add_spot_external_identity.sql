ALTER TABLE spot
    ADD COLUMN IF NOT EXISTS external_source VARCHAR(30),
    ADD COLUMN IF NOT EXISTS external_id VARCHAR(255);

CREATE UNIQUE INDEX IF NOT EXISTS uk_spot_external_source_id
    ON spot (external_source, external_id)
    WHERE external_source IS NOT NULL
      AND external_id IS NOT NULL;
