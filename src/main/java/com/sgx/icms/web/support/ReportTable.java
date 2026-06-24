package com.sgx.icms.web.support;

import java.util.List;

/** A simple titled, tabular report (headers + string rows) — render or export as CSV. */
public class ReportTable {

    private final String key;
    private final String title;
    private final List<String> headers;
    private final List<List<String>> rows;

    public ReportTable(String key, String title, List<String> headers, List<List<String>> rows) {
        this.key = key;
        this.title = title;
        this.headers = headers;
        this.rows = rows;
    }

    public String getKey() { return key; }
    public String getTitle() { return title; }
    public List<String> getHeaders() { return headers; }
    public List<List<String>> getRows() { return rows; }
    public boolean isEmpty() { return rows == null || rows.isEmpty(); }
}
