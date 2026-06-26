package com.sgx.icms.web.action.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.Policyholder;
import com.sgx.icms.domain.User;
import com.sgx.icms.service.ClaimService;
import com.sgx.icms.web.action.BaseAction;
import com.sgx.icms.web.support.SessionUser;

class NewClaimActionTest {

    private static HashMap<String, Object> sessionFor(String email) {
        User u = new User();
        u.setId(1L);
        u.setEmail(email);
        u.setRoleName("CUSTOMER");
        HashMap<String, Object> session = new HashMap<>();
        session.put(SessionUser.SESSION_KEY, SessionUser.from(u));
        return session;
    }

    @Test
    void show_loadsPoliciesAndReturnsInput() {
        try (MockedConstruction<ClaimService> mocked = mockConstruction(ClaimService.class)) {
            Policyholder ph = new Policyholder();
            ph.setId(1L);
            when(mocked.constructed().get(0).resolveCustomer(eq("a@x.com"))).thenReturn(ph);
            when(mocked.constructed().get(0).policiesForCustomer(eq(1L))).thenReturn(Collections.emptyList());

            NewClaimAction action = new NewClaimAction();
            action.setSession(sessionFor("a@x.com"));

            assertEquals(BaseAction.INPUT, action.show());
        }
    }

    @Test
    void execute_noProfile_returnsInputWithActionError() {
        try (MockedConstruction<ClaimService> mocked = mockConstruction(ClaimService.class)) {
            NewClaimAction real = new NewClaimAction();
            real.setSession(new HashMap<>());

            NewClaimAction action = spy(real);
            doReturn("No policyholder profile is linked to your account.")
                    .when(action).getText(anyString());

            String result = action.execute();

            assertEquals(BaseAction.INPUT, result);
            assertFalse(action.getActionErrors().isEmpty());
        }
    }

    @Test
    void execute_missingPolicyAndDescription_returnsInputWithFieldErrors() {
        try (MockedConstruction<ClaimService> mocked = mockConstruction(ClaimService.class)) {
            Policyholder ph = new Policyholder();
            ph.setId(1L);
            when(mocked.constructed().get(0).resolveCustomer(eq("a@x.com"))).thenReturn(ph);

            NewClaimAction action = new NewClaimAction();
            action.setSession(sessionFor("a@x.com"));
            action.setPolicyId(0L);
            action.setDescription("");

            String result = action.execute();

            assertEquals(BaseAction.INPUT, result);
            assertTrue(action.hasFieldErrors());
            assertTrue(action.getFieldErrors().containsKey("policyId"));
            assertTrue(action.getFieldErrors().containsKey("description"));
        }
    }

    @Test
    void execute_badDateAndTime_returnsInputWithFieldErrors() {
        try (MockedConstruction<ClaimService> mocked = mockConstruction(ClaimService.class)) {
            Policyholder ph = new Policyholder();
            ph.setId(1L);
            when(mocked.constructed().get(0).resolveCustomer(eq("a@x.com"))).thenReturn(ph);

            NewClaimAction action = new NewClaimAction();
            action.setSession(sessionFor("a@x.com"));
            action.setPolicyId(7L);
            action.setDescription("Fender bender");
            action.setIncidentDate("not-a-date");
            action.setIncidentTime("not-a-time");

            String result = action.execute();

            assertEquals(BaseAction.INPUT, result);
            assertTrue(action.getFieldErrors().containsKey("incidentDate"));
            assertTrue(action.getFieldErrors().containsKey("incidentTime"));
        }
    }

    @Test
    void execute_negativeEstimatedLoss_returnsInputWithFieldError() {
        try (MockedConstruction<ClaimService> mocked = mockConstruction(ClaimService.class)) {
            Policyholder ph = new Policyholder();
            ph.setId(1L);
            when(mocked.constructed().get(0).resolveCustomer(eq("a@x.com"))).thenReturn(ph);

            NewClaimAction action = new NewClaimAction();
            action.setSession(sessionFor("a@x.com"));
            action.setPolicyId(7L);
            action.setDescription("Fender bender");
            action.setEstimatedLoss("-50");

            String result = action.execute();

            assertEquals(BaseAction.INPUT, result);
            assertTrue(action.getFieldErrors().containsKey("estimatedLoss"));
        }
    }

    @Test
    void execute_nonNumericEstimatedLoss_returnsInputWithFieldError() {
        try (MockedConstruction<ClaimService> mocked = mockConstruction(ClaimService.class)) {
            Policyholder ph = new Policyholder();
            ph.setId(1L);
            when(mocked.constructed().get(0).resolveCustomer(eq("a@x.com"))).thenReturn(ph);

            NewClaimAction action = new NewClaimAction();
            action.setSession(sessionFor("a@x.com"));
            action.setPolicyId(7L);
            action.setDescription("Fender bender");
            action.setEstimatedLoss("not-a-number");

            String result = action.execute();

            assertEquals(BaseAction.INPUT, result);
            assertTrue(action.getFieldErrors().containsKey("estimatedLoss"));
        }
    }

    @Test
    void execute_validSubmit_createsClaimAndRedirects() {
        try (MockedConstruction<ClaimService> mocked = mockConstruction(ClaimService.class)) {
            Policyholder ph = new Policyholder();
            ph.setId(1L);
            ClaimService svc = mocked.constructed().get(0);
            when(svc.resolveCustomer(eq("a@x.com"))).thenReturn(ph);
            when(svc.createClaim(any(), eq(ph), any(Claim.class), eq(true), any())).thenReturn(123L);

            NewClaimAction action = new NewClaimAction();
            action.setSession(sessionFor("a@x.com"));
            action.setPolicyId(7L);
            action.setDescription("Fender bender");
            action.setMode("submit");

            String result = action.execute();

            assertEquals(BaseAction.SUCCESS, result);
            assertEquals("/customer/claim?id=123", action.getRedirectUrl());
        }
    }

    @Test
    void execute_validDraft_savesDraft() {
        try (MockedConstruction<ClaimService> mocked = mockConstruction(ClaimService.class)) {
            Policyholder ph = new Policyholder();
            ph.setId(1L);
            ClaimService svc = mocked.constructed().get(0);
            when(svc.resolveCustomer(eq("a@x.com"))).thenReturn(ph);
            when(svc.createClaim(any(), eq(ph), any(Claim.class), eq(false), any())).thenReturn(124L);

            NewClaimAction action = new NewClaimAction();
            action.setSession(sessionFor("a@x.com"));
            action.setPolicyId(7L);
            action.setDescription("Fender bender");
            action.setMode("draft");

            String result = action.execute();

            assertEquals(BaseAction.SUCCESS, result);
            assertEquals("/customer/claim?id=124", action.getRedirectUrl());
        }
    }

    @Test
    void execute_createClaimThrows_returnsInputWithActionError() {
        try (MockedConstruction<ClaimService> mocked = mockConstruction(ClaimService.class)) {
            Policyholder ph = new Policyholder();
            ph.setId(1L);
            ClaimService svc = mocked.constructed().get(0);
            when(svc.resolveCustomer(eq("a@x.com"))).thenReturn(ph);
            when(svc.createClaim(any(), eq(ph), any(Claim.class), anyBoolean(), any()))
                    .thenThrow(new IllegalArgumentException("Policy is not active."));

            NewClaimAction action = new NewClaimAction();
            action.setSession(sessionFor("a@x.com"));
            action.setPolicyId(7L);
            action.setDescription("Fender bender");

            String result = action.execute();

            assertEquals(BaseAction.INPUT, result);
            assertTrue(action.getActionErrors().contains("Policy is not active."));
        }
    }
}
