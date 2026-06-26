package com.sgx.icms.web.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class CsvWriterTest {

    @Test
    void plainValuesAreNotQuoted() {
        ReportTable t = new ReportTable("k", "title",
                Arrays.asList("Name", "Amount"),
                Collections.singletonList(Arrays.asList("John", "100")));

        assertEquals("Name,Amount\r\nJohn,100\r\n", CsvWriter.toCsv(t));
    }

    @Test
    void valuesContainingCommaAreQuoted() {
        ReportTable t = new ReportTable("k", "title",
                Collections.singletonList("Address"),
                Collections.singletonList(Collections.singletonList("123 Main St, Apt 4")));

        assertEquals("Address\r\n\"123 Main St, Apt 4\"\r\n", CsvWriter.toCsv(t));
    }

    @Test
    void embeddedQuotesAreDoubledAndFieldIsQuoted() {
        ReportTable t = new ReportTable("k", "title",
                Collections.singletonList("Note"),
                Collections.singletonList(Collections.singletonList("He said \"hi\"")));

        assertEquals("Note\r\n\"He said \"\"hi\"\"\"\r\n", CsvWriter.toCsv(t));
    }

    @Test
    void newlinesTriggerQuoting() {
        ReportTable t = new ReportTable("k", "title",
                Collections.singletonList("Note"),
                Collections.singletonList(Collections.singletonList("line1\nline2")));

        assertEquals("Note\r\n\"line1\nline2\"\r\n", CsvWriter.toCsv(t));
    }

    @Test
    void carriageReturnTriggersQuoting() {
        ReportTable t = new ReportTable("k", "title",
                Collections.singletonList("Note"),
                Collections.singletonList(Collections.singletonList("line1\rline2")));

        assertEquals("Note\r\n\"line1\rline2\"\r\n", CsvWriter.toCsv(t));
    }

    @Test
    void nullValueIsRenderedAsEmptyString() {
        ReportTable t = new ReportTable("k", "title",
                Collections.singletonList("Note"),
                Collections.singletonList(Collections.singletonList((String) null)));

        assertEquals("Note\r\n\r\n", CsvWriter.toCsv(t));
    }

    @Test
    void multipleRowsAreEachOnTheirOwnLine() {
        ReportTable t = new ReportTable("k", "title",
                Collections.singletonList("Name"),
                Arrays.asList(Collections.singletonList("A"), Collections.singletonList("B")));

        assertEquals("Name\r\nA\r\nB\r\n", CsvWriter.toCsv(t));
    }
}
