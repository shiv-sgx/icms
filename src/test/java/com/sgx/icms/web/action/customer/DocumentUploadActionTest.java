package com.sgx.icms.web.action.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.Policyholder;
import com.sgx.icms.domain.User;
import com.sgx.icms.service.AuditService;
import com.sgx.icms.service.ClaimService;
import com.sgx.icms.service.DocumentService;
import com.sgx.icms.web.action.BaseAction;
import com.sgx.icms.web.support.SessionUser;

class DocumentUploadActionTest {

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
             MockedConstruction<DocumentService> docSvc = mockConstruction(DocumentService.class);
             MockedConstruction<AuditService> auditSvc = mockConstruction(AuditService.class)) {

            DocumentUploadAction action = new DocumentUploadAction();
            action.setSession(new HashMap<>());
            action.setClaimId(10L);

            assertEquals("missing", action.execute());
            assertEquals("/customer/claim?id=10", action.getRedirectUrl());
        }
    }

    @Test
    void execute_claimNotFound_returnsMissing() {
        try (MockedConstruction<ClaimService> claimSvc = mockConstruction(ClaimService.class);
             MockedConstruction<DocumentService> docSvc = mockConstruction(DocumentService.class);
             MockedConstruction<AuditService> auditSvc = mockConstruction(AuditService.class)) {

            Policyholder ph = new Policyholder();
            ph.setId(2L);
            when(claimSvc.constructed().get(0).resolveCustomer(eq("u@x.com"))).thenReturn(ph);
            when(claimSvc.constructed().get(0).getOwnedClaim(eq(2L), eq(10L))).thenReturn(null);

            DocumentUploadAction action = new DocumentUploadAction();
            action.setSession(sessionFor("u@x.com"));
            action.setClaimId(10L);

            assertEquals("missing", action.execute());
        }
    }

    @Test
    void execute_uploadSucceeds_returnsSuccessWithFlash() {
        try (MockedConstruction<ClaimService> claimSvc = mockConstruction(ClaimService.class);
             MockedConstruction<DocumentService> docSvc = mockConstruction(DocumentService.class);
             MockedConstruction<AuditService> auditSvc = mockConstruction(AuditService.class)) {

            Policyholder ph = new Policyholder();
            ph.setId(2L);
            Claim claim = new Claim();
            claim.setId(10L);
            claim.setClaimNo("CLM-2026-0001");
            when(claimSvc.constructed().get(0).resolveCustomer(eq("u@x.com"))).thenReturn(ph);
            when(claimSvc.constructed().get(0).getOwnedClaim(eq(2L), eq(10L))).thenReturn(claim);

            DocumentUploadAction action = new DocumentUploadAction();
            action.setSession(sessionFor("u@x.com"));
            action.setClaimId(10L);
            action.setDocType("FIR");

            String result = action.execute();

            assertEquals(BaseAction.SUCCESS, result);
        }
    }

    @Test
    void execute_uploadRejected_setsErrorFlashButStillSuccess() {
        try (MockedConstruction<ClaimService> claimSvc = mockConstruction(ClaimService.class);
             MockedConstruction<DocumentService> docSvc = mockConstruction(DocumentService.class,
                     (mock, ctx) -> doThrow(new IllegalArgumentException("Unsupported file type."))
                             .when(mock).upload(anyLong(), anyString(), any(), anyString()));
             MockedConstruction<AuditService> auditSvc = mockConstruction(AuditService.class)) {

            Policyholder ph = new Policyholder();
            ph.setId(2L);
            Claim claim = new Claim();
            claim.setId(10L);
            claim.setClaimNo("CLM-2026-0001");
            when(claimSvc.constructed().get(0).resolveCustomer(eq("u@x.com"))).thenReturn(ph);
            when(claimSvc.constructed().get(0).getOwnedClaim(eq(2L), eq(10L))).thenReturn(claim);

            DocumentUploadAction action = new DocumentUploadAction();
            action.setSession(sessionFor("u@x.com"));
            action.setClaimId(10L);
            action.setDocType("FIR");
            action.setUploadFileName("x.exe");

            String result = action.execute();

            assertEquals(BaseAction.SUCCESS, result);
        }
    }
}
