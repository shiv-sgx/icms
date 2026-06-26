package com.sgx.icms.web.action.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.opensymphony.xwork2.Action;
import com.sgx.icms.service.AgentClaimService;
import com.sgx.icms.service.ApprovalService;
import com.sgx.icms.service.ManagerService;

class DecisionActionTest {

    @Test
    void decide_success_setsFlashWithNewStatus() {
        try (MockedConstruction<ApprovalService> approvalMock = mockConstruction(ApprovalService.class);
             MockedConstruction<ManagerService> managerMock = mockConstruction(ManagerService.class);
             MockedConstruction<AgentClaimService> claimsMock = mockConstruction(AgentClaimService.class)) {

            DecisionAction action = new DecisionAction();
            ApprovalService svc = approvalMock.constructed().get(0);
            when(svc.decide(any(), eq(5L), eq("APPROVED"), any(), any())).thenReturn("APPROVED");

            action.setClaimId(5);
            action.setDecision("APPROVED");

            assertEquals(Action.SUCCESS, action.decide());
            assertEquals("/manager/claim?id=5", action.getRedirectUrl());
            assertEquals("success", action.getFlashType());
        }
    }

    @Test
    void decide_invalidDecision_setsErrorFlash() {
        try (MockedConstruction<ApprovalService> approvalMock = mockConstruction(ApprovalService.class,
                (mock, ctx) -> doThrow(new IllegalArgumentException("Invalid approval decision."))
                        .when(mock).decide(any(), anyLong(), any(), any(), any()));
             MockedConstruction<ManagerService> managerMock = mockConstruction(ManagerService.class);
             MockedConstruction<AgentClaimService> claimsMock = mockConstruction(AgentClaimService.class)) {

            DecisionAction action = new DecisionAction();
            action.setClaimId(5);
            action.setDecision("BOGUS");

            assertEquals(Action.SUCCESS, action.decide());
            assertEquals("error", action.getFlashType());
        }
    }

    @Test
    void override_validAmount_setsSuccessFlash() {
        try (MockedConstruction<ApprovalService> approvalMock = mockConstruction(ApprovalService.class);
             MockedConstruction<ManagerService> managerMock = mockConstruction(ManagerService.class);
             MockedConstruction<AgentClaimService> claimsMock = mockConstruction(AgentClaimService.class)) {

            DecisionAction action = new DecisionAction();
            action.setClaimId(9);
            action.setAmount("1500.50");
            action.setJustification("manual review");

            assertEquals(Action.SUCCESS, action.override());
            ManagerService svc = managerMock.constructed().get(0);
            org.mockito.Mockito.verify(svc).overrideSettlement(any(), eq(9L), eq(new BigDecimal("1500.50")),
                    eq("manual review"), any());
            assertEquals("success", action.getFlashType());
        }
    }

    @Test
    void override_blankAmount_setsErrorFlash() {
        try (MockedConstruction<ApprovalService> approvalMock = mockConstruction(ApprovalService.class);
             MockedConstruction<ManagerService> managerMock = mockConstruction(ManagerService.class);
             MockedConstruction<AgentClaimService> claimsMock = mockConstruction(AgentClaimService.class)) {

            DecisionAction action = new DecisionAction();
            action.setClaimId(9);
            action.setAmount(null);

            assertEquals(Action.SUCCESS, action.override());
            assertEquals("error", action.getFlashType());
            assertEquals("Enter a valid amount.", action.getFlashMessage());
        }
    }

    @Test
    void override_noSettlementToOverride_setsErrorFlash() {
        try (MockedConstruction<ApprovalService> approvalMock = mockConstruction(ApprovalService.class);
             MockedConstruction<ManagerService> managerMock = mockConstruction(ManagerService.class,
                     (mock, ctx) -> doThrow(new IllegalStateException("No settlement exists yet to override."))
                             .when(mock).overrideSettlement(any(), anyLong(), any(), any(), any()));
             MockedConstruction<AgentClaimService> claimsMock = mockConstruction(AgentClaimService.class)) {

            DecisionAction action = new DecisionAction();
            action.setClaimId(9);
            action.setAmount("100");

            assertEquals(Action.SUCCESS, action.override());
            assertEquals("error", action.getFlashType());
            assertEquals("No settlement exists yet to override.", action.getFlashMessage());
        }
    }
}
