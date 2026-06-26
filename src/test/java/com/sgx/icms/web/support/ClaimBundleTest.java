package com.sgx.icms.web.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sgx.icms.domain.Approval;
import com.sgx.icms.domain.Assessment;
import com.sgx.icms.domain.AssessmentComponent;
import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.ClaimDocument;
import com.sgx.icms.domain.Communication;
import com.sgx.icms.domain.Settlement;

class ClaimBundleTest {

    @Test
    void defaultsAreEmptyListsAndNullSingulars() {
        ClaimBundle b = new ClaimBundle();
        assertTrue(b.getDocuments().isEmpty());
        assertTrue(b.getMessages().isEmpty());
        assertTrue(b.getComponents().isEmpty());
        assertTrue(b.getApprovals().isEmpty());
        assertTrue(b.getTimeline().isEmpty());
        assertFalse(b.isHasAssessment());
        assertFalse(b.isHasSettlement());
    }

    @Test
    void gettersAndSettersRoundTrip() {
        ClaimBundle b = new ClaimBundle();
        Claim claim = new Claim();
        Assessment assessment = new Assessment();
        Settlement settlement = new Settlement();
        List<ClaimDocument> docs = Collections.singletonList(new ClaimDocument());
        List<Communication> messages = Collections.singletonList(new Communication());
        List<AssessmentComponent> components = Collections.singletonList(new AssessmentComponent());
        List<Approval> approvals = Collections.singletonList(new Approval());
        List<TimelineStage> timeline = Collections.singletonList(new TimelineStage("Submitted", TimelineStage.DONE));

        b.setClaim(claim);
        b.setDocuments(docs);
        b.setMessages(messages);
        b.setAssessment(assessment);
        b.setComponents(components);
        b.setApprovals(approvals);
        b.setSettlement(settlement);
        b.setTimeline(timeline);

        assertEquals(claim, b.getClaim());
        assertEquals(docs, b.getDocuments());
        assertEquals(messages, b.getMessages());
        assertEquals(assessment, b.getAssessment());
        assertEquals(components, b.getComponents());
        assertEquals(approvals, b.getApprovals());
        assertEquals(settlement, b.getSettlement());
        assertEquals(timeline, b.getTimeline());
        assertTrue(b.isHasAssessment());
        assertTrue(b.isHasSettlement());
    }
}
