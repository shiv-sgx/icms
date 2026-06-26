package com.sgx.icms.web.action.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.opensymphony.xwork2.Action;
import com.sgx.icms.domain.Role;
import com.sgx.icms.service.AdminService;

class RolesActionTest {

    @Test
    void execute_returnsRolesFromService() {
        List<Role> roles = Collections.singletonList(new Role());
        try (MockedConstruction<AdminService> mocked = mockConstruction(AdminService.class)) {
            RolesAction action = new RolesAction();
            AdminService svc = mocked.constructed().get(0);
            when(svc.roles()).thenReturn(roles);

            assertEquals(Action.SUCCESS, action.execute());
            assertSame(roles, action.getRoles());
        }
    }
}
