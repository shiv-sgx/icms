package com.sgx.icms.web.action.customer;

import com.sgx.icms.domain.Claim;
import com.sgx.icms.service.AuditService;
import com.sgx.icms.service.CommunicationService;

/** Posts a customer message to a claim's communication thread. */
public class MessageAction extends CustomerBaseAction {

    private static final long serialVersionUID = 1L;

    private final transient CommunicationService communicationService = new CommunicationService();
    private final transient AuditService audit = new AuditService();

    private long claimId;
    private String content;
    private String redirectUrl;

    @Override
    public String execute() {
        redirectUrl = "/customer/claim?id=" + claimId;
        if (policyholder() == null) {
            return "missing";
        }
        Claim claim = claimService.getOwnedClaim(policyholder().getId(), claimId);
        if (claim == null) {
            setFlash("error", "Claim not found.");
            return "missing";
        }
        if (content == null || content.trim().isEmpty()) {
            setFlash("error", "Message cannot be empty.");
            return SUCCESS;
        }
        communicationService.postMessage(currentUser(), claimId, content.trim());
        audit.success(currentUser(), "MESSAGE_SENT", claim.getClaimNo(), clientIp());
        setFlash("success", "Message sent.");
        return SUCCESS;
    }

    public void setClaimId(long claimId) { this.claimId = claimId; }
    public long getClaimId() { return claimId; }
    public void setContent(String content) { this.content = content; }
    public String getContent() { return content; }
    public String getRedirectUrl() { return redirectUrl; }
}
