package com.sgx.icms.web.action.surveyor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.opensymphony.xwork2.Action;
import com.sgx.icms.domain.Claim;
import com.sgx.icms.service.SurveyorService;
import com.sgx.icms.web.support.Paged;
import com.sgx.icms.web.support.SessionUser;

class SurveyorDashboardActionTest {

    @Test
    void execute_aggregatesCountsAndAssignedClaims() {
        SessionUser surveyor = new SessionUser(11, "sam", "Sam", "s@x.com", "SURVEYOR", "HQ");
        Map<String, Object> session = new HashMap<>();
        session.put(SessionUser.SESSION_KEY, surveyor);

        long[] counts = {5, 2, 3};
        Paged<Claim> page = new Paged<>(Collections.emptyList(), 1, 15, 5);

        try (MockedConstruction<SurveyorService> mocked = mockConstruction(SurveyorService.class)) {
            SurveyorDashboardAction action = new SurveyorDashboardAction();
            action.setSession(session);
            SurveyorService svc = mocked.constructed().get(0);
            when(svc.counts(11L)).thenReturn(counts);
            when(svc.assignedClaims(11L, 1, 15)).thenReturn(page);

            assertEquals(Action.SUCCESS, action.execute());
            assertEquals(5, action.getTotalAssigned());
            assertEquals(2, action.getPendingSurvey());
            assertEquals(3, action.getAssessed());
            assertSame(page, action.getClaims());
        }
    }
}
