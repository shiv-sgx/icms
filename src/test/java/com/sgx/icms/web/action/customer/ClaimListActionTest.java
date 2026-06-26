package com.sgx.icms.web.action.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
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
import com.sgx.icms.web.support.Paged;
import com.sgx.icms.web.support.SessionUser;

class ClaimListActionTest {

    @Test
    void execute_withProfile_listsClaims() {
        try (MockedConstruction<ClaimService> mocked = mockConstruction(ClaimService.class)) {
            Policyholder ph = new Policyholder();
            ph.setId(9L);
            ClaimService svc = mocked.constructed().get(0);
            when(svc.resolveCustomer(eq("c@x.com"))).thenReturn(ph);
            Paged<Claim> page = new Paged<>(Collections.emptyList(), 1, 15, 0);
            when(svc.listForCustomer(eq(9L), anyInt(), anyInt())).thenReturn(page);

            ClaimListAction action = new ClaimListAction();
            User u = new User();
            u.setId(2L);
            u.setEmail("c@x.com");
            u.setRoleName("CUSTOMER");
            var session = new HashMap<String, Object>();
            session.put(SessionUser.SESSION_KEY, SessionUser.from(u));
            action.setSession(session);
            action.setPage(1);

            String result = action.execute();

            assertEquals(BaseAction.SUCCESS, result);
            assertEquals(page, action.getClaims());
        }
    }

    @Test
    void execute_withoutProfile_leavesClaimsNull() {
        try (MockedConstruction<ClaimService> mocked = mockConstruction(ClaimService.class)) {
            ClaimListAction action = new ClaimListAction();
            action.setSession(new HashMap<>());

            String result = action.execute();

            assertEquals(BaseAction.SUCCESS, result);
            assertNull(action.getClaims());
        }
    }

    @Test
    void pageGetterSetter() {
        ClaimListAction action = new ClaimListAction();
        action.setPage(4);
        assertEquals(4, action.getPage());
    }
}
