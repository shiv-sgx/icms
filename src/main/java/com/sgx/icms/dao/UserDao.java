package com.sgx.icms.dao;

import java.sql.Connection;
import java.util.List;

import com.sgx.icms.domain.User;

/** Persistence operations for {@code users} (+ joined role name). */
public interface UserDao {

    User findByUsername(Connection conn, String username);

    User findById(Connection conn, long id);

    void updateLastLogin(Connection conn, long userId);

    /** Active users with the given role name (e.g. for surveyor/agent pickers). */
    List<User> findActiveByRole(Connection conn, String roleName);

    /* ---- admin user management ---- */

    List<User> search(Connection conn, String q, String roleName, int limit, int offset);

    long countSearch(Connection conn, String q, String roleName);

    long insert(Connection conn, User u);

    void updateStatusAndRole(Connection conn, long userId, String status, int roleId);

    void updatePassword(Connection conn, long userId, String passwordHash);

    boolean existsByUsernameOrEmail(Connection conn, String username, String email);
}
