package com.blxble.meshpanel.db;

import org.litepal.crud.DataSupport;

import java.io.PipedReader;

/**
 * Created by Shibin on 9/13/2017.
 */

public class ElementBoundAppKey extends DataSupport {
    private long id;
    private short appKeyIdx;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public short getAppKeyIdx() {
        return appKeyIdx;
    }

    public void setAppKeyIdx(short appKeyIdx) {
        this.appKeyIdx = appKeyIdx;
    }

    @Override
    public String toString() {
        return "ElementBoundAppKey{" +
                "id=" + id +
                ", appKeyIdx=" + appKeyIdx +
                '}';
    }
}
