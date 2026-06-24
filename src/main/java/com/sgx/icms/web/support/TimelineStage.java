package com.sgx.icms.web.support;

/** One node in a claim's status timeline (view model). */
public class TimelineStage {

    public static final String DONE = "done";
    public static final String CURRENT = "current";
    public static final String PENDING = "pending";

    private final String label;
    private final String state;

    public TimelineStage(String label, String state) {
        this.label = label;
        this.state = state;
    }

    public String getLabel() { return label; }
    public String getState() { return state; }
}
