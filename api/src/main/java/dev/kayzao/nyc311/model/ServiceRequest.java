package dev.kayzao.nyc311.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import org.locationtech.jts.geom.Point;

/*
 * This file defines the ServiceRequest entity, which maps to a row of the
 * service_requests table in the PostGIS database. It includes fields for
 * various attributes of a 311 service request, including a geometry field
 * for location data.
 */

@Entity
@Table(name = "service_requests")
public class ServiceRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Primary key for the service_requests table
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true)
    // Unique external identifier for the service request
    private String externalId;

    @Column(name = "created_at", nullable = false)
    // Timestamp when the service request was created
    private OffsetDateTime createdAt;

    // Timestamp when the service request was closed
    private OffsetDateTime closedAt;
    // Current status of the service request
    private String status;
    // Agency responsible for handling the service request
    private String agency;

    // Type of complaint for the service request
    @Column(name = "complaint_type")
    private String complaintType;

    // Detailed descriptor of the service request
    private String descriptor;

    // Map PostGIS geometry(Point,4326) -> JTS Point
    @Column(columnDefinition = "geometry(Point,4326)")
    private Point geom;

    @Column(columnDefinition = "jsonb")
    private String raw;

    // --- getters and setters (generate via your IDE) ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(OffsetDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public String getComplaintType() {
        return complaintType;
    }

    public void setComplaintType(String complaintType) {
        this.complaintType = complaintType;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public Point getGeom() {
        return geom;
    }

    public void setGeom(Point geom) {
        this.geom = geom;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }
}
