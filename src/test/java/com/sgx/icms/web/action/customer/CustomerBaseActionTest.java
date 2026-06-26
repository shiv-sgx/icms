package com.sgx.icms.web.action.customer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.sgx.icms.domain.Policyholder;
import com.sgx.icms.domain.User;
import com.sgx.icms.service.ClaimService;
import com.sgx.icms.web.support.SessionUser;

class CustomerBaseActionTest {

    static class TestAction extends CustomerBaseAction {
        private static final long serialVersionUID = 1L;
        @Override
        public String execute() { return SUCCESS; }
    }

    private static SessionUser sessionUserWithEmail(String email) {
        User u = new User();
        u.setId(1L);
        u.setUsername("cust1");
        u.setEmail(email);
        u.setRoleName("CUSTOMER");
        return SessionUser.from(u);
    }

    @Test
    void policyholder_noCurrentUser_returnsNull() {
        try (MockedConstruction<ClaimService> mocked = mockConstruction(ClaimService.class)) {
            TestAction action = new TestAction();
            action.setSession(new HashMap<>());

            assertNull(action.getPolicyholder());
            assertFalse(action.isHasProfile());
        }
    }

    @Test
    void policyholder_resolvesByEmailAndCaches() {
        try (MockedConstruction<ClaimService> mocked = mockConstruction(ClaimService.class)) {
            Policyholder ph = new Policyholder();
            when(mocked.constructed().get(0).resolveCustomer(eq("a@example.com"))).thenReturn(ph);

            TestAction action = new TestAction();
            var session = new HashMap<String, Object>();
            session.put(SessionUser.SESSION_KEY, sessionUserWithEmail("a@example.com"));
            action.setSession(session);

            assertSame(ph, action.getPolicyholder());
            assertTrue(action.isHasProfile());
            // second call should use the cached value, not call the service again
            action.getPolicyholder();
            verify(mocked.constructed().get(0), times(1)).resolveCustomer(eq("a@example.com"));
        }
    }

    @Test
    void policyholder_serviceReturnsNull_hasProfileFalse() {
        try (MockedConstruction<ClaimService> mocked = mockConstruction(ClaimService.class)) {
            when(mocked.constructed().get(0).resolveCustomer(eq("none@example.com"))).thenReturn(null);

            TestAction action = new TestAction();
            var session = new HashMap<String, Object>();
            session.put(SessionUser.SESSION_KEY, sessionUserWithEmail("none@example.com"));
            action.setSession(session);

            assertNull(action.getPolicyholder());
            assertFalse(action.isHasProfile());
        }
    }
}
