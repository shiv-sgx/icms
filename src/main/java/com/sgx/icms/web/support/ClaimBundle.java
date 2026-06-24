package com.sgx.icms.web.support;

import java.util.Collections;
import java.util.List;

import com.sgx.icms.domain.Approval;
import com.sgx.icms.domain.Assessment;
import com.sgx.icms.domain.AssessmentComponent;
import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.ClaimDocument;
import com.sgx.icms.domain.Communication;
import com.sgx.icms.domain.Settlement;

/** Aggregated read model for a claim detail page (agent/manager/surveyor views). */
public class ClaimBundle {

    private Claim claim;
    private List<ClaimDocument> documents = Collections.emptyList();
    private List<Communication> messages = Collections.emptyList();
    private Assessment assessment;
    private List<AssessmentComponent> components = Collections.emptyList();
    private List<Approval> approvals = Collections.emptyList();
    private Settlement settlement;
    private List<TimelineStage> timeline = Collections.emptyList();

    public Claim getClaim() { return claim; }
    public void setClaim(Claim claim) { this.claim = claim; }

    public List<ClaimDocument> getDocuments() { return documents; }
    public void setDocuments(List<ClaimDocument> documents) { this.documents = documents; }

    public List<Communication> getMessages() { return messages; }
    public void setMessages(List<Communication> messages) { this.messages = messages; }

    public Assessment getAssessment() { return assessment; }
    public void setAssessment(Assessment assessment) { this.assessment = assessment; }

    public List<AssessmentComponent> getComponents() { return components; }
    public void setComponents(List<AssessmentComponent> components) { this.components = components; }

    public List<Approval> getApprovals() { return approvals; }
    public void setApprovals(List<Approval> approvals) { this.approvals = approvals; }

    public Settlement getSettlement() { return settlement; }
    public void setSettlement(Settlement settlement) { this.settlement = settlement; }

    public List<TimelineStage> getTimeline() { return timeline; }
    public void setTimeline(List<TimelineStage> timeline) { this.timeline = timeline; }

    public boolean isHasAssessment() { return assessment != null; }
    public boolean isHasSettlement() { return settlement != null; }
}
