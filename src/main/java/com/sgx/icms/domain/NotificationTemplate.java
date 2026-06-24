package com.sgx.icms.domain;

/** Mirrors the {@code notification_templates} table. */
public class NotificationTemplate {

    private int id;
    private String name;
    private String channel;   // SMS/EMAIL
    private boolean active;
    private String body;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}
