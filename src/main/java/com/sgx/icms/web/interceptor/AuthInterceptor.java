package com.sgx.icms.web.interceptor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.sgx.icms.web.support.SessionUser;

/**
 * Gatekeeper for secured packages: if there is no {@link SessionUser} in session,
 * the request is bounced to the login page ({@code "login"} global result) before
 * the action ever runs.
 */
public class AuthInterceptor extends AbstractInterceptor {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(AuthInterceptor.class);

    /** Global result name returned when authentication is missing. */
    public static final String LOGIN = "login";

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        Map<String, Object> session = ActionContext.getContext().getSession();
        Object principal = session == null ? null : session.get(SessionUser.SESSION_KEY);
        if (principal == null) {
            LOG.debug("Unauthenticated access to {} — redirecting to login",
                    invocation.getProxy().getActionName());
            return LOGIN;
        }
        return invocation.invoke();
    }
}
