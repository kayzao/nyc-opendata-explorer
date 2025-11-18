package dev.kayzao.nyc311.repo;

import dev.kayzao.nyc311.model.ServiceRequest;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ServiceRequestRepo extends JpaRepository<ServiceRequest, Long> {

    @Query(value = """
              SELECT * FROM service_requests
               WHERE (:since IS NULL OR created_at >= :since)
                 AND (:hasBbox = false OR ST_Contains(
                      ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326),
                      geom))
               ORDER BY created_at DESC, id DESC
               LIMIT :limit
            """, nativeQuery = true)
    List<ServiceRequest> search(
            @Param("limit") int limit,
            @Param("since") OffsetDateTime since,
            @Param("hasBbox") boolean hasBbox,
            @Param("minLon") Double minLon, @Param("minLat") Double minLat,
            @Param("maxLon") Double maxLon, @Param("maxLat") Double maxLat);
}