package com.sgx.icms.web.action.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.sgx.icms.domain.Policyholder;
import com.sgx.icms.domain.User;
import com.sgx.icms.service.ClaimService;
import com.sgx.icms.web.action.BaseAction;
import com.sgx.icms.web.support.SessionUser;

class WithdrawActionTest {

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
        try (MockedConstruction<ClaimService> mocked = mockConstruction(ClaimService.class)) {
            WithdrawAction action = new WithdrawAction();
            action.setSession(new HashMap<>());
            action.setClaimId(5L);

            assertEquals("missing", action.execute());
            assertEquals("/customer/claim?id=5", action.getRedirectUrl());
        }
    }

    @Test
    void execute_withdrawSucceeds_returnsSuccess() {
        try (MockedConstruction<ClaimService> mocked = mockConstruction(ClaimService.class)) {
            Policyholder ph = new Policyholder();
            ph.setId(2L);
            when(mocked.constructed().get(0).resolveCustomer(eq("w@x.com"))).thenReturn(ph);
            when(mocked.constructed().get(0).withdraw(any(), eq(2L), eq(5L), any())).thenReturn(true);

            WithdrawAction action = new WithdrawAction();
            action.setSession(sessionFor("w@x.com"));
            action.setClaimId(5L);

            assertEquals(BaseAction.SUCCESS, action.execute());
        }
    }

    @Test
    void execute_withdrawReturnsFalse_returnsSuccessWithErrorFlash() {
        try (MockedConstruction<ClaimService> mocked = mockConstruction(ClaimService.class)) {
            Policyholder ph = new Policyholder();
            ph.setId(2L);
            when(mocked.constructed().get(0).resolveCustomer(eq("w@x.com"))).thenReturn(ph);
            when(mocked.constructed().get(0).withdraw(any(), eq(2L), eq(5L), any())).thenReturn(false);

            WithdrawAction action = new WithdrawAction();
            action.setSession(sessionFor("w@x.com"));
            action.setClaimId(5L);

            assertEquals(BaseAction.SUCCESS, action.execute());
        }
    }

    @Test
    void execute_withdrawThrowsIllegalState_setsErrorFlash() {
        try (MockedConstruction<ClaimService> mocked = mockConstruction(ClaimService.class)) {
            Policyholder ph = new Policyholder();
            ph.setId(2L);
            when(mocked.constructed().get(0).resolveCustomer(eq("w@x.com"))).thenReturn(ph);
            when(mocked.constructed().get(0).withdraw(any(), eq(2L), eq(5L), any()))
                    .thenThrow(new IllegalStateException("Claim already settled."));

            WithdrawAction action = new WithdrawAction();
            action.setSession(sessionFor("w@x.com"));
            action.setClaimId(5L);

            assertEquals(BaseAction.SUCCESS, action.execute());
        }
    }
}
