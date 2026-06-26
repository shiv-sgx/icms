package com.sgx.icms.web.action.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.sgx.icms.domain.Roles;
import com.sgx.icms.domain.User;
import com.sgx.icms.service.AuthService;
import com.sgx.icms.web.action.BaseAction;
import com.sgx.icms.web.support.SessionUser;

class LoginActionTest {

    @Test
    void execute_validCredentials_putsSessionUserAndReturnsSuccess() {
        try (MockedConstruction<AuthService> mocked = mockConstruction(AuthService.class)) {
            User user = new User();
            user.setId(7L);
            user.setUsername("alice");
            user.setFullName("Alice Smith");
            user.setEmail("alice@example.com");
            user.setRoleName(Roles.AGENT);

            LoginAction action = new LoginAction();
            when(mocked.constructed().get(0).authenticate(any(), any(), any())).thenReturn(user);

            action.setUsername("alice");
            action.setPassword("secret");
            Map<String, Object> session = new HashMap<>();
            action.setSession(session);

            String result = action.execute();

            assertEquals(BaseAction.SUCCESS, result);
            assertEquals("/agent/dashboard", action.getRedirectUrl());
            SessionUser stored = (SessionUser) session.get(SessionUser.SESSION_KEY);
            assertTrue(stored != null && stored.getUsername().equals("alice"));
        }
    }

    @Test
    void execute_invalidCredentials_returnsInputWithActionError() {
        try (MockedConstruction<AuthService> mocked = mockConstruction(AuthService.class)) {
            LoginAction realAction = new LoginAction();
            when(mocked.constructed().get(0).authenticate(any(), any(), any())).thenReturn(null);

            LoginAction action = spy(realAction);
            doReturn("Invalid username or password.").when(action).getText(anyString(), anyString());

            action.setUsername("bob");
            action.setPassword("wrong");
            action.setSession(new HashMap<>());

            String result = action.execute();

            assertEquals(BaseAction.INPUT, result);
            assertTrue(action.getActionErrors().contains("Invalid username or password."));
        }
    }

    @Test
    void usernameGetterSetter() {
        LoginAction action = new LoginAction();
        action.setUsername("carol");
        assertEquals("carol", action.getUsername());
    }
}
