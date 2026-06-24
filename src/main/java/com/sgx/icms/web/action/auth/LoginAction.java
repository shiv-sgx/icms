package com.sgx.icms.web.action.auth;

import com.sgx.icms.domain.Roles;
import com.sgx.icms.domain.User;
import com.sgx.icms.service.AuthService;
import com.sgx.icms.web.action.BaseAction;
import com.sgx.icms.web.support.SessionUser;

/**
 * Processes the login form. On success, stores a {@link SessionUser} and exposes
 * a role-specific dashboard URL for a post-redirect-get to the right portal.
 */
public class LoginAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private final transient AuthService authService = new AuthService();

    private String username;
    private String password;
    private String redirectUrl;

    @Override
    public String execute() {
        User user = authService.authenticate(username, password, clientIp());
        if (user == null) {
            addActionError(getText("login.error.invalid", "Invalid username or password."));
            return INPUT;
        }
        // Rotate is ideal; with the Struts session Map we at least replace the principal.
        session.put(SessionUser.SESSION_KEY, SessionUser.from(user));
        this.redirectUrl = Roles.dashboardFor(user.getRoleName());
        return SUCCESS;
    }

    public void setUsername(String username) { this.username = username; }
    public String getUsername() { return username; }

    public void setPassword(String password) { this.password = password; }

    public String getRedirectUrl() { return redirectUrl; }
}
