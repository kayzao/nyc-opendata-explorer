# TODO:

- A GitHub repo created with a clear folder structure.

- A first-pass API contract (docs/openapi.yaml) describing endpoints, filters, pagination, and errors.

    - Open API Notes:
        - Open API just defines a specific syntax to describe APIs and how to consume them
        - Ensures our API is both human and machine readable
        - Used by other software to generate code

- A draft database schema with PostGIS (db/schema.sql) including spatial and common indexes.

- A short architecture document (docs/architecture-v1.md) describing components and data flow.

- 2–3 example API requests/responses and a small guardrails section (limits on date range, bbox area, page size).