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
import com.sgx.icms.web.support.SessionUser;

class AuthInterceptorTest {

    private final AuthInterceptor interceptor = new AuthInterceptor();

    @AfterEach
    void clearContext() {
        ActionContext.setContext(null);
    }

    private static ActionInvocation invocationWithActionName(String name) {
        ActionInvocation invocation = mock(ActionInvocation.class);
        ActionProxy proxy = mock(ActionProxy.class);
        when(proxy.getActionName()).thenReturn(name);
        when(invocation.getProxy()).thenReturn(proxy);
        return invocation;
    }

    @Test
    void redirectsToLoginWhenNoSession() throws Exception {
        ActionContext ctx = new ActionContext(new HashMap<>());
        ctx.setSession(new HashMap<>());
        ActionContext.setContext(ctx);
        ActionInvocation invocation = invocationWithActionName("dashboard");

        String result = interceptor.intercept(invocation);

        assertEquals(AuthInterceptor.LOGIN, result);
        verify(invocation, never()).invoke();
    }

    @Test
    void redirectsToLoginWhenSessionMapIsNull() throws Exception {
        ActionContext ctx = new ActionContext(new HashMap<>());
        ctx.setSession(null);
        ActionContext.setContext(ctx);
        ActionInvocation invocation = invocationWithActionName("dashboard");

        String result = interceptor.intercept(invocation);

        assertEquals(AuthInterceptor.LOGIN, result);
        verify(invocation, never()).invoke();
    }

    @Test
    void invokesActionWhenPrincipalPresent() throws Exception {
        Map<String, Object> session = new HashMap<>();
        session.put(SessionUser.SESSION_KEY, new SessionUser(1L, "bob", "Bob", "b@x.com", "AGENT", "BR1"));
        ActionContext ctx = new ActionContext(new HashMap<>());
        ctx.setSession(session);
        ActionContext.setContext(ctx);
        ActionInvocation invocation = invocationWithActionName("dashboard");
        when(invocation.invoke()).thenReturn("success");

        String result = interceptor.intercept(invocation);

        assertEquals("success", result);
        verify(invocation).invoke();
    }
}
