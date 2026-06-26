package com.sgx.icms.web.action.agent;

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

import com.sgx.icms.domain.Assessment;
import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.Settlement;
import com.sgx.icms.service.AgentClaimService;
import com.sgx.icms.service.SettlementService;
import com.sgx.icms.web.action.BaseAction;
import com.sgx.icms.web.support.ClaimBundle;

class SettlementActionTest {

    @Test
    void show_claimNotFound_returnsMissing() {
        try (MockedConstruction<AgentClaimService> agentSvc = mockConstruction(AgentClaimService.class);
             MockedConstruction<SettlementService> settleSvc = mockConstruction(SettlementService.class)) {

            when(agentSvc.constructed().get(0).bundle(eq(1L))).thenReturn(null);

            SettlementAction action = new SettlementAction();
            action.setId(1L);

            assertEquals("missing", action.show());
        }
    }

    @Test
    void show_usesSettlementFinalAmountWhenPresent() {
        try (MockedConstruction<AgentClaimService> agentSvc = mockConstruction(AgentClaimService.class);
             MockedConstruction<SettlementService> settleSvc = mockConstruction(SettlementService.class)) {

            ClaimBundle bundle = new ClaimBundle();
            bundle.setClaim(new Claim());
            Settlement s = new Settlement();
            s.setFinalAmount(new BigDecimal("500.00"));
            bundle.setSettlement(s);
            when(agentSvc.constructed().get(0).bundle(eq(1L))).thenReturn(bundle);

            SettlementAction action = new SettlementAction();
            action.setId(1L);

            assertEquals(BaseAction.SUCCESS, action.show());
            assertEquals(new BigDecimal("500.00"), action.getSuggestedAmount());
        }
    }

    @Test
    void show_fallsBackToAssessmentNetPayable() {
        try (MockedConstruction<AgentClaimService> agentSvc = mockConstruction(AgentClaimService.class);
             MockedConstruction<SettlementService> settleSvc = mockConstruction(SettlementService.class)) {

            ClaimBundle bundle = new ClaimBundle();
            bundle.setClaim(new Claim());
            Assessment a = new Assessment();
            a.setNetPayable(new BigDecimal("300.00"));
            bundle.setAssessment(a);
            when(agentSvc.constructed().get(0).bundle(eq(1L))).thenReturn(bundle);

            SettlementAction action = new SettlementAction();
            action.setId(1L);

            assertEquals(BaseAction.SUCCESS, action.show());
            assertEquals(new BigDecimal("300.00"), action.getSuggestedAmount());
        }
    }

    @Test
    void show_fallsBackToClaimEstimatedLoss() {
        try (MockedConstruction<AgentClaimService> agentSvc = mockConstruction(AgentClaimService.class);
             MockedConstruction<SettlementService> settleSvc = mockConstruction(SettlementService.class)) {

            ClaimBundle bundle = new ClaimBundle();
            Claim claim = new Claim();
            claim.setEstimatedLoss(new BigDecimal("99.99"));
            bundle.setClaim(claim);
            when(agentSvc.constructed().get(0).bundle(eq(1L))).thenReturn(bundle);

            SettlementAction action = new SettlementAction();
            action.setId(1L);

            assertEquals(BaseAction.SUCCESS, action.show());
            assertEquals(new BigDecimal("99.99"), action.getSuggestedAmount());
        }
    }

    @Test
    void process_validAmount_authorizesSettlement() {
        try (MockedConstruction<AgentClaimService> agentSvc = mockConstruction(AgentClaimService.class);
             MockedConstruction<SettlementService> settleSvc = mockConstruction(SettlementService.class)) {

            SettlementAction action = new SettlementAction();
            action.setId(1L);
            action.setAmount("1000.50");

            assertEquals(BaseAction.SUCCESS, action.process());
        }
    }

    @Test
    void process_invalidAmount_setsErrorFlash() {
        try (MockedConstruction<AgentClaimService> agentSvc = mockConstruction(AgentClaimService.class);
             MockedConstruction<SettlementService> settleSvc = mockConstruction(SettlementService.class)) {

            SettlementAction action = new SettlementAction();
            action.setId(1L);
            action.setAmount("not-a-number");

            assertEquals(BaseAction.SUCCESS, action.process());
        }
    }

    @Test
    void process_nullAmount_setsErrorFlash() {
        try (MockedConstruction<AgentClaimService> agentSvc = mockConstruction(AgentClaimService.class);
             MockedConstruction<SettlementService> settleSvc = mockConstruction(SettlementService.class)) {

            SettlementAction action = new SettlementAction();
            action.setId(1L);

            assertEquals(BaseAction.SUCCESS, action.process());
        }
    }

    @Test
    void process_serviceThrowsIllegalState_setsErrorFlash() {
        try (MockedConstruction<AgentClaimService> agentSvc = mockConstruction(AgentClaimService.class);
             MockedConstruction<SettlementService> settleSvc = mockConstruction(SettlementService.class,
                     (mock, ctx) -> doThrow(new IllegalStateException("Already settled."))
                             .when(mock).authorize(any(), anyLong(), any(), any(), any(), any(), any(), any(), any(), any()))) {

            SettlementAction action = new SettlementAction();
            action.setId(1L);
            action.setAmount("200");

            assertEquals(BaseAction.SUCCESS, action.process());
        }
    }

    @Test
    void advance_success() {
        try (MockedConstruction<AgentClaimService> agentSvc = mockConstruction(AgentClaimService.class);
             MockedConstruction<SettlementService> settleSvc = mockConstruction(SettlementService.class)) {

            when(settleSvc.constructed().get(0).advance(any(), eq(1L), any())).thenReturn("PAID");

            SettlementAction action = new SettlementAction();
            action.setId(1L);

            assertEquals(BaseAction.SUCCESS, action.advance());
        }
    }

    @Test
    void advance_illegalState_setsErrorFlash() {
        try (MockedConstruction<AgentClaimService> agentSvc = mockConstruction(AgentClaimService.class);
             MockedConstruction<SettlementService> settleSvc = mockConstruction(SettlementService.class,
                     (mock, ctx) -> doThrow(new IllegalStateException("Nothing to advance."))
                             .when(mock).advance(any(), anyLong(), any()))) {

            SettlementAction action = new SettlementAction();
            action.setId(1L);

            assertEquals(BaseAction.SUCCESS, action.advance());
        }
    }

    @Test
    void redirectUrl_buildsFromId() {
        SettlementAction action = new SettlementAction();
        action.setId(55L);
        assertEquals("/agent/settlement?id=55", action.getRedirectUrl());
    }
}
