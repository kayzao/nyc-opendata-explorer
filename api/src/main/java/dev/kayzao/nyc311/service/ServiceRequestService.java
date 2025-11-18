package dev.kayzao.nyc311.service;

import dev.kayzao.nyc311.model.ServiceRequest;
import dev.kayzao.nyc311.repo.ServiceRequestRepo;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Service layer for 311 service requests.
 * Encapsulates business rules like limit clamping and query selection.
 */
@Service
public class ServiceRequestService {

    private final ServiceRequestRepo repo;

    public ServiceRequestService(ServiceRequestRepo repo) {
        this.repo = repo;
    }

    /**
     * Runs the appropriate search query based on the presence of since/bbox,
     * and enforces the limit guardrail.
     */
    public List<ServiceRequest> searchRequests(
            int limit,
            OffsetDateTime since,
            boolean hasBbox,
            double minLon,
            double minLat,
            double maxLon,
            double maxLat
    ) {
        // Clamp limit to [1, 100] to enforce guardrail
        int clampedLimit = Math.min(Math.max(limit, 1), 100);
        boolean hasSince = (since != null);

        if (!hasSince && !hasBbox) {
            return repo.searchPlain(clampedLimit);
        } else if (hasSince && !hasBbox) {
            return repo.searchSince(clampedLimit, since);
        } else if (!hasSince && hasBbox) {
            return repo.searchBbox(clampedLimit, minLon, minLat, maxLon, maxLat);
        } else { // hasSince && hasBbox
            return repo.searchSinceBbox(clampedLimit, since, minLon, minLat, maxLon, maxLat);
        }
    }
}
