package com.sgx.icms.web.action.customer;

import java.util.Collections;
import java.util.List;

import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.Notification;
import com.sgx.icms.service.NotificationService;

/** Customer dashboard: claim KPIs, recent claims, and notifications. */
public class CustomerDashboardAction extends CustomerBaseAction {

    private static final long serialVersionUID = 1L;

    private final transient NotificationService notificationService = new NotificationService();

    private long totalClaims;
    private long openClaims;
    private long settledClaims;
    private List<Claim> recentClaims = Collections.emptyList();
    private List<Notification> notifications = Collections.emptyList();

    @Override
    public String execute() {
        if (policyholder() != null) {
            long[] counts = claimService.customerCounts(policyholder().getId());
            totalClaims = counts[0];
            openClaims = counts[1];
            settledClaims = counts[2];
            recentClaims = claimService.listForCustomer(policyholder().getId(), 1, 5).getItems();
        }
        notifications = notificationService.recentForUser(
                currentUser().getId(), currentUser().getRole(), 5);
        return SUCCESS;
    }

    public long getTotalClaims() { return totalClaims; }
    public long getOpenClaims() { return openClaims; }
    public long getSettledClaims() { return settledClaims; }
    public List<Claim> getRecentClaims() { return recentClaims; }
    public List<Notification> getNotifications() { return notifications; }
}
