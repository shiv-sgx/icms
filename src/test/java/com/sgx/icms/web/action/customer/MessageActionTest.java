package com.sgx.icms.web.action.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.Policyholder;
import com.sgx.icms.domain.User;
import com.sgx.icms.service.AuditService;
import com.sgx.icms.service.ClaimService;
import com.sgx.icms.service.CommunicationService;
import com.sgx.icms.web.action.BaseAction;
import com.sgx.icms.web.support.SessionUser;

class MessageActionTest {

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
    void execute_noProfile_returnsMissing() {
        try (MockedConstruction<ClaimService> claimSvc = mockConstruction(ClaimService.class);
             MockedConstruction<CommunicationService> commSvc = mockConstruction(CommunicationService.class);
             MockedConstruction<AuditService> auditSvc = mockConstruction(AuditService.class)) {

            MessageAction action = new MessageAction();
            action.setSession(new HashMap<>());
            action.setClaimId(5L);

            assertEquals("missing", action.execute());
        }
    }

    @Test
    void execute_blankContent_returnsSuccessWithErrorFlashAndNoPost() {
        try (MockedConstruction<ClaimService> claimSvc = mockConstruction(ClaimService.class);
             MockedConstruction<CommunicationService> commSvc = mockConstruction(CommunicationService.class);
             MockedConstruction<AuditService> auditSvc = mockConstruction(AuditService.class)) {

            Policyholder ph = new Policyholder();
            ph.setId(2L);
            Claim claim = new Claim();
            claim.setId(5L);
            claim.setClaimNo("CLM-1");
            when(claimSvc.constructed().get(0).resolveCustomer(eq("u@x.com"))).thenReturn(ph);
            when(claimSvc.constructed().get(0).getOwnedClaim(eq(2L), eq(5L))).thenReturn(claim);

            MessageAction action = new MessageAction();
            action.setSession(sessionFor("u@x.com"));
            action.setClaimId(5L);
            action.setContent("   ");

            String result = action.execute();

            assertEquals(BaseAction.SUCCESS, result);
            verifyNoInteractions(commSvc.constructed().get(0));
        }
    }

    @Test
    void execute_validContent_postsMessageAndAudits() {
        try (MockedConstruction<ClaimService> claimSvc = mockConstruction(ClaimService.class);
             MockedConstruction<CommunicationService> commSvc = mockConstruction(CommunicationService.class);
             MockedConstruction<AuditService> auditSvc = mockConstruction(AuditService.class)) {

            Policyholder ph = new Policyholder();
            ph.setId(2L);
            Claim claim = new Claim();
            claim.setId(5L);
            claim.setClaimNo("CLM-1");
            when(claimSvc.constructed().get(0).resolveCustomer(eq("u@x.com"))).thenReturn(ph);
            when(claimSvc.constructed().get(0).getOwnedClaim(eq(2L), eq(5L))).thenReturn(claim);

            MessageAction action = new MessageAction();
            action.setSession(sessionFor("u@x.com"));
            action.setClaimId(5L);
            action.setContent("  Hello there  ");

            String result = action.execute();

            assertEquals(BaseAction.SUCCESS, result);
            verify(commSvc.constructed().get(0)).postMessage(any(), eq(5L), eq("Hello there"));
            verify(auditSvc.constructed().get(0)).success(any(), eq("MESSAGE_SENT"), eq("CLM-1"), any());
        }
    }
}
