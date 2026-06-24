package com.sgx.icms.web.action.manager;

import java.math.BigDecimal;

import com.sgx.icms.service.ApprovalService;
import com.sgx.icms.service.ManagerService;

/**
 * Manager decisions on a claim: approval decision (approve/reject/return/hold) and
 * settlement override. One method per action (DMI disabled).
 */
public class DecisionAction extends ManagerBaseAction {

    private static final long serialVersionUID = 1L;

    private final transient ApprovalService approvalService = new ApprovalService();
    private final transient ManagerService managerService = new ManagerService();

    private long claimId;
    private String decision;
    private String remarks;
    private String amount;
    private String justification;
    private String redirectUrl;

    public String decide() {
        redirectUrl = detailUrl(claimId);
        try {
            String status = approvalService.decide(currentUser(), claimId, decision, remarks, clientIp());
            setFlash("success", "Decision recorded. Claim is now " + status + ".");
        } catch (IllegalArgumentException | IllegalStateException e) {
            setFlash("error", e.getMessage());
        }
        return SUCCESS;
    }

    public String override() {
        redirectUrl = detailUrl(claimId);
        try {
            managerService.overrideSettlement(currentUser(), claimId, new BigDecimal(amount.trim()),
                    justification, clientIp());
            setFlash("success", "Settlement amount overridden.");
        } catch (NumberFormatException | NullPointerException e) {
            setFlash("error", "Enter a valid amount.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            setFlash("error", e.getMessage());
        }
        return SUCCESS;
    }

    public void setClaimId(long claimId) { this.claimId = claimId; }
    public long getClaimId() { return claimId; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getDecision() { return decision; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getRemarks() { return remarks; }
    public void setAmount(String amount) { this.amount = amount; }
    public String getAmount() { return amount; }
    public void setJustification(String justification) { this.justification = justification; }
    public String getJustification() { return justification; }
    public String getRedirectUrl() { return redirectUrl; }
}
