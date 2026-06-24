package com.sgx.icms.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/** Mirrors the {@code settlements} table (payment authorisation + tracking). */
public class Settlement {

    public static final String AUTHORIZED = "AUTHORIZED";
    public static final String PAYMENT_INITIATED = "PAYMENT_INITIATED";
    public static final String BANK_PROCESSING = "BANK_PROCESSING";
    public static final String PAYMENT_CONFIRMED = "PAYMENT_CONFIRMED";
    public static final String CLAIMANT_NOTIFIED = "CLAIMANT_NOTIFIED";
    public static final String CLOSED = "CLOSED";

    /** Payment tracker order. */
    public static final List<String> TRACKER = Arrays.asList(
            AUTHORIZED, PAYMENT_INITIATED, BANK_PROCESSING, PAYMENT_CONFIRMED, CLAIMANT_NOTIFIED, CLOSED);

    private long id;
    private long claimId;
    private BigDecimal finalAmount;
    private String justification;
    private String paymentMethod;   // NEFT/CHEQUE/DEMAND_DRAFT/DIRECT_TO_WORKSHOP
    private String accountHolder;
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String status;
    private Long approvedBy;
    private LocalDateTime approvalDate;
    private LocalDateTime paymentInitiatedAt;
    private LocalDateTime paymentConfirmedAt;
    private LocalDateTime closedAt;
    private LocalDateTime createdAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getClaimId() { return claimId; }
    public void setClaimId(long claimId) { this.claimId = claimId; }

    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getAccountHolder() { return accountHolder; }
    public void setAccountHolder(String accountHolder) { this.accountHolder = accountHolder; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getIfscCode() { return ifscCode; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getApprovalDate() { return approvalDate; }
    public void setApprovalDate(LocalDateTime approvalDate) { this.approvalDate = approvalDate; }

    public LocalDateTime getPaymentInitiatedAt() { return paymentInitiatedAt; }
    public void setPaymentInitiatedAt(LocalDateTime v) { this.paymentInitiatedAt = v; }

    public LocalDateTime getPaymentConfirmedAt() { return paymentConfirmedAt; }
    public void setPaymentConfirmedAt(LocalDateTime v) { this.paymentConfirmedAt = v; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
