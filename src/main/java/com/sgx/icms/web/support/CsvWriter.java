package com.sgx.icms.web.support;

import java.util.List;

/**
 * Minimal RFC-4180 CSV serialisation: quotes fields containing commas, quotes,
 * or newlines and doubles embedded quotes. No external dependency.
 */
public final class CsvWriter {

    private CsvWriter() {
    }

    public static String toCsv(ReportTable table) {
        StringBuilder sb = new StringBuilder();
        line(sb, table.getHeaders());
        for (List<String> row : table.getRows()) {
            line(sb, row);
        }
        return sb.toString();
    }

    private static void line(StringBuilder sb, List<String> fields) {
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escape(fields.get(i)));
        }
        sb.append("\r\n");
    }

    private static String escape(String value) {
        String v = value == null ? "" : value;
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            return '"' + v.replace("\"", "\"\"") + '"';
        }
        return v;
    }
}
