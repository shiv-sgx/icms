package com.sgx.icms.web.action.admin;

import java.util.Collections;
import java.util.List;

import com.sgx.icms.domain.Role;
import com.sgx.icms.domain.User;
import com.sgx.icms.web.support.Paged;

/** User management: search/list, create, update (status/role), reset password. */
public class UserAdminAction extends AdminBaseAction {

    private static final long serialVersionUID = 1L;

    // list/filter
    private String q;
    private String role;
    private int page = 1;
    private transient Paged<User> users;
    private transient List<Role> roles = Collections.emptyList();
    private String flashMessage;
    private String flashType;

    // create
    private String fullName;
    private String email;
    private String username;
    private String password;
    private int roleId;
    private String branch;

    // update / reset
    private long userId;
    private String status;
    private String newPassword;

    private String redirectUrl = "/admin/users";

    public String list() {
        roles = adminService.roles();
        users = adminService.searchUsers(trim(q), trim(role), normalizePage(page), defaultPageSize());
        flashMessage = consumeFlash();
        flashType = consumeFlashType();
        return SUCCESS;
    }

    public String create() {
        try {
            User u = new User();
            u.setFullName(fullName);
            u.setEmail(email == null ? null : email.trim());
            u.setUsername(username == null ? null : username.trim());
            u.setRoleId(roleId);
            u.setBranch(trim(branch));
            u.setStatus("ACTIVE");
            adminService.createUser(currentUser(), u, password, clientIp());
            setFlash("success", "User created.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            setFlash("error", e.getMessage());
        }
        return SUCCESS;
    }

    public String update() {
        try {
            adminService.updateUser(currentUser(), userId, status, roleId, clientIp());
            setFlash("success", "User updated.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            setFlash("error", e.getMessage());
        }
        return SUCCESS;
    }

    public String resetPassword() {
        try {
            adminService.resetPassword(currentUser(), userId, newPassword, clientIp());
            setFlash("success", "Password reset.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            setFlash("error", e.getMessage());
        }
        return SUCCESS;
    }

    private static String trim(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    public void setQ(String q) { this.q = q; }
    public String getQ() { return q; }
    public void setRole(String role) { this.role = role; }
    public String getRole() { return role; }
    public void setPage(int page) { this.page = page; }
    public int getPage() { return page; }
    public Paged<User> getUsers() { return users; }
    public List<Role> getRoles() { return roles; }
    public String getFlashMessage() { return flashMessage; }
    public String getFlashType() { return flashType; }
    public String getRedirectUrl() { return redirectUrl; }

    public void setFullName(String v) { this.fullName = v; }
    public void setEmail(String v) { this.email = v; }
    public void setUsername(String v) { this.username = v; }
    public void setPassword(String v) { this.password = v; }
    public void setRoleId(int v) { this.roleId = v; }
    public void setBranch(String v) { this.branch = v; }
    public void setUserId(long v) { this.userId = v; }
    public void setStatus(String v) { this.status = v; }
    public void setNewPassword(String v) { this.newPassword = v; }
}
