package com.sgx.icms.domain;

import java.math.BigDecimal;

/** Mirrors the {@code assessment_components} table (line items of an assessment). */
public class AssessmentComponent {

    private long id;
    private long assessmentId;
    private String component;
    private String severity;        // MINOR/MODERATE/SEVERE
    private BigDecimal repairCost;
    private boolean replaceFlag;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(long assessmentId) { this.assessmentId = assessmentId; }

    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public BigDecimal getRepairCost() { return repairCost; }
    public void setRepairCost(BigDecimal repairCost) { this.repairCost = repairCost; }

    public boolean isReplaceFlag() { return replaceFlag; }
    public void setReplaceFlag(boolean replaceFlag) { this.replaceFlag = replaceFlag; }
}
