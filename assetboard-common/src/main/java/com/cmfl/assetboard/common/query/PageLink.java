package com.cmfl.assetboard.common.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageLink {
    private int pageSize = 20;
    private int page = 0;
    private String textSearch;
    private String sortProperty;
    private String sortOrder;

    public PageLink(int pageSize, int page) {
        this.pageSize = pageSize;
        this.page = page;
    }
}
