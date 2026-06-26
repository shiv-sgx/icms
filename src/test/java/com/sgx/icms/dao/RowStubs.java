package com.sgx.icms.dao;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

/** Test-only helper: builds a mock {@link ResultSet} from a column-name -&gt; typed-value map. */
final class RowStubs {

    private RowStubs() {
    }

    static Map<String, Object> row() {
        return new LinkedHashMap<>();
    }

    static ResultSet stub(Map<String, Object> values) throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        for (Map.Entry<String, Object> e : values.entrySet()) {
            String col = e.getKey();
            Object v = e.getValue();
            if (v instanceof LocalDate) {
                lenient().when(rs.getObject(col, LocalDate.class)).thenReturn((LocalDate) v);
            } else if (v instanceof LocalTime) {
                lenient().when(rs.getObject(col, LocalTime.class)).thenReturn((LocalTime) v);
            } else if (v instanceof LocalDateTime) {
                lenient().when(rs.getObject(col, LocalDateTime.class)).thenReturn((LocalDateTime) v);
            } else if (v instanceof BigDecimal) {
                lenient().when(rs.getBigDecimal(col)).thenReturn((BigDecimal) v);
            } else if (v instanceof Boolean) {
                lenient().when(rs.getBoolean(col)).thenReturn((Boolean) v);
            } else if (v instanceof Long) {
                lenient().when(rs.getLong(col)).thenReturn((Long) v);
            } else if (v instanceof Integer) {
                lenient().when(rs.getInt(col)).thenReturn((Integer) v);
                lenient().when(rs.getLong(col)).thenReturn(((Integer) v).longValue());
            } else {
                lenient().when(rs.getString(col)).thenReturn(v == null ? null : v.toString());
            }
        }
        return rs;
    }
}
