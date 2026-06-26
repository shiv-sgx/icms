package com.sgx.icms.web.action.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.sgx.icms.service.AgentClaimService;
import com.sgx.icms.service.AssignmentService;
import com.sgx.icms.service.AuditService;
import com.sgx.icms.service.CommunicationService;
import com.sgx.icms.web.action.BaseAction;

class AgentClaimActionsActionTest {

    @Test
    void acknowledge_success_setsRedirectAndFlash() {
        try (MockedConstruction<AgentClaimService> agentSvc = mockConstruction(AgentClaimService.class);
             MockedConstruction<AssignmentService> assignSvc = mockConstruction(AssignmentService.class);
             MockedConstruction<CommunicationService> commSvc = mockConstruction(CommunicationService.class);
             MockedConstruction<AuditService> auditSvc = mockConstruction(AuditService.class)) {

            AgentClaimActionsAction action = new AgentClaimActionsAction();
            action.setClaimId(8L);

            assertEquals(BaseAction.SUCCESS, action.acknowledge());
            assertEquals("/agent/claim?id=8", action.getRedirectUrl());
        }
    }

    @Test
    void acknowledge_illegalState_setsErrorFlashStillSuccess() {
        try (MockedConstruction<AgentClaimService> agentSvc = mockConstruction(AgentClaimService.class,
                     (mock, ctx) -> doThrow(new IllegalStateException("Already acknowledged."))
                             .when(mock).acknowledge(any(), anyLong(), any()));
             MockedConstruction<AssignmentService> assignSvc = mockConstruction(AssignmentService.class);
             MockedConstruction<CommunicationService> commSvc = mockConstruction(CommunicationService.class);
             MockedConstruction<AuditService> auditSvc = mockConstruction(AuditService.class)) {

            AgentClaimActionsAction action = new AgentClaimActionsAction();
            action.setClaimId(8L);

            assertEquals(BaseAction.SUCCESS, action.acknowledge());
        }
    }

    @Test
    void assign_success() {
        try (MockedConstruction<AgentClaimService> agentSvc = mockConstruction(AgentClaimService.class);
             MockedConstruction<AssignmentService> assignSvc = mockConstruction(AssignmentService.class);
             MockedConstruction<CommunicationService> commSvc = mockConstruction(CommunicationService.class);
             MockedConstruction<AuditService> auditSvc = mockConstruction(AuditService.class)) {

            AgentClaimActionsAction action = new AgentClaimActionsAction();
            action.setClaimId(8L);
            action.setSurveyorId(20L);

            assertEquals(BaseAction.SUCCESS, action.assign());
            verify(assignSvc.constructed().get(0)).assignSurveyor(any(), eq(8L), eq(20L), any());
        }
    }

    @Test
    void assign_illegalArgument_setsErrorFlash() {
        try (MockedConstruction<AgentClaimService> agentSvc = mockConstruction(AgentClaimService.class);
             MockedConstruction<AssignmentService> assignSvc = mockConstruction(AssignmentService.class,
                     (mock, ctx) -> doThrow(new IllegalArgumentException("Invalid surveyor."))
                             .when(mock).assignSurveyor(any(), anyLong(), anyLong(), any()));
             MockedConstruction<CommunicationService> commSvc = mockConstruction(CommunicationService.class);
             MockedConstruction<AuditService> auditSvc = mockConstruction(AuditService.class)) {

            AgentClaimActionsAction action = new AgentClaimActionsAction();
            action.setClaimId(8L);

            assertEquals(BaseAction.SUCCESS, action.assign());
        }
    }

    @Test
    void forward_success() {
        try (MockedConstruction<AgentClaimService> agentSvc = mockConstruction(AgentClaimService.class);
             MockedConstruction<AssignmentService> assignSvc = mockConstruction(AssignmentService.class);
             MockedConstruction<CommunicationService> commSvc = mockConstruction(CommunicationService.class);
             MockedConstruction<AuditService> auditSvc = mockConstruction(AuditService.class)) {

            when(agentSvc.constructed().get(0).forwardForApproval(any(), eq(8L), any()))
                    .thenReturn("PENDING_APPROVAL");

            AgentClaimActionsAction action = new AgentClaimActionsAction();
            action.setClaimId(8L);

            assertEquals(BaseAction.SUCCESS, action.forward());
        }
    }

