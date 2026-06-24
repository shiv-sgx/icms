package com.sgx.icms.domain;

import java.time.LocalDateTime;

/** Mirrors the {@code approvals} table (multi-level approval chain). */
public class Approval {

    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String CONDITIONAL = "CONDITIONAL";
    public static final String REJECTED = "REJECTED";
    public static final String RETURNED = "RETURNED";
    public static final String ON_HOLD = "ON_HOLD";

    private long id;
    private long claimId;
    private String level;          // L1/L2/L3
    private Long approverId;
    private String approverRole;
    private String decision;
    private String remarks;
    private LocalDateTime decidedAt;
    private LocalDateTime createdAt;
    private String approverName;   // joined

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getClaimId() { return claimId; }
    public void setClaimId(long claimId) { this.claimId = claimId; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }

    public String getApproverRole() { return approverRole; }
    public void setApproverRole(String approverRole) { this.approverRole = approverRole; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }

    public boolean isPending() { return PENDING.equals(decision); }
}
