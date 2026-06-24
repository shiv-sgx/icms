package com.sgx.icms.web.support;

import java.util.List;

/**
 * A page of results plus the metadata views need to render pagination controls.
 * Keeps list screens from loading entire tables into memory.
 */
public class Paged<T> {

    private final List<T> items;
    private final int page;        // 1-based
    private final int size;
    private final long totalItems;

    public Paged(List<T> items, int page, int size, long totalItems) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.totalItems = totalItems;
    }

    public List<T> getItems() { return items; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalItems() { return totalItems; }

    public int getTotalPages() {
        if (size <= 0) {
            return 1;
        }
        return (int) Math.max(1, (totalItems + size - 1) / size);
    }

    public boolean isHasPrev() { return page > 1; }
    public boolean isHasNext() { return page < getTotalPages(); }
    public int getFirstItemIndex() { return totalItems == 0 ? 0 : (page - 1) * size + 1; }
    public long getLastItemIndex() { return Math.min((long) page * size, totalItems); }
}
