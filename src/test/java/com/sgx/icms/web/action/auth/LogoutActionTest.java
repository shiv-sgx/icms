package com.sgx.icms.web.action.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.sgx.icms.domain.User;
import com.sgx.icms.service.AuditService;
import com.sgx.icms.web.action.BaseAction;
import com.sgx.icms.web.support.SessionUser;

class LogoutActionTest {

    @Test
    void execute_withLoggedInUser_removesSessionAndAudits() {
        try (MockedConstruction<AuditService> mocked = mockConstruction(AuditService.class)) {
            LogoutAction action = new LogoutAction();
            User u = new User();
            u.setId(1L);
            u.setUsername("dave");
            u.setRoleName("CUSTOMER");
            Map<String, Object> session = new HashMap<>();
            session.put(SessionUser.SESSION_KEY, SessionUser.from(u));
            action.setSession(session);

            String result = action.execute();

            assertEquals(BaseAction.SUCCESS, result);
            assertFalse(session.containsKey(SessionUser.SESSION_KEY));
            verify(mocked.constructed().get(0)).success(org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.eq("LOGOUT"),
                    org.mockito.ArgumentMatchers.eq("user:dave"),
                    org.mockito.ArgumentMatchers.any());
        }
    }

    @Test
    void execute_withNoLoggedInUser_isNoOp() {
        try (MockedConstruction<AuditService> mocked = mockConstruction(AuditService.class)) {
            LogoutAction action = new LogoutAction();
            action.setSession(new HashMap<>());

            String result = action.execute();

            assertEquals(BaseAction.SUCCESS, result);
            verifyNoInteractions(mocked.constructed().get(0));
        }
    }
}
