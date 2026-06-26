package com.sgx.icms.web.action.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.sgx.icms.domain.Policy;
import com.sgx.icms.domain.Policyholder;
import com.sgx.icms.domain.User;
import com.sgx.icms.service.ClaimService;
import com.sgx.icms.web.action.BaseAction;
import com.sgx.icms.web.support.SessionUser;

class ProfileActionTest {

    @Test
    void execute_withProfile_loadsPolicies() {
        try (MockedConstruction<ClaimService> mocked = mockConstruction(ClaimService.class)) {
            Policyholder ph = new Policyholder();
            ph.setId(1L);
            when(mocked.constructed().get(0).resolveCustomer(eq("p@x.com"))).thenReturn(ph);
            when(mocked.constructed().get(0).policiesForCustomer(eq(1L)))
                    .thenReturn(Collections.singletonList(new Policy()));

            ProfileAction action = new ProfileAction();
            User u = new User();
            u.setId(1L);
            u.setEmail("p@x.com");
            u.setRoleName("CUSTOMER");
            HashMap<String, Object> session = new HashMap<>();
            session.put(SessionUser.SESSION_KEY, SessionUser.from(u));
            action.setSession(session);

            assertEquals(BaseAction.SUCCESS, action.execute());
            assertTrue(action.getPolicies().size() == 1);
        }
    }

    @Test
    void execute_withoutProfile_emptyPolicies() {
        try (MockedConstruction<ClaimService> mocked = mockConstruction(ClaimService.class)) {
            ProfileAction action = new ProfileAction();
            action.setSession(new HashMap<>());

            assertEquals(BaseAction.SUCCESS, action.execute());
            assertTrue(action.getPolicies().isEmpty());
        }
    }
}