    @Test
    void forward_illegalState_setsErrorFlash() {
        try (MockedConstruction<AgentClaimService> agentSvc = mockConstruction(AgentClaimService.class,
                     (mock, ctx) -> doThrow(new IllegalStateException("Cannot forward yet."))
                             .when(mock).forwardForApproval(any(), anyLong(), any()));
             MockedConstruction<AssignmentService> assignSvc = mockConstruction(AssignmentService.class);
             MockedConstruction<CommunicationService> commSvc = mockConstruction(CommunicationService.class);
             MockedConstruction<AuditService> auditSvc = mockConstruction(AuditService.class)) {

            AgentClaimActionsAction action = new AgentClaimActionsAction();
            action.setClaimId(8L);

            assertEquals(BaseAction.SUCCESS, action.forward());
        }
    }

    @Test
    void note_success() {
        try (MockedConstruction<AgentClaimService> agentSvc = mockConstruction(AgentClaimService.class);
             MockedConstruction<AssignmentService> assignSvc = mockConstruction(AssignmentService.class);
             MockedConstruction<CommunicationService> commSvc = mockConstruction(CommunicationService.class);
             MockedConstruction<AuditService> auditSvc = mockConstruction(AuditService.class)) {

            AgentClaimActionsAction action = new AgentClaimActionsAction();
            action.setClaimId(8L);
            action.setNotes("internal note");

            assertEquals(BaseAction.SUCCESS, action.note());
            verify(agentSvc.constructed().get(0)).updateNotes(any(), eq(8L), eq("internal note"), any());
        }
    }

    @Test
    void note_illegalState_setsErrorFlash() {
        try (MockedConstruction<AgentClaimService> agentSvc = mockConstruction(AgentClaimService.class,
                     (mock, ctx) -> doThrow(new IllegalStateException("Claim closed."))
                             .when(mock).updateNotes(any(), anyLong(), any(), any()));
             MockedConstruction<AssignmentService> assignSvc = mockConstruction(AssignmentService.class);
             MockedConstruction<CommunicationService> commSvc = mockConstruction(CommunicationService.class);
             MockedConstruction<AuditService> auditSvc = mockConstruction(AuditService.class)) {

            AgentClaimActionsAction action = new AgentClaimActionsAction();
            action.setClaimId(8L);

            assertEquals(BaseAction.SUCCESS, action.note());
        }
    }

    @Test
    void message_blankContent_noPostAndNoOp() {
        try (MockedConstruction<AgentClaimService> agentSvc = mockConstruction(AgentClaimService.class);
             MockedConstruction<AssignmentService> assignSvc = mockConstruction(AssignmentService.class);
             MockedConstruction<CommunicationService> commSvc = mockConstruction(CommunicationService.class);
             MockedConstruction<AuditService> auditSvc = mockConstruction(AuditService.class)) {

            AgentClaimActionsAction action = new AgentClaimActionsAction();
            action.setClaimId(8L);
            action.setContent("   ");

            assertEquals(BaseAction.SUCCESS, action.message());
            verifyNoInteractions(commSvc.constructed().get(0));
        }
    }

    @Test
    void message_validContent_postsAndAudits() {
        try (MockedConstruction<AgentClaimService> agentSvc = mockConstruction(AgentClaimService.class);
             MockedConstruction<AssignmentService> assignSvc = mockConstruction(AssignmentService.class);
             MockedConstruction<CommunicationService> commSvc = mockConstruction(CommunicationService.class);
             MockedConstruction<AuditService> auditSvc = mockConstruction(AuditService.class)) {

            AgentClaimActionsAction action = new AgentClaimActionsAction();
            action.setClaimId(8L);
            action.setContent(" hi there ");

            assertEquals(BaseAction.SUCCESS, action.message());
            verify(commSvc.constructed().get(0)).postMessage(any(), eq(8L), eq("hi there"));
            verify(auditSvc.constructed().get(0)).success(any(), eq("MESSAGE_SENT"), eq("claim:8"), any());
        }
    }

    @Test
    void gettersSetters() {
        AgentClaimActionsAction action = new AgentClaimActionsAction();
        action.setClaimId(1L);
        action.setSurveyorId(2L);
        action.setNotes("n");
        action.setContent("c");
        assertEquals(1L, action.getClaimId());
        assertEquals(2L, action.getSurveyorId());
        assertEquals("n", action.getNotes());
        assertEquals("c", action.getContent());
    }
}
