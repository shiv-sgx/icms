package com.sgx.icms.web.support;

/** System-monitoring snapshot for the admin dashboard. */
public class AdminStats {

    private long users;
    private long claims;
    private long roles;
    private long auditEvents;
    private int poolActive;
    private int poolIdle;
    private int poolTotal;

    public long getUsers() { return users; }
    public void setUsers(long users) { this.users = users; }

    public long getClaims() { return claims; }
    public void setClaims(long claims) { this.claims = claims; }

    public long getRoles() { return roles; }
    public void setRoles(long roles) { this.roles = roles; }

    public long getAuditEvents() { return auditEvents; }
    public void setAuditEvents(long auditEvents) { this.auditEvents = auditEvents; }

    public int getPoolActive() { return poolActive; }
    public void setPoolActive(int poolActive) { this.poolActive = poolActive; }

    public int getPoolIdle() { return poolIdle; }
    public void setPoolIdle(int poolIdle) { this.poolIdle = poolIdle; }

    public int getPoolTotal() { return poolTotal; }
    public void setPoolTotal(int poolTotal) { this.poolTotal = poolTotal; }
}
