package com.sgx.icms.domain;

import java.util.Arrays;
import java.util.List;

/**
 * Canonical role names (mirror the {@code roles.name} column). Used by the role
 * authorisation interceptor and navigation rendering. Kept as String constants
 * rather than an enum so they line up directly with DB values and namespaces.
 */
public final class Roles {

    public static final String CUSTOMER = "CUSTOMER";
    public static final String AGENT = "AGENT";
    public static final String SURVEYOR = "SURVEYOR";
    public static final String MANAGER = "MANAGER";
    public static final String ADMIN = "ADMIN";

    public static final List<String> ALL =
            Arrays.asList(CUSTOMER, AGENT, SURVEYOR, MANAGER, ADMIN);

    private Roles() {
    }

    /**
     * Maps a URL namespace (e.g. {@code /agent} or any sub-path like {@code /admin/config})
     * to the role allowed to use it. Prefix-matching ensures sub-namespaces stay guarded
     * and can never fall through to "unrestricted".
     */
    public static String forNamespace(String namespace) {
        if (namespace == null) {
            return null;
        }
        if (matches(namespace, "/customer")) return CUSTOMER;
        if (matches(namespace, "/agent"))    return AGENT;
        if (matches(namespace, "/surveyor")) return SURVEYOR;
        if (matches(namespace, "/manager"))  return MANAGER;
        if (matches(namespace, "/admin"))    return ADMIN;
        return null;
    }

    private static boolean matches(String namespace, String prefix) {
        return namespace.equals(prefix) || namespace.startsWith(prefix + "/");
    }

    /** Landing dashboard namespace for a freshly authenticated role. */
    public static String dashboardFor(String role) {
        if (role == null) {
            return "/";
        }
        switch (role) {
            case CUSTOMER: return "/customer/dashboard";
            case AGENT:    return "/agent/dashboard";
            case SURVEYOR: return "/surveyor/dashboard";
            case MANAGER:  return "/manager/dashboard";
            case ADMIN:    return "/admin/dashboard";
            default:       return "/";
        }
    }
}
