package com.sgx.icms.web.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class PagedTest {

    @Test
    void basicFieldAccessors() {
        List<String> items = Collections.singletonList("a");
        Paged<String> p = new Paged<>(items, 2, 10, 25);

        assertEquals(items, p.getItems());
        assertEquals(2, p.getPage());
        assertEquals(10, p.getSize());
        assertEquals(25L, p.getTotalItems());
    }

    @Test
    void totalPagesRoundsUp() {
        Paged<String> p = new Paged<>(Collections.emptyList(), 1, 10, 25);
        assertEquals(3, p.getTotalPages());
    }

    @Test
    void totalPagesIsOneWhenSizeIsZeroOrNegative() {
        Paged<String> zero = new Paged<>(Collections.emptyList(), 1, 0, 100);
        assertEquals(1, zero.getTotalPages());

        Paged<String> negative = new Paged<>(Collections.emptyList(), 1, -5, 100);
        assertEquals(1, negative.getTotalPages());
    }

    @Test
    void totalPagesIsAtLeastOneWhenNoItems() {
        Paged<String> p = new Paged<>(Collections.emptyList(), 1, 10, 0);
        assertEquals(1, p.getTotalPages());
    }

    @Test
    void hasPrevFalseOnFirstPage() {
        Paged<String> p = new Paged<>(Collections.emptyList(), 1, 10, 25);
        assertFalse(p.isHasPrev());
    }

    @Test
    void hasPrevTrueOnLaterPage() {
        Paged<String> p = new Paged<>(Collections.emptyList(), 2, 10, 25);
        assertTrue(p.isHasPrev());
    }

    @Test
    void hasNextTrueWhenMorePagesRemain() {
        Paged<String> p = new Paged<>(Collections.emptyList(), 1, 10, 25);
        assertTrue(p.isHasNext());
    }

    @Test
    void hasNextFalseOnLastPage() {
        Paged<String> p = new Paged<>(Collections.emptyList(), 3, 10, 25);
        assertFalse(p.isHasNext());
    }

    @Test
    void firstItemIndexIsZeroWhenNoItems() {
        Paged<String> p = new Paged<>(Collections.emptyList(), 1, 10, 0);
        assertEquals(0, p.getFirstItemIndex());
    }

    @Test
    void firstItemIndexIsOneBasedOffset() {
        Paged<String> p = new Paged<>(Collections.emptyList(), 3, 10, 100);
        assertEquals(21, p.getFirstItemIndex());
    }

    @Test
    void lastItemIndexClampsToTotalItems() {
        Paged<String> p = new Paged<>(Collections.emptyList(), 3, 10, 25);
        assertEquals(25L, p.getLastItemIndex());
    }

    @Test
    void lastItemIndexIsPageBoundaryWhenNotOnLastPage() {
        Paged<String> p = new Paged<>(Collections.emptyList(), 1, 10, 25);
        assertEquals(10L, p.getLastItemIndex());
    }
}
