package com.sgx.icms.web.action.agent;

import com.sgx.icms.service.AssignmentService;
import com.sgx.icms.service.AuditService;
import com.sgx.icms.service.CommunicationService;

/**
 * Handles the agent's claim-detail actions (one method per action name, since
 * dynamic method invocation is disabled): acknowledge, assign surveyor, forward for
 * approval, update internal notes, and post a message. All redirect back to detail
 * with a flash message.
 */
public class AgentClaimActionsAction extends AgentBaseAction {

    private static final long serialVersionUID = 1L;

    private final transient AssignmentService assignmentService = new AssignmentService();
    private final transient CommunicationService communicationService = new CommunicationService();
    private final transient AuditService audit = new AuditService();

    private long claimId;
    private long surveyorId;
    private String notes;
    private String content;
    private String redirectUrl;

    public String acknowledge() {
        redirectUrl = detailUrl(claimId);
        try {
            agentClaims.acknowledge(currentUser(), claimId, clientIp());
            setFlash("success", "Claim acknowledged and under review.");
        } catch (IllegalStateException e) {
            setFlash("error", e.getMessage());
        }
        return SUCCESS;
    }

    public String assign() {
        redirectUrl = detailUrl(claimId);
        try {
            assignmentService.assignSurveyor(currentUser(), claimId, surveyorId, clientIp());
            setFlash("success", "Surveyor assigned. The claim is now scheduled for survey.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            setFlash("error", e.getMessage());
        }
        return SUCCESS;
    }

    public String forward() {
        redirectUrl = detailUrl(claimId);
        try {
            String status = agentClaims.forwardForApproval(currentUser(), claimId, clientIp());
            setFlash("success", "Claim forwarded for approval (" + status + ").");
        } catch (IllegalStateException e) {
            setFlash("error", e.getMessage());
        }
        return SUCCESS;
    }

    public String note() {
        redirectUrl = detailUrl(claimId);
        try {
            agentClaims.updateNotes(currentUser(), claimId, notes, clientIp());
            setFlash("success", "Internal notes saved.");
        } catch (IllegalStateException e) {
            setFlash("error", e.getMessage());
        }
        return SUCCESS;
    }

    public String message() {
        redirectUrl = detailUrl(claimId);
        if (content == null || content.trim().isEmpty()) {
            setFlash("error", "Message cannot be empty.");
            return SUCCESS;
        }
        communicationService.postMessage(currentUser(), claimId, content.trim());
        audit.success(currentUser(), "MESSAGE_SENT", "claim:" + claimId, clientIp());
        setFlash("success", "Message sent.");
        return SUCCESS;
    }

    public void setClaimId(long claimId) { this.claimId = claimId; }
    public long getClaimId() { return claimId; }
    public void setSurveyorId(long surveyorId) { this.surveyorId = surveyorId; }
    public long getSurveyorId() { return surveyorId; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getNotes() { return notes; }
    public void setContent(String content) { this.content = content; }
    public String getContent() { return content; }
    public String getRedirectUrl() { return redirectUrl; }
}
