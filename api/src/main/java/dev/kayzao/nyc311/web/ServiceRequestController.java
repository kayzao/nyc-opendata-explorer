package dev.kayzao.nyc311.web;

import dev.kayzao.nyc311.model.ServiceRequest;
import dev.kayzao.nyc311.repo.ServiceRequestRepo;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/requests")

public class ServiceRequestController {
    private final ServiceRequestRepo repo;

    public ServiceRequestController(ServiceRequestRepo repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime since,
            @RequestParam(required = false) String bbox) {

        // Guardrail: clamp limit to [1, 100]
        limit = Math.min(Math.max(limit, 1), 100);

        boolean hasBbox = false;
        Double minLon = null, minLat = null, maxLon = null, maxLat = null;

        if (bbox != null && !bbox.isBlank()) {
            String[] p = bbox.split(",");
            if (p.length != 4) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "bbox must be 'minLon,minLat,maxLon,maxLat'"));
            }
            try {
                minLon = Double.valueOf(p[0]);
                minLat = Double.valueOf(p[1]);
                maxLon = Double.valueOf(p[2]);
                maxLat = Double.valueOf(p[3]);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "bbox values must be numbers"));
            }
            if (!(minLon < maxLon && minLat < maxLat)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "bbox must have min<max for lon and lat"));
            }
            if (minLon < -180 || maxLon > 180 || minLat < -90 || maxLat > 90) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "bbox out of WGS84 range"));
            }
            hasBbox = true;
        }

        List<ServiceRequest> rows = repo.search(limit, since, hasBbox, minLon, minLat, maxLon, maxLat);

        // Map entities to simple JSON-friendly DTOs
        List<Map<String, Object>> items = new ArrayList<>();
        for (ServiceRequest r : rows) {
            var pt = r.getGeom(); // JTS Point; X = lon, Y = lat
            items.add(Map.of(
                    "external_id", r.getExternalId(),
                    "created_at", r.getCreatedAt(),
                    "status", r.getStatus(),
                    "agency", r.getAgency(),
                    "complaint_type", r.getComplaintType(),
                    "descriptor", r.getDescriptor(),
                    "location", Map.of("lon", pt.getX(), "lat", pt.getY())));
        }

        return ResponseEntity.ok(Map.of(
                "items", items,
                "next_token", null // placeholder for future pagination
        ));
    }
}
