package dev.kayzao.nyc311.web;

import dev.kayzao.nyc311.model.ServiceRequest;
import dev.kayzao.nyc311.service.ServiceRequestService;
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
 * REST controller for handling requests to the /api/311 endpoint.
 * Supports optional filtering by bounding box and creation time.
 * Returns a JSON response with a list of service requests.
 */
@RestController
@RequestMapping("/api/311")
public class ServiceRequestController {

    private final ServiceRequestService service;

    public ServiceRequestController(ServiceRequestService service) {
        this.service = service;
    }

    /* GET /api/311 */
    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime since,
            @RequestParam(required = false) String bbox) {

        // ---- Parse bbox (if present) ----
        boolean hasBbox = false;
        double minLon = 0.0, minLat = 0.0, maxLon = 0.0, maxLat = 0.0;

        if (bbox != null && !bbox.isBlank()) {
            String[] parts = bbox.split(",");
            if (parts.length != 4) {
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

        // ---- Delegate to service for business logic / DB access ----
        List<ServiceRequest> rows = service.searchRequests(
                limit, since, hasBbox, minLon, minLat, maxLon, maxLat
        );

        // ---- Map entities to JSON-friendly structures ----
        List<Map<String, Object>> items = new ArrayList<>();

        for (ServiceRequest r : rows) {
            Map<String, Object> item = new LinkedHashMap<>();

            item.put("service_request_number", r.getServiceRequestNumber());
            item.put("created_date", r.getCreatedDate());
            item.put("closed_date", r.getClosedDate());
            item.put("status", r.getStatus());
            item.put("agency", r.getAgency());
            item.put("complaint_type", r.getComplaintType());
            item.put("descriptor", r.getDescriptor());
            item.put("borough", r.getBorough());

            Double latitude = r.getLatitude();
            Double longitude = r.getLongitude();

            Point pt = r.getGeom();
            if (pt != null && (latitude == null || longitude == null)) {
                longitude = pt.getX();
                latitude = pt.getY();
            }
            item.put("latitude", latitude);
            item.put("longitude", longitude);

            items.add(item);
        }

        // ---- Build response body (allowing null next_token) ----
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", items);
        body.put("next_token", null); // placeholder for future keyset pagination

        return ResponseEntity.ok(body);
    }
}
