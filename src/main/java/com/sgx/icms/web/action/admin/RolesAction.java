package com.sgx.icms.web.action.admin;

import java.util.Collections;
import java.util.List;

import com.sgx.icms.domain.Role;

/** Role &amp; permission overview (roles are fixed; shows user counts). */
public class RolesAction extends AdminBaseAction {

    private static final long serialVersionUID = 1L;

    private transient List<Role> roles = Collections.emptyList();

    @Override
    public String execute() {
        roles = adminService.roles();
        return SUCCESS;
    }

    public List<Role> getRoles() { return roles; }
}
