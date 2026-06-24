package com.sgx.icms.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sgx.icms.dao.ReportDao;
import com.sgx.icms.db.Db;
import com.sgx.icms.web.support.ReportTable;

/**
 * Builds the manager Reports &amp; Analytics tables. Each report has a stable key so
 * the CSV-export endpoint can request exactly one.
 */
public class ReportService {

    private final ReportDao reportDao = new ReportDao();

    /** All report tables, in display order, keyed by their export key. */
    public Map<String, ReportTable> allReports() {
        Map<String, ReportTable> reports = new LinkedHashMap<>();
        Db.withConnection(conn -> {
            reports.put("claims-volume", new ReportTable("claims-volume", "Claims Volume by Status",
                    Arrays.asList("Status", "Claims", "Estimated (₹)"), rows(reportDao.claimsByStatus(conn))));
            reports.put("claims-type", new ReportTable("claims-type", "Claims by Type",
                    Arrays.asList("Type", "Claims", "Estimated (₹)"), rows(reportDao.claimsByType(conn))));
            reports.put("sla-compliance", new ReportTable("sla-compliance", "SLA Compliance",
                    Arrays.asList("Bucket", "Claims"), rows(reportDao.slaCompliance(conn))));
            reports.put("settlement-tat", new ReportTable("settlement-tat", "Settlement TAT",
                    Arrays.asList("Metric", "Value"), rows(reportDao.settlementTat(conn))));
            reports.put("fraud-detection", new ReportTable("fraud-detection", "Fraud Detection (high risk)",
                    Arrays.asList("Claim No.", "Type", "Risk", "Fraud Score", "Status"), rows(reportDao.fraudWatch(conn))));
            reports.put("agent-performance", new ReportTable("agent-performance", "Agent Performance",
                    Arrays.asList("Agent", "Total", "Settled", "Pending Approval"), rows(reportDao.agentPerformance(conn))));
            return null;
        });
        return reports;
    }

    public ReportTable report(String key) {
        return allReports().get(key);
    }

    private static List<List<String>> rows(List<String[]> raw) {
        List<List<String>> out = new ArrayList<>();
        for (String[] r : raw) {
            out.add(Arrays.asList(r));
        }
        return out;
    }
}
