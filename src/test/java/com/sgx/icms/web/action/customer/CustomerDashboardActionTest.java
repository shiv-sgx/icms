package com.sgx.icms.web.action.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.Policyholder;
import com.sgx.icms.domain.User;
import com.sgx.icms.service.ClaimService;
import com.sgx.icms.service.NotificationService;
import com.sgx.icms.web.action.BaseAction;
import com.sgx.icms.web.support.Paged;
import com.sgx.icms.web.support.SessionUser;

class CustomerDashboardActionTest {

    @Test
    void execute_withProfile_populatesCountsAndNotifications() {
        try (MockedConstruction<ClaimService> claimSvc = mockConstruction(ClaimService.class);
             MockedConstruction<NotificationService> notifSvc = mockConstruction(NotificationService.class)) {

            Policyholder ph = new Policyholder();
            ph.setId(11L);
            ClaimService cs = claimSvc.constructed().get(0);
            when(cs.resolveCustomer(eq("e@x.com"))).thenReturn(ph);
            when(cs.customerCounts(eq(11L))).thenReturn(new long[] {5L, 2L, 3L});
            Paged<Claim> page = new Paged<>(Collections.singletonList(new Claim()), 1, 5, 5);
            when(cs.listForCustomer(eq(11L), eq(1), eq(5))).thenReturn(page);

            CustomerDashboardAction action = new CustomerDashboardAction();
            User u = new User();
            u.setId(1L);
            u.setEmail("e@x.com");
            u.setRoleName("CUSTOMER");
            HashMap<String, Object> session = new HashMap<>();
            session.put(SessionUser.SESSION_KEY, SessionUser.from(u));
            action.setSession(session);

            String result = action.execute();

            assertEquals(BaseAction.SUCCESS, result);
            assertEquals(5L, action.getTotalClaims());
            assertEquals(2L, action.getOpenClaims());
            assertEquals(3L, action.getSettledClaims());
            assertEquals(1, action.getRecentClaims().size());
        }
    }

    @Test
    void execute_withoutProfile_stillLoadsNotifications() {
        try (MockedConstruction<ClaimService> claimSvc = mockConstruction(ClaimService.class);
             MockedConstruction<NotificationService> notifSvc = mockConstruction(NotificationService.class)) {

            when(notifSvc.constructed().get(0).recentForUser(eq(1L), eq("CUSTOMER"), anyInt()))
                    .thenReturn(Collections.emptyList());

            CustomerDashboardAction action = new CustomerDashboardAction();
            User u = new User();
            u.setId(1L);
            u.setEmail(null);
            u.setRoleName("CUSTOMER");
            HashMap<String, Object> session = new HashMap<>();
            session.put(SessionUser.SESSION_KEY, SessionUser.from(u));
            action.setSession(session);

            String result = action.execute();

            assertEquals(BaseAction.SUCCESS, result);
            assertEquals(0L, action.getTotalClaims());
            assertTrue(action.getNotifications().isEmpty());
        }
    }
}
