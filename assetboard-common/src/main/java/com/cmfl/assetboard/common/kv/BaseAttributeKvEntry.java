package com.cmfl.assetboard.common.kv;

import lombok.Getter;

@Getter
public class BaseAttributeKvEntry implements AttributeKvEntry {
    private final long lastUpdateTs;
    private final KvEntry kv;

    public BaseAttributeKvEntry(long lastUpdateTs, KvEntry kv) {
        this.lastUpdateTs = lastUpdateTs;
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
