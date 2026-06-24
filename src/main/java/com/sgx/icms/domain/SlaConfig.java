package com.sgx.icms.domain;

/** Mirrors the {@code sla_config} table. */
public class SlaConfig {

    private int id;
    private String stage;
    private int hours;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public int getHours() { return hours; }
    public void setHours(int hours) { this.hours = hours; }
}
