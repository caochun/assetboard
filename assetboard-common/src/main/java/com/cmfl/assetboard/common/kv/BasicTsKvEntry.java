package com.cmfl.assetboard.common.kv;

import lombok.Getter;

@Getter
public class BasicTsKvEntry implements TsKvEntry {
    private final long ts;
    private final KvEntry kv;

    public BasicTsKvEntry(long ts, KvEntry kv) {
        this.ts = ts;
        this.kv = kv;
    }

    @Override
    public String getKey() {
        return kv.getKey();
    }

    @Override
    public DataType getDataType() {
        return kv.getDataType();
    }

    @Override
    public Object getValue() {
        return kv.getValue();
    }

    @Override
    public String getValueAsString() {
        return kv.getValueAsString();
    }
}
