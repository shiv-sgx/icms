package com.sgx.icms.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Mirrors the {@code assessments} table (surveyor's damage assessment). */
public class Assessment {

    public static final String STATUS_ASSIGNED = "ASSIGNED";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_SUBMITTED = "SUBMITTED";

    private long id;
    private long claimId;
    private Long surveyorId;
    private LocalDate visitDate;
    private LocalTime visitTime;
    private String siteObservations;
    private String reportRefNo;
    private BigDecimal grossAssessed;
    private BigDecimal policyDeductible;
    private BigDecimal depreciationPct;
    private BigDecimal depreciationAmt;
    private BigDecimal salvageValue;
    private BigDecimal netPayable;
    private String recommendation;   // APPROVE_FULL/PARTIAL_APPROVE/REJECT
    private String remarks;
    private String status;
    private LocalDateTime createdAt;
    private String surveyorName;     // joined

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getClaimId() { return claimId; }
    public void setClaimId(long claimId) { this.claimId = claimId; }

    public Long getSurveyorId() { return surveyorId; }
    public void setSurveyorId(Long surveyorId) { this.surveyorId = surveyorId; }

    public LocalDate getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }

    public LocalTime getVisitTime() { return visitTime; }
    public void setVisitTime(LocalTime visitTime) { this.visitTime = visitTime; }

    public String getSiteObservations() { return siteObservations; }
    public void setSiteObservations(String siteObservations) { this.siteObservations = siteObservations; }

    public String getReportRefNo() { return reportRefNo; }
    public void setReportRefNo(String reportRefNo) { this.reportRefNo = reportRefNo; }

    public BigDecimal getGrossAssessed() { return grossAssessed; }
    public void setGrossAssessed(BigDecimal v) { this.grossAssessed = v; }

    public BigDecimal getPolicyDeductible() { return policyDeductible; }
    public void setPolicyDeductible(BigDecimal v) { this.policyDeductible = v; }

    public BigDecimal getDepreciationPct() { return depreciationPct; }
    public void setDepreciationPct(BigDecimal v) { this.depreciationPct = v; }

    public BigDecimal getDepreciationAmt() { return depreciationAmt; }
    public void setDepreciationAmt(BigDecimal v) { this.depreciationAmt = v; }

    public BigDecimal getSalvageValue() { return salvageValue; }
    public void setSalvageValue(BigDecimal v) { this.salvageValue = v; }

    public BigDecimal getNetPayable() { return netPayable; }
    public void setNetPayable(BigDecimal v) { this.netPayable = v; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getSurveyorName() { return surveyorName; }
    public void setSurveyorName(String surveyorName) { this.surveyorName = surveyorName; }
}
