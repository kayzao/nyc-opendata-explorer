package dev.kayzao.nyc311.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "service_requests")
public class ServiceRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_request_number", nullable = false, unique = true)
    private String serviceRequestNumber;

    @Column(name = "created_date", nullable = false)
    private OffsetDateTime createdDate;

    @Column(name = "closed_date")
    private OffsetDateTime closedDate;

    private String status;
    private String agency;

    @Column(name = "complaint_type")
    private String complaintType;

    private String descriptor;

    private String borough;

    private Double latitude;

    private Double longitude;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point geom;

    @Column(columnDefinition = "jsonb")
    private String raw;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServiceRequestNumber() {
        return serviceRequestNumber;
    }

    public void setServiceRequestNumber(String serviceRequestNumber) {
        this.serviceRequestNumber = serviceRequestNumber;
    }

    public OffsetDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(OffsetDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public OffsetDateTime getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(OffsetDateTime closedDate) {
        this.closedDate = closedDate;
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

    public String getBorough() {
        return borough;
    }

    public void setBorough(String borough) {
        this.borough = borough;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
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
