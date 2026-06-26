package com.sgx.icms.web.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class ReportTableTest {

    @Test
    void gettersExposeConstructorArgs() {
        List<String> headers = Arrays.asList("Name", "Count");
        List<List<String>> rows = Collections.singletonList(Arrays.asList("X", "1"));
        ReportTable t = new ReportTable("key1", "Title 1", headers, rows);

        assertEquals("key1", t.getKey());
        assertEquals("Title 1", t.getTitle());
        assertEquals(headers, t.getHeaders());
        assertEquals(rows, t.getRows());
    }

    @Test
    void isEmptyTrueWhenRowsIsEmpty() {
        ReportTable t = new ReportTable("k", "t", Collections.singletonList("H"), Collections.emptyList());
        assertTrue(t.isEmpty());
    }

    @Test
    void isEmptyTrueWhenRowsIsNull() {
        ReportTable t = new ReportTable("k", "t", Collections.singletonList("H"), null);
        assertTrue(t.isEmpty());
    }

    @Test
    void isEmptyFalseWhenRowsHasData() {
        ReportTable t = new ReportTable("k", "t", Collections.singletonList("H"),
                Collections.singletonList(Collections.singletonList("v")));
        assertFalse(t.isEmpty());
    }
}
