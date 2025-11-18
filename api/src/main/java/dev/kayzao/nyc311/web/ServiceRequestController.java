package dev.kayzao.nyc311.web;

import dev.kayzao.nyc311.model.ServiceRequest;
import dev.kayzao.nyc311.repo.ServiceRequestRepo;
import org.locationtech.jts.geom.Point;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * REST controller for handling requests to the /requests endpoint.
 * Supports optional filtering by bounding box and creation time.
 * Returns a JSON response with a list of service requests.
 */
@RestController
@RequestMapping("/requests")
public class ServiceRequestController {

    private final ServiceRequestRepo repo;

    public ServiceRequestController(ServiceRequestRepo repo) {
        this.repo = repo;
    }

    /* GET /requests */
    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime since,
            @RequestParam(required = false) String bbox) {
        // Clamp limit to [1, 100] to enforce guardrail
        int clampedLimit = Math.min(Math.max(limit, 1), 100);

        // ---- Parse bbox (if present) ----
        boolean hasBbox = false;
        double minLon = 0.0, minLat = 0.0, maxLon = 0.0, maxLat = 0.0;

        if (bbox != null && !bbox.isBlank()) {
            String[] parts = bbox.split(",");
            if (parts.length != 4) {
                // Note: Map.of is safe here because neither key nor value is null
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "bbox must be 'minLon,minLat,maxLon,maxLat'"));
            }
            try {
                minLon = Double.parseDouble(parts[0]);
                minLat = Double.parseDouble(parts[1]);
                maxLon = Double.parseDouble(parts[2]);
                maxLat = Double.parseDouble(parts[3]);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "bbox values must be numbers"));
            }

            // Basic ordering check
            if (!(minLon < maxLon && minLat < maxLat)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "bbox must have min<max for both lon and lat"));
            }

            // WGS84 bounds check
            if (minLon < -180 || maxLon > 180 || minLat < -90 || maxLat > 90) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "bbox out of WGS84 range"));
            }

            hasBbox = true;
        }

        boolean hasSince = (since != null);

        // ---- Choose which query to run based on since/bbox ----
        List<ServiceRequest> rows;
        if (!hasSince && !hasBbox) {
            rows = repo.searchPlain(clampedLimit);
        } else if (hasSince && !hasBbox) {
            rows = repo.searchSince(clampedLimit, since);
        } else if (!hasSince && hasBbox) {
            rows = repo.searchBbox(clampedLimit, minLon, minLat, maxLon, maxLat);
        } else { // hasSince && hasBbox
            rows = repo.searchSinceBbox(clampedLimit, since, minLon, minLat, maxLon, maxLat);
        }

        // ---- Map entities to JSON-friendly structures ----
        List<Map<String, Object>> items = new ArrayList<>();

        for (ServiceRequest r : rows) {
            Map<String, Object> item = new LinkedHashMap<>();

            item.put("external_id", r.getExternalId());
            item.put("created_at", r.getCreatedAt());
            item.put("status", r.getStatus());
            item.put("agency", r.getAgency());
            item.put("complaint_type", r.getComplaintType());
            item.put("descriptor", r.getDescriptor());

            Point pt = r.getGeom();
            if (pt != null) {
                Map<String, Object> location = new LinkedHashMap<>();
                location.put("lon", pt.getX());
                location.put("lat", pt.getY());
                item.put("location", location);
            } else {
                // Explicitly store null if geometry is missing
                item.put("location", null);
            }

            items.add(item);
        }

        // ---- Build response body (allowing null next_token) ----
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", items);
        body.put("next_token", null); // placeholder for future keyset pagination

        return ResponseEntity.ok(body);
    }
}
