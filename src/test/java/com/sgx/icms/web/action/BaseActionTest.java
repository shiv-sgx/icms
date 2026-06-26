package com.sgx.icms.web.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;

import com.sgx.icms.config.AppConfig;
import com.sgx.icms.domain.User;
import com.sgx.icms.web.support.SessionUser;

/** Same package as BaseAction, so its protected members are directly visible here. */
class BaseActionTest {

    static class TestAction extends BaseAction {
        private static final long serialVersionUID = 1L;
        @Override
        public String execute() { return SUCCESS; }
    }

    @Test
    void currentUser_nullSession_returnsNull() {
        TestAction action = new TestAction();
        assertNull(action.getCurrentUser());
    }

    @Test
    void currentUser_returnsPrincipalFromSession() {
        TestAction action = new TestAction();
        Map<String, Object> session = new HashMap<>();
        User u = new User();
        u.setId(1L);
        u.setUsername("bob");
        u.setFullName("Bob Jones");
        u.setEmail("bob@example.com");
        u.setRoleName("CUSTOMER");
        SessionUser su = SessionUser.from(u);
        session.put(SessionUser.SESSION_KEY, su);
        action.setSession(session);

        assertSame(su, action.getCurrentUser());
    }

    @Test
    void clientIp_noRequest_returnsNull() {
        TestAction action = new TestAction();
        assertNull(action.clientIp());
    }

    @Test
    void clientIp_noXForwardedFor_usesRemoteAddr() {
        TestAction action = new TestAction();
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("X-Forwarded-For")).thenReturn(null);
        when(req.getRemoteAddr()).thenReturn("10.0.0.5");
        action.setServletRequest(req);

        assertEquals("10.0.0.5", action.clientIp());
    }

    @Test
    void clientIp_singleXForwardedFor_usesIt() {
        TestAction action = new TestAction();
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("X-Forwarded-For")).thenReturn("203.0.113.7");
        action.setServletRequest(req);

        assertEquals("203.0.113.7", action.clientIp());
    }

    @Test
    void clientIp_multipleXForwardedFor_usesFirst() {
        TestAction action = new TestAction();
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("X-Forwarded-For")).thenReturn("203.0.113.7, 10.0.0.1, 10.0.0.2");
        action.setServletRequest(req);

        assertEquals("203.0.113.7", action.clientIp());
    }

    @Test
    void clientIp_emptyXForwardedFor_fallsBackToRemoteAddr() {
        TestAction action = new TestAction();
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("X-Forwarded-For")).thenReturn("");
        when(req.getRemoteAddr()).thenReturn("192.168.1.1");
        action.setServletRequest(req);

        assertEquals("192.168.1.1", action.clientIp());
    }

    @Test
    void defaultPageSize_matchesAppConfig() {
        TestAction action = new TestAction();
        assertEquals(AppConfig.get().getInt("icms.page.size", 15), action.defaultPageSize());
    }

    @Test
    void flash_withNullSession_isNoOpAndReturnsNull() {
        TestAction action = new TestAction();
        action.setFlash("success", "hi");
        assertNull(action.consumeFlash());
        assertEquals("success", action.consumeFlashType());
    }

    @Test
    void flash_setThenConsume_roundTrips() {
        TestAction action = new TestAction();
        action.setSession(new HashMap<>());
        action.setFlash("error", "Something went wrong.");

        assertEquals("error", action.consumeFlashType());
        assertEquals("Something went wrong.", action.consumeFlash());
        // second read returns null - flash is consumed
        assertNull(action.consumeFlash());
    }

    @Test
    void consumeFlashType_defaultsToSuccessWhenNoneSet() {
        TestAction action = new TestAction();
        action.setSession(new HashMap<>());
        assertEquals("success", action.consumeFlashType());
    }

    @Test
    void normalizePage_clampsBelowOne() {
        TestAction action = new TestAction();
        assertEquals(1, action.normalizePage(0));
        assertEquals(1, action.normalizePage(-5));
        assertEquals(3, action.normalizePage(3));
    }
}
