package com.blxble.meshpanel.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Shibin on 9/13/2017.
 */

public class ElementSubscribeAddress extends DataSupport {
    private long id;
    private short subscribeAddress;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public short getSubscribeAddress() {
        return subscribeAddress;
    }

    public void setSubscribeAddress(short subscribeAddress) {
        this.subscribeAddress = subscribeAddress;
    }

    @Override
    public String toString() {
        return "ElementSubscribeAddress{" +
                "id=" + id +
                ", subscribeAddress=" + subscribeAddress +
                '}';
    }
}
