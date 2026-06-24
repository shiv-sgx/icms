package com.sgx.icms.domain;

import java.math.BigDecimal;

/** Mirrors the {@code approval_thresholds} config table (L1/L2/L3 amount bands). */
public class ApprovalThreshold {

    private int id;
    private String level;
    private String label;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;   // null = no upper bound

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }

    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }
}
