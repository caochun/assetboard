package com.cmfl.assetboard.common.kv;

import lombok.Getter;

@Getter
public class BasicKvEntry implements KvEntry {
    private final String key;
    private final DataType dataType;
    private final Object value;

    private BasicKvEntry(String key, DataType dataType, Object value) {
        this.key = key;
        this.dataType = dataType;
        this.value = value;
    }

    public static BasicKvEntry ofBoolean(String key, boolean value) {
        return new BasicKvEntry(key, DataType.BOOLEAN, value);
    }

    public static BasicKvEntry ofLong(String key, long value) {
        return new BasicKvEntry(key, DataType.LONG, value);
    }

    public static BasicKvEntry ofDouble(String key, double value) {
        return new BasicKvEntry(key, DataType.DOUBLE, value);
    }

    public static BasicKvEntry ofString(String key, String value) {
        return new BasicKvEntry(key, DataType.STRING, value);
    }

    public static BasicKvEntry ofJson(String key, String jsonValue) {
        return new BasicKvEntry(key, DataType.JSON, jsonValue);
    }

    @Override
    public String getValueAsString() {
        return value != null ? value.toString() : "";
    }
}
