package com.sgx.icms.web.action.agent;

import java.math.BigDecimal;

import com.sgx.icms.domain.Settlement;
import com.sgx.icms.service.SettlementService;
import com.sgx.icms.web.support.ClaimBundle;

/**
 * Settlement screen: shows the settlement summary + payment tracker and processes
 * authorisation / payment-status advancement. One method per action (DMI disabled).
 */
public class SettlementAction extends AgentBaseAction {

    private static final long serialVersionUID = 1L;

    private final transient SettlementService settlementService = new SettlementService();

    private long id;            // claim id
    private transient ClaimBundle bundle;
    private BigDecimal suggestedAmount;
    private String flashMessage;
    private String flashType;

    // process form fields
    private String amount;
    private String paymentMethod = "NEFT";
    private String accountHolder;
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String justification;

    public String show() {
        bundle = agentClaims.bundle(id);
        if (bundle == null) {
            setFlash("error", "Claim not found.");
            return "missing";
        }
        suggestedAmount = computeSuggested();
        flashMessage = consumeFlash();
        flashType = consumeFlashType();
        return SUCCESS;
    }

    public String process() {
        try {
            BigDecimal amt = new BigDecimal(amount.trim());
            settlementService.authorize(currentUser(), id, amt, paymentMethod, accountHolder,
                    bankName, accountNumber, ifscCode, justification, clientIp());
            setFlash("success", "Settlement authorised. Payment processing started.");
        } catch (NumberFormatException | NullPointerException e) {
            setFlash("error", "Enter a valid settlement amount.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            setFlash("error", e.getMessage());
        }
        return SUCCESS;
    }

    public String advance() {
        try {
            String s = settlementService.advance(currentUser(), id, clientIp());
            setFlash("success", "Payment status advanced to " + s + ".");
        } catch (IllegalStateException e) {
            setFlash("error", e.getMessage());
        }
        return SUCCESS;
    }

    private BigDecimal computeSuggested() {
        Settlement s = bundle.getSettlement();
        if (s != null && s.getFinalAmount() != null) {
            return s.getFinalAmount();
        }
        if (bundle.getAssessment() != null && bundle.getAssessment().getNetPayable() != null) {
            return bundle.getAssessment().getNetPayable();
        }
        return bundle.getClaim().getEstimatedLoss();
    }

    public void setId(long id) { this.id = id; }
    public long getId() { return id; }
    public String getRedirectUrl() { return "/agent/settlement?id=" + id; }
    public ClaimBundle getBundle() { return bundle; }
    public BigDecimal getSuggestedAmount() { return suggestedAmount; }
    public String getFlashMessage() { return flashMessage; }
    public String getFlashType() { return flashType; }

    public void setAmount(String amount) { this.amount = amount; }
    public String getAmount() { return amount; }
    public void setPaymentMethod(String v) { this.paymentMethod = v; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setAccountHolder(String v) { this.accountHolder = v; }
    public String getAccountHolder() { return accountHolder; }
    public void setBankName(String v) { this.bankName = v; }
    public String getBankName() { return bankName; }
    public void setAccountNumber(String v) { this.accountNumber = v; }
    public String getAccountNumber() { return accountNumber; }
    public void setIfscCode(String v) { this.ifscCode = v; }
    public String getIfscCode() { return ifscCode; }
    public void setJustification(String v) { this.justification = v; }
    public String getJustification() { return justification; }
}
