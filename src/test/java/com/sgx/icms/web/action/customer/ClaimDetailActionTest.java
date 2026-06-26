package com.sgx.icms.web.action.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.Policyholder;
import com.sgx.icms.domain.User;
import com.sgx.icms.service.ClaimService;
import com.sgx.icms.service.CommunicationService;
import com.sgx.icms.service.DocumentService;
import com.sgx.icms.web.action.BaseAction;
import com.sgx.icms.web.support.SessionUser;

class ClaimDetailActionTest {

    private static HashMap<String, Object> sessionFor(String email) {
        User u = new User();
        u.setId(3L);
        u.setEmail(email);
        u.setRoleName("CUSTOMER");
        HashMap<String, Object> session = new HashMap<>();
        session.put(SessionUser.SESSION_KEY, SessionUser.from(u));
        return session;
    }

    @Test
    void execute_noProfile_returnsMissing() {
        try (MockedConstruction<ClaimService> claimSvc = mockConstruction(ClaimService.class);
             MockedConstruction<DocumentService> docSvc = mockConstruction(DocumentService.class);
             MockedConstruction<CommunicationService> commSvc = mockConstruction(CommunicationService.class)) {

            ClaimDetailAction action = new ClaimDetailAction();
            action.setSession(new HashMap<>());

            assertEquals("missing", action.execute());
        }
    }

    @Test
    void execute_claimNotFound_returnsMissingWithFlash() {
        try (MockedConstruction<ClaimService> claimSvc = mockConstruction(ClaimService.class);
             MockedConstruction<DocumentService> docSvc = mockConstruction(DocumentService.class);
             MockedConstruction<CommunicationService> commSvc = mockConstruction(CommunicationService.class)) {

            Policyholder ph = new Policyholder();
            ph.setId(5L);
            when(claimSvc.constructed().get(0).resolveCustomer(eq("d@x.com"))).thenReturn(ph);
            when(claimSvc.constructed().get(0).getOwnedClaim(eq(5L), eq(99L))).thenReturn(null);

            ClaimDetailAction action = new ClaimDetailAction();
            action.setSession(sessionFor("d@x.com"));
            action.setId(99L);

            assertEquals("missing", action.execute());
        }
    }

    @Test
    void execute_claimFound_loadsTimelineDocumentsAndMessages() {
        try (MockedConstruction<ClaimService> claimSvc = mockConstruction(ClaimService.class);
             MockedConstruction<DocumentService> docSvc = mockConstruction(DocumentService.class);
             MockedConstruction<CommunicationService> commSvc = mockConstruction(CommunicationService.class)) {

            Policyholder ph = new Policyholder();
            ph.setId(5L);
            Claim claim = new Claim();
            claim.setId(42L);
            when(claimSvc.constructed().get(0).resolveCustomer(eq("d@x.com"))).thenReturn(ph);
            when(claimSvc.constructed().get(0).getOwnedClaim(eq(5L), eq(42L))).thenReturn(claim);

            ClaimDetailAction action = new ClaimDetailAction();
            action.setSession(sessionFor("d@x.com"));
            action.setId(42L);

            String result = action.execute();

            assertEquals(BaseAction.SUCCESS, result);
            assertEquals(claim, action.getClaim());
        }
    }
}
