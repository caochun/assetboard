package com.cmfl.assetboard.common.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TsKvQuery {
    private String key;
    private long startTs;
    private long endTs;
    private int limit = 1000;

    public TsKvQuery(String key, long startTs, long endTs) {
        this.key = key;
        this.startTs = startTs;
        this.endTs = endTs;
    }
}
