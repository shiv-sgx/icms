package com.sgx.icms.web.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.sgx.icms.domain.Roles;
import com.sgx.icms.web.support.SessionUser;

class RoleInterceptorTest {

    private final RoleInterceptor interceptor = new RoleInterceptor();

    @AfterEach
    void clearContext() {
        ActionContext.setContext(null);
    }

    private static ActionInvocation invocationWithNamespace(String namespace) {
        ActionInvocation invocation = mock(ActionInvocation.class);
        ActionProxy proxy = mock(ActionProxy.class);
        when(proxy.getNamespace()).thenReturn(namespace);
        when(proxy.getActionName()).thenReturn("action");
        when(invocation.getProxy()).thenReturn(proxy);
        return invocation;
    }

    private static void setSessionUser(SessionUser user) {
        Map<String, Object> session = new HashMap<>();
        if (user != null) {
            session.put(SessionUser.SESSION_KEY, user);
        }
        ActionContext ctx = new ActionContext(new HashMap<>());
        ctx.setSession(session);
        ActionContext.setContext(ctx);
    }

    @Test
    void allowsUnrestrictedNamespaceWithoutCheckingSession() throws Exception {
        ActionContext.setContext(null);
        ActionInvocation invocation = invocationWithNamespace("/shared");
        when(invocation.invoke()).thenReturn("success");

        String result = interceptor.intercept(invocation);

        assertEquals("success", result);
        verify(invocation).invoke();
    }

    @Test
    void redirectsToLoginWhenRestrictedNamespaceHasNoPrincipal() throws Exception {
        setSessionUser(null);
        ActionInvocation invocation = invocationWithNamespace("/agent");

        String result = interceptor.intercept(invocation);

        assertEquals(AuthInterceptor.LOGIN, result);
        verify(invocation, never()).invoke();
    }

    @Test
    void deniesWhenRoleDoesNotMatchNamespace() throws Exception {
        setSessionUser(new SessionUser(1L, "carol", "Carol", "c@x.com", Roles.CUSTOMER, "BR1"));
        ActionInvocation invocation = invocationWithNamespace("/agent");

        String result = interceptor.intercept(invocation);

        assertEquals(RoleInterceptor.DENIED, result);
        verify(invocation, never()).invoke();
    }

    @Test
    void allowsWhenRoleMatchesNamespace() throws Exception {
        setSessionUser(new SessionUser(1L, "alice", "Alice", "a@x.com", Roles.AGENT, "BR1"));
        ActionInvocation invocation = invocationWithNamespace("/agent");
        when(invocation.invoke()).thenReturn("success");

        String result = interceptor.intercept(invocation);

        assertEquals("success", result);
        verify(invocation).invoke();
    }

    @Test
    void allowsAdminEverywhere() throws Exception {
        setSessionUser(new SessionUser(1L, "root", "Root", "r@x.com", Roles.ADMIN, "BR1"));
        ActionInvocation invocation = invocationWithNamespace("/manager");
        when(invocation.invoke()).thenReturn("success");

        String result = interceptor.intercept(invocation);

        assertEquals("success", result);
        verify(invocation).invoke();
    }

    @Test
    void allowsMatchingSubNamespace() throws Exception {
        setSessionUser(new SessionUser(1L, "root", "Root", "r@x.com", Roles.ADMIN, "BR1"));
        ActionInvocation invocation = invocationWithNamespace("/admin/config");
        when(invocation.invoke()).thenReturn("success");

        String result = interceptor.intercept(invocation);

        assertEquals("success", result);
        verify(invocation).invoke();
    }
}
