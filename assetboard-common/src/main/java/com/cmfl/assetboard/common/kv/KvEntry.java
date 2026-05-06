package com.cmfl.assetboard.common.kv;

import java.io.Serializable;

public interface KvEntry extends Serializable {
    String getKey();
    DataType getDataType();
    Object getValue();
    String getValueAsString();
}
