package com.sgx.icms.web.action.admin;

import com.sgx.icms.service.AdminService;
import com.sgx.icms.web.action.BaseAction;

/** Shared base for admin-portal actions. */
public abstract class AdminBaseAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    protected final transient AdminService adminService = new AdminService();
}
