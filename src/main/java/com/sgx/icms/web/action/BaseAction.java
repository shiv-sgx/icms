package com.sgx.icms.web.action;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;

import com.opensymphony.xwork2.ActionSupport;
import com.sgx.icms.config.AppConfig;
import com.sgx.icms.web.support.SessionUser;

import org.apache.struts2.interceptor.SessionAware;

/**
 * Common base for all ICMS actions: exposes the authenticated {@link SessionUser},
 * the client IP (for audit), and a default page size. Thin by design — business
 * logic stays in services.
 */
public abstract class BaseAction extends ActionSupport implements SessionAware, ServletRequestAware {

    private static final long serialVersionUID = 1L;

    protected transient Map<String, Object> session;
    protected transient HttpServletRequest request;

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    @Override
    public void setServletRequest(HttpServletRequest request) {
        this.request = request;
    }

    protected SessionUser currentUser() {
        return session == null ? null : (SessionUser) session.get(SessionUser.SESSION_KEY);
    }

    /** Exposed to JSPs as {@code currentUser} for the layout/header. */
    public SessionUser getCurrentUser() {
        return currentUser();
    }

    protected String clientIp() {
        if (request == null) {
            return null;
        }
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            int comma = xff.indexOf(',');
            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
        }
        return request.getRemoteAddr();
    }

    protected int defaultPageSize() {
        return AppConfig.get().getInt("icms.page.size", 15);
    }

    /* ---- Flash messages: survive a post-redirect-get via the session ---- */
    protected static final String FLASH_MSG = "FLASH_MSG";
    protected static final String FLASH_TYPE = "FLASH_TYPE";

    /** type is "success" or "error" (drives the alert CSS class). */
    protected void setFlash(String type, String message) {
        if (session != null) {
            session.put(FLASH_TYPE, type);
            session.put(FLASH_MSG, message);
        }
    }

    /** Reads and clears the flash message (call once per render). */
    protected String consumeFlash() {
        if (session == null) {
            return null;
        }
        Object msg = session.remove(FLASH_MSG);
        return msg == null ? null : msg.toString();
    }

    protected String consumeFlashType() {
        if (session == null) {
            return null;
        }
        Object t = session.remove(FLASH_TYPE);
        return t == null ? "success" : t.toString();
    }

    /** Clamps a 1-based page request to a sane minimum. */
    protected int normalizePage(int page) {
        return page < 1 ? 1 : page;
    }
}
