package com.cmfl.assetboard.common.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageData<T> {
    private List<T> data;
    private int totalPages;
    private long totalElements;
    private boolean hasNext;

    public static <T> PageData<T> empty() {
        return new PageData<>(Collections.emptyList(), 0, 0, false);
    }
}
