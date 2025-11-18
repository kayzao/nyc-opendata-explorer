package dev.kayzao.nyc311.repo;

import dev.kayzao.nyc311.model.ServiceRequest;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

/*
 * Repository interface for accessing ServiceRequest entities.
 * Provides methods to search service requests with optional filters for bounding box and creation time.
 */

public interface ServiceRequestRepo extends JpaRepository<ServiceRequest, Long> {

    // no bbox, no since
    @Query(value = """
        SELECT * FROM service_requests
         ORDER BY created_at DESC, id DESC
         LIMIT :limit
    """, nativeQuery = true)
    List<ServiceRequest> searchPlain(
        @Param("limit") int limit);

    // since only
    @Query(value = """
        SELECT * FROM service_requests
         WHERE created_at >= :since
         ORDER BY created_at DESC, id DESC
         LIMIT :limit
    """, nativeQuery = true)
    List<ServiceRequest> searchSince(
        @Param("limit") int limit,
        @Param("since") OffsetDateTime since);

    // bbox only
    @Query(value = """
        SELECT * FROM service_requests
         WHERE ST_Contains(
                ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326),
                geom)
         ORDER BY created_at DESC, id DESC
         LIMIT :limit
    """, nativeQuery = true)
    List<ServiceRequest> searchBbox(
        @Param("limit") int limit,
        @Param("minLon") double minLon, @Param("minLat") double minLat,
        @Param("maxLon") double maxLon, @Param("maxLat") double maxLat);

    // since + bbox
    @Query(value = """
        SELECT * FROM service_requests
         WHERE created_at >= :since
           AND ST_Contains(
                ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326),
                geom)
         ORDER BY created_at DESC, id DESC
         LIMIT :limit
    """, nativeQuery = true)
    List<ServiceRequest> searchSinceBbox(
        @Param("limit") int limit,
        @Param("since") OffsetDateTime since,
        @Param("minLon") double minLon, @Param("minLat") double minLat,
        @Param("maxLon") double maxLon, @Param("maxLat") double maxLat);
}