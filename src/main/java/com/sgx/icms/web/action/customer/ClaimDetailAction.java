package com.sgx.icms.web.action.customer;

import java.util.Collections;
import java.util.List;

import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.ClaimDocument;
import com.sgx.icms.domain.Communication;
import com.sgx.icms.service.CommunicationService;
import com.sgx.icms.service.DocumentService;
import com.sgx.icms.web.support.TimelineStage;

/** Claim detail: summary, status timeline, documents, and message thread. */
public class ClaimDetailAction extends CustomerBaseAction {

    private static final long serialVersionUID = 1L;

    private final transient DocumentService documentService = new DocumentService();
    private final transient CommunicationService communicationService = new CommunicationService();

    private long id;
    private transient Claim claim;
    private transient List<TimelineStage> timeline = Collections.emptyList();
    private transient List<ClaimDocument> documents = Collections.emptyList();
    private transient List<Communication> messages = Collections.emptyList();
    private String flashMessage;
    private String flashType;

    @Override
    public String execute() {
        if (policyholder() == null) {
            return "missing";
        }
        claim = claimService.getOwnedClaim(policyholder().getId(), id);
        if (claim == null) {
            setFlash("error", "Claim not found.");
            return "missing";
        }
        timeline = claimService.timeline(claim);
        documents = documentService.forClaim(claim.getId());
        messages = communicationService.forClaim(claim.getId());
        flashMessage = consumeFlash();
        flashType = consumeFlashType();
        return SUCCESS;
    }

    public void setId(long id) { this.id = id; }
    public long getId() { return id; }

    public Claim getClaim() { return claim; }
    public List<TimelineStage> getTimeline() { return timeline; }
    public List<ClaimDocument> getDocuments() { return documents; }
    public List<Communication> getMessages() { return messages; }
    public String getFlashMessage() { return flashMessage; }
    public String getFlashType() { return flashType; }
}
