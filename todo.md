# Notes:

- Open API Notes:
    - Open API just defines a specific syntax to describe APIs and how to consume them
    - Ensures our API is both human and machine readable
    - Used by other software to generate code
    - Use editor.swagger.io to explore API

- A draft database schema with PostGIS (db/schema.sql) including spatial and common indexes.
    - SQL that defines the schema

- A short architecture document (docs/architecture-v1.md) describing components and data flow.

- Utilize Docker Compose to set up PostGIS
    - Docker Compose is a tool for defining and running multi-container applications. It maanges services, networks, and volumes in one YAML file
    - Provides way of documenting and configuring all of the application's serviec dependencies
    - `Service` - Computing resource within an app that can be scaled/replaced independently from other components. They're backed by set of containers, thus defined by a Docker image and set of runtime args. All containers withing a service are identically created.
        - A service definition contains the config applied to each service container
        - Each service may also have a build section, defining how to create the Docker image for it.
        - `deploy` groups the runtime constraints and lets platform adjust the deployment strat to best match containers' needs.
    - Compose starts and stops containers in dependcy order, which are determined by depends_on, links, volumes_from, and network_mode: "service:..."
        - `condition` - option for dependency, could be `service_started`, `service_health`, etc..
    - Volume - Persistent data stores of containers. When mounted into a container, the directory containing it is mounted into it. They're ideal for persisting data generated/used by Docker container, while bind mounts are dependent on directory strucure and OS of the host machine. 
        - Volumes are easier to back up/migrate than bind mounts, can be managed by Docker CLI or API, are more safely shared amongst containers.
        - As the volume is managed by Docker, it is difficult to access files from the host.
    - `docker compose exec` runs a command inside the specified service container