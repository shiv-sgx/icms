package com.sgx.icms.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Mirrors the {@code policies} table. */
public class Policy {

    private long id;
    private String policyNo;
    private long policyholderId;
    private String type;
    private BigDecimal sumInsured;
    private BigDecimal premium;
    private LocalDate startDate;
    private LocalDate expiryDate;
    private BigDecimal ncbDiscount;
    private String status;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getPolicyNo() { return policyNo; }
    public void setPolicyNo(String policyNo) { this.policyNo = policyNo; }

    public long getPolicyholderId() { return policyholderId; }
    public void setPolicyholderId(long policyholderId) { this.policyholderId = policyholderId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getSumInsured() { return sumInsured; }
    public void setSumInsured(BigDecimal sumInsured) { this.sumInsured = sumInsured; }

    public BigDecimal getPremium() { return premium; }
    public void setPremium(BigDecimal premium) { this.premium = premium; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public BigDecimal getNcbDiscount() { return ncbDiscount; }
    public void setNcbDiscount(BigDecimal ncbDiscount) { this.ncbDiscount = ncbDiscount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /** "POL-84521 — MOTOR" for select dropdowns. */
    public String getDisplayLabel() { return policyNo + " — " + type; }
}
