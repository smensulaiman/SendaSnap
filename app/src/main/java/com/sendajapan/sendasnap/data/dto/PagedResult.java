package com.sendajapan.sendasnap.data.dto;

import java.util.List;

public class PagedResult<T> {
    private List<T> items;
    private PaginationDto pagination;

    public PagedResult() {
    }

    public PagedResult(List<T> items, PaginationDto pagination) {
        this.items = items;
        this.pagination = pagination;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public PaginationDto getPagination() {
        return pagination;
    }

    public void setPagination(PaginationDto pagination) {
        this.pagination = pagination;
    }

    public boolean hasMore() {
        return pagination != null && 
               pagination.getCurrentPage() != null && 
               pagination.getLastPage() != null &&
               pagination.getCurrentPage() < pagination.getLastPage();
    }
}

