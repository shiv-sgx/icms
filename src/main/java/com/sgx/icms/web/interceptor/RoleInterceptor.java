package com.sgx.icms.web.interceptor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.sgx.icms.domain.Roles;
import com.sgx.icms.web.support.SessionUser;

/**
 * Server-side authorisation: derives the required role from the action's URL
 * namespace ({@code /agent} → AGENT, etc.) and blocks principals whose role does
 * not match. ADMIN is intentionally allowed everywhere for support/oversight.
 *
 * <p>This runs after {@link AuthInterceptor}, so a principal is guaranteed present.
 * Enforcing here — not just by hiding nav links — closes the "type the URL directly"
 * hole.
 */
public class RoleInterceptor extends AbstractInterceptor {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(RoleInterceptor.class);

    /** Global result name returned on authorisation failure. */
    public static final String DENIED = "denied";

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        String namespace = invocation.getProxy().getNamespace();
        String requiredRole = Roles.forNamespace(namespace);
        if (requiredRole == null) {
            // Namespace not role-restricted (shared secured area) — allow.
            return invocation.invoke();
        }

        Map<String, Object> session = ActionContext.getContext().getSession();
        SessionUser user = session == null ? null : (SessionUser) session.get(SessionUser.SESSION_KEY);
        if (user == null) {
            return AuthInterceptor.LOGIN;
        }

        if (user.hasRole(requiredRole) || user.hasRole(Roles.ADMIN)) {
            return invocation.invoke();
        }

        LOG.warn("Access denied: user '{}' (role {}) attempted {}{}",
                user.getUsername(), user.getRole(), namespace, invocation.getProxy().getActionName());
        return DENIED;
    }
}
