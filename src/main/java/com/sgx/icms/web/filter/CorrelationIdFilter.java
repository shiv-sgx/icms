package com.sgx.icms.web.filter;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;

/**
 * Assigns a short correlation id to every request so log lines can be traced
 * end-to-end (rendered via {@code %X{cid}} in the log pattern) and echoed back in the
 * {@code X-Correlation-Id} response header. Honours an inbound id if a trusted proxy
 * sets one. Always clears the MDC to avoid leaking ids across pooled threads.
 */
public class CorrelationIdFilter implements Filter {

    private static final String MDC_KEY = "cid";
    private static final String HEADER = "X-Correlation-Id";

    @Override
    public void init(FilterConfig filterConfig) {
        // no-op
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String cid = null;
        if (request instanceof HttpServletRequest) {
            cid = ((HttpServletRequest) request).getHeader(HEADER);
        }
        if (cid == null || cid.isEmpty() || cid.length() > 64) {
            cid = UUID.randomUUID().toString().substring(0, 8);
        }
        MDC.put(MDC_KEY, cid);
        try {
            if (response instanceof HttpServletResponse) {
                ((HttpServletResponse) response).setHeader(HEADER, cid);
            }
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    @Override
    public void destroy() {
        // no-op
    }
}
