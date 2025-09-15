package com.example.domainchecker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "domain_results")
public class DomainResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String domain;

    @Column(nullable = false)
    private Boolean reachable;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "virus_check")
    private String virusCheck;

    @Column(name = "virus_details", length = 4000)
    private String virusDetails;

    @Column(name = "curl_check")
    private String curlCheck;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public DomainResult() {}

    public DomainResult(String domain, Boolean reachable, Integer httpStatus, String virusCheck) {
        this.domain = domain;
        this.reachable = reachable;
        this.httpStatus = httpStatus;
        this.virusCheck = virusCheck;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public Boolean getReachable() { return reachable; }
    public void setReachable(Boolean reachable) { this.reachable = reachable; }

    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }

    public String getVirusCheck() { return virusCheck; }
    public void setVirusCheck(String virusCheck) { this.virusCheck = virusCheck; }

    public String getVirusDetails() { return virusDetails; }
    public void setVirusDetails(String virusDetails) { this.virusDetails = virusDetails; }

    public String getCurlCheck() { return curlCheck; }
    public void setCurlCheck(String curlCheck) { this.curlCheck = curlCheck; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

