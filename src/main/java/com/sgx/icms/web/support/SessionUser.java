package com.sgx.icms.web.support;

import java.io.Serializable;

import com.sgx.icms.domain.User;

/**
 * Minimal authenticated-principal stored in the HTTP session. Deliberately does
 * NOT carry the password hash or other sensitive fields.
 */
public class SessionUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Session attribute key under which this principal is stored. */
    public static final String SESSION_KEY = "ICMS_USER";

    private final long id;
    private final String username;
    private final String fullName;
    private final String email;
    private final String role;
    private final String branch;

    public SessionUser(long id, String username, String fullName, String email, String role, String branch) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.branch = branch;
    }

    public static SessionUser from(User u) {
        return new SessionUser(u.getId(), u.getUsername(), u.getFullName(), u.getEmail(), u.getRoleName(), u.getBranch());
    }

    public long getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getBranch() { return branch; }

    public boolean hasRole(String r) { return role != null && role.equalsIgnoreCase(r); }
}
