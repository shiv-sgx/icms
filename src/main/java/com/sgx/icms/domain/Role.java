package com.sgx.icms.domain;

/** Mirrors the {@code roles} table. */
public class Role {

    private int id;
    private String name;
    private String description;
    private long userCount;   // joined aggregate (admin views)

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getUserCount() { return userCount; }
    public void setUserCount(long userCount) { this.userCount = userCount; }
}
