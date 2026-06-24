package com.sgx.icms.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Mirrors the {@code claims} table (the core entity). */
public class Claim {

    private long id;
    private String claimNo;
    private long policyId;
    private long policyholderId;
    private String claimantName;
    private String claimType;
    private String claimSubtype;
    private LocalDate incidentDate;
    private LocalTime incidentTime;
    private String incidentLocation;
    private String city;
    private String state;
    private String pinCode;
    private String description;
    private BigDecimal estimatedLoss;
    // type-specific optional fields
    private String vehicleRegNo;
    private String firNumber;
    private String policeStation;
    private String hospitalName;
    private String workshopName;
    private String thirdParty;
    // workflow
    private String status;
    private Long agentId;
    private Long surveyorId;
    private String riskLevel;
    private int fraudScore;
    private String internalNotes;
    private LocalDate slaDueDate;
    private LocalDateTime filedAt;
    private LocalDateTime updatedAt;
    // joined / derived (read views)
    private String policyNo;
    private String agentName;
    private String surveyorName;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getClaimNo() { return claimNo; }
    public void setClaimNo(String claimNo) { this.claimNo = claimNo; }

    public long getPolicyId() { return policyId; }
    public void setPolicyId(long policyId) { this.policyId = policyId; }

    public long getPolicyholderId() { return policyholderId; }
    public void setPolicyholderId(long policyholderId) { this.policyholderId = policyholderId; }

    public String getClaimantName() { return claimantName; }
    public void setClaimantName(String claimantName) { this.claimantName = claimantName; }

    public String getClaimType() { return claimType; }
    public void setClaimType(String claimType) { this.claimType = claimType; }

    public String getClaimSubtype() { return claimSubtype; }
    public void setClaimSubtype(String claimSubtype) { this.claimSubtype = claimSubtype; }

    public LocalDate getIncidentDate() { return incidentDate; }
    public void setIncidentDate(LocalDate incidentDate) { this.incidentDate = incidentDate; }

    public LocalTime getIncidentTime() { return incidentTime; }
    public void setIncidentTime(LocalTime incidentTime) { this.incidentTime = incidentTime; }

    public String getIncidentLocation() { return incidentLocation; }
    public void setIncidentLocation(String incidentLocation) { this.incidentLocation = incidentLocation; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPinCode() { return pinCode; }
    public void setPinCode(String pinCode) { this.pinCode = pinCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getEstimatedLoss() { return estimatedLoss; }
    public void setEstimatedLoss(BigDecimal estimatedLoss) { this.estimatedLoss = estimatedLoss; }

    public String getVehicleRegNo() { return vehicleRegNo; }
    public void setVehicleRegNo(String vehicleRegNo) { this.vehicleRegNo = vehicleRegNo; }

    public String getFirNumber() { return firNumber; }
    public void setFirNumber(String firNumber) { this.firNumber = firNumber; }

    public String getPoliceStation() { return policeStation; }
    public void setPoliceStation(String policeStation) { this.policeStation = policeStation; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getWorkshopName() { return workshopName; }
    public void setWorkshopName(String workshopName) { this.workshopName = workshopName; }

    public String getThirdParty() { return thirdParty; }
    public void setThirdParty(String thirdParty) { this.thirdParty = thirdParty; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }

    public Long getSurveyorId() { return surveyorId; }
    public void setSurveyorId(Long surveyorId) { this.surveyorId = surveyorId; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public int getFraudScore() { return fraudScore; }
    public void setFraudScore(int fraudScore) { this.fraudScore = fraudScore; }

    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }

    public LocalDate getSlaDueDate() { return slaDueDate; }
    public void setSlaDueDate(LocalDate slaDueDate) { this.slaDueDate = slaDueDate; }

    public LocalDateTime getFiledAt() { return filedAt; }
    public void setFiledAt(LocalDateTime filedAt) { this.filedAt = filedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getPolicyNo() { return policyNo; }
    public void setPolicyNo(String policyNo) { this.policyNo = policyNo; }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }

    public String getSurveyorName() { return surveyorName; }
    public void setSurveyorName(String surveyorName) { this.surveyorName = surveyorName; }

    /* ---- derived helpers for views ---- */
    public String getStatusLabel() { return ClaimStatus.label(status); }
    public String getStatusPill() { return ClaimStatus.pill(status); }
    public boolean isWithdrawable() { return ClaimStatus.isWithdrawable(status); }
}
