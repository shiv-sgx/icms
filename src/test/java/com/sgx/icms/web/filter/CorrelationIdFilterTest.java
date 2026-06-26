package com.sgx.icms.web.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;

class CorrelationIdFilterTest {

    private static final String HEADER = "X-Correlation-Id";

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void generatesFreshIdWhenNoInboundHeader() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader(HEADER)).thenReturn(null);

        filter.doFilter(request, response, chain);

        ArgumentCaptor<String> cidCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(eq(HEADER), cidCaptor.capture());
        assertNotNull(cidCaptor.getValue());
        assertEquals(8, cidCaptor.getValue().length());
        verify(chain).doFilter(request, response);
        assertNull(MDC.get("cid"));
    }

    @Test
    void honoursTrustedInboundId() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader(HEADER)).thenReturn("abc-123");

        filter.doFilter(request, response, chain);

        verify(response).setHeader(HEADER, "abc-123");
        verify(chain).doFilter(request, response);
    }

    @Test
    void rejectsEmptyInboundIdAndGeneratesFresh() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader(HEADER)).thenReturn("");

        filter.doFilter(request, response, chain);

        ArgumentCaptor<String> cidCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(eq(HEADER), cidCaptor.capture());
        assertEquals(8, cidCaptor.getValue().length());
    }

    @Test
    void rejectsOverlongInboundIdAndGeneratesFresh() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        String tooLong = "x".repeat(65);
        when(request.getHeader(HEADER)).thenReturn(tooLong);

        filter.doFilter(request, response, chain);

        ArgumentCaptor<String> cidCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(eq(HEADER), cidCaptor.capture());
        assertNotEquals(tooLong, cidCaptor.getValue());
        assertEquals(8, cidCaptor.getValue().length());
    }

    @Test
    void clearsMdcEvenWhenChainThrows() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader(HEADER)).thenReturn("keep-me");
        org.mockito.Mockito.doThrow(new RuntimeException("boom")).when(chain).doFilter(any(), any());

        try {
            filter.doFilter(request, response, chain);
        } catch (RuntimeException expected) {
            // expected - propagated after MDC cleanup
        }

        assertNull(MDC.get("cid"));
    }

    @Test
    void worksWithNonHttpServletTypes() throws Exception {
        ServletRequest request = mock(ServletRequest.class);
        ServletResponse response = mock(ServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        verify(chain, never()).doFilter(null, null);
    }
}
