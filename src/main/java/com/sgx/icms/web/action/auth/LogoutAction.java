package com.sgx.icms.web.action.auth;

import com.sgx.icms.service.AuditService;
import com.sgx.icms.web.action.BaseAction;
import com.sgx.icms.web.support.SessionUser;

/** Invalidates the session principal and audits the logout. */
public class LogoutAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private final transient AuditService audit = new AuditService();

    @Override
    public String execute() {
        SessionUser user = currentUser();
        if (user != null) {
            audit.success(user, "LOGOUT", "user:" + user.getUsername(), clientIp());
            session.remove(SessionUser.SESSION_KEY);
        }
        return SUCCESS;
    }
}
