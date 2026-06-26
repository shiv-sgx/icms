package com.sgx.icms.web.action.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.opensymphony.xwork2.Action;
import com.sgx.icms.domain.Role;
import com.sgx.icms.domain.User;
import com.sgx.icms.service.AdminService;
import com.sgx.icms.web.support.Paged;
import com.sgx.icms.web.support.SessionUser;

class UserAdminActionTest {

    private static java.util.Map<String, Object> sessionWith(SessionUser u) {
        java.util.Map<String, Object> session = new HashMap<>();
        session.put(SessionUser.SESSION_KEY, u);
        return session;
    }

    @Test
    void list_loadsRolesAndSearchResults() {
        List<Role> roles = Collections.singletonList(new Role());
        Paged<User> users = new Paged<>(Collections.emptyList(), 1, 15, 0);
        try (MockedConstruction<AdminService> mocked = mockConstruction(AdminService.class)) {
            UserAdminAction action = new UserAdminAction();
            action.setSession(new HashMap<>());
            AdminService svc = mocked.constructed().get(0);
            when(svc.roles()).thenReturn(roles);
            when(svc.searchUsers(null, null, 1, 15)).thenReturn(users);

            action.setQ("  ");
            action.setRole("");

            assertEquals(Action.SUCCESS, action.list());
            assertSame(roles, action.getRoles());
            assertSame(users, action.getUsers());
        }
    }

    @Test
    void create_success_setsSuccessFlash() {
        try (MockedConstruction<AdminService> mocked = mockConstruction(AdminService.class)) {
            UserAdminAction action = new UserAdminAction();
            action.setSession(sessionWith(new SessionUser(1, "admin", "Admin", "a@x.com", "ADMIN", "HQ")));
            action.setFullName("Bob");
            action.setEmail(" bob@x.com ");
            action.setUsername(" bob ");
            action.setPassword("secret1");
            action.setRoleId(2);
            action.setBranch("HQ");

            assertEquals(Action.SUCCESS, action.create());
            assertEquals("success", action.getFlashType());
        }
    }

    @Test
    void create_duplicateUser_setsErrorFlash() {
        try (MockedConstruction<AdminService> mocked = mockConstruction(AdminService.class,
                (mock, ctx) -> doThrow(new IllegalStateException("A user with that username or email already exists."))
                        .when(mock).createUser(any(), any(), any(), any()))) {
            UserAdminAction action = new UserAdminAction();
            action.setSession(sessionWith(new SessionUser(1, "admin", "Admin", "a@x.com", "ADMIN", "HQ")));
            action.setUsername("bob");
            action.setEmail("bob@x.com");
            action.setFullName("Bob");
            action.setPassword("secret1");

            assertEquals(Action.SUCCESS, action.create());
            assertEquals("error", action.getFlashType());
            assertEquals("A user with that username or email already exists.", action.getFlashMessage());
        }
    }

    @Test
    void update_success() {
        try (MockedConstruction<AdminService> mocked = mockConstruction(AdminService.class)) {
            UserAdminAction action = new UserAdminAction();
            action.setSession(sessionWith(new SessionUser(1, "admin", "Admin", "a@x.com", "ADMIN", "HQ")));
            action.setUserId(7);
            action.setStatus("INACTIVE");
            action.setRoleId(3);

            assertEquals(Action.SUCCESS, action.update());
            assertEquals("success", action.getFlashType());
            AdminService svc = mocked.constructed().get(0);
            verify(svc).updateUser(any(), eq(7L), eq("INACTIVE"), eq(3), any());
        }
    }

    @Test
    void update_userNotFound_setsErrorFlash() {
        try (MockedConstruction<AdminService> mocked = mockConstruction(AdminService.class,
                (mock, ctx) -> doThrow(new IllegalStateException("User not found."))
                        .when(mock).updateUser(any(), anyLong(), any(), anyInt(), any()))) {
            UserAdminAction action = new UserAdminAction();
            action.setSession(sessionWith(new SessionUser(1, "admin", "Admin", "a@x.com", "ADMIN", "HQ")));

            assertEquals(Action.SUCCESS, action.update());
            assertEquals("error", action.getFlashType());
        }
    }

    @Test
    void resetPassword_success() {
        try (MockedConstruction<AdminService> mocked = mockConstruction(AdminService.class)) {
            UserAdminAction action = new UserAdminAction();
            action.setSession(sessionWith(new SessionUser(1, "admin", "Admin", "a@x.com", "ADMIN", "HQ")));
            action.setUserId(7);
            action.setNewPassword("newsecret1");

            assertEquals(Action.SUCCESS, action.resetPassword());
            assertEquals("success", action.getFlashType());
        }
    }

    @Test
    void resetPassword_tooShort_setsErrorFlash() {
        try (MockedConstruction<AdminService> mocked = mockConstruction(AdminService.class,
                (mock, ctx) -> doThrow(new IllegalArgumentException("Password must be at least 6 characters."))
                        .when(mock).resetPassword(any(), anyLong(), any(), any()))) {
            UserAdminAction action = new UserAdminAction();
            action.setSession(sessionWith(new SessionUser(1, "admin", "Admin", "a@x.com", "ADMIN", "HQ")));
            action.setNewPassword("123");

            assertEquals(Action.SUCCESS, action.resetPassword());
            assertEquals("error", action.getFlashType());
        }
    }
}
