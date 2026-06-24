package com.sgx.icms.domain;

import java.util.Arrays;
import java.util.List;

/**
 * Claim workflow status values (mirror the {@code claims.status} ENUM) plus the
 * canonical happy-path lifecycle order used to render the status timeline and to
 * guard transitions in {@code ClaimService}.
 */
public final class ClaimStatus {

    public static final String DRAFT = "DRAFT";
    public static final String SUBMITTED = "SUBMITTED";
    public static final String UNDER_REVIEW = "UNDER_REVIEW";
    public static final String SURVEY_SCHEDULED = "SURVEY_SCHEDULED";
    public static final String UNDER_ASSESSMENT = "UNDER_ASSESSMENT";
    public static final String PENDING_APPROVAL = "PENDING_APPROVAL";
    public static final String APPROVED = "APPROVED";
    public static final String SETTLEMENT_PROCESSING = "SETTLEMENT_PROCESSING";
    public static final String SETTLED = "SETTLED";
    public static final String CLOSED = "CLOSED";
    // Off-path states
    public static final String REJECTED = "REJECTED";
    public static final String WITHDRAWN = "WITHDRAWN";
    public static final String ON_HOLD = "ON_HOLD";

    /** Happy-path order for timeline rendering and stage comparison. */
    public static final List<String> LIFECYCLE = Arrays.asList(
            SUBMITTED, UNDER_REVIEW, SURVEY_SCHEDULED, UNDER_ASSESSMENT,
            PENDING_APPROVAL, APPROVED, SETTLEMENT_PROCESSING, SETTLED, CLOSED);

    private ClaimStatus() {
    }

    public static int lifecycleIndex(String status) {
        return LIFECYCLE.indexOf(status);
    }

    public static boolean isTerminal(String status) {
        return CLOSED.equals(status) || REJECTED.equals(status) || WITHDRAWN.equals(status);
    }

    /** A customer may withdraw only before the claim has been approved/settled. */
    public static boolean isWithdrawable(String status) {
        if (status == null) {
            return false;
        }
        switch (status) {
            case DRAFT:
            case SUBMITTED:
            case UNDER_REVIEW:
            case SURVEY_SCHEDULED:
            case UNDER_ASSESSMENT:
            case PENDING_APPROVAL:
            case ON_HOLD:
                return true;
            default:
                return false;
        }
    }

    /** Human-friendly label (e.g. PENDING_APPROVAL -> "Pending Approval"). */
    public static String label(String status) {
        if (status == null) {
            return "";
        }
        String[] parts = status.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) {
                continue;
            }
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }

    /** Pill CSS class for a status (used by JSPs). */
    public static String pill(String status) {
        if (status == null) {
            return "pill-muted";
        }
        switch (status) {
            case SETTLED:
            case APPROVED:
            case CLOSED:
                return "pill-ok";
            case REJECTED:
            case WITHDRAWN:
                return "pill-danger";
            case ON_HOLD:
            case PENDING_APPROVAL:
                return "pill-warn";
            case DRAFT:
                return "pill-muted";
            default:
                return "pill-info";
        }
    }
}
