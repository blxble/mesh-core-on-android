package com.blxble.meshpanel.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Shibin on 9/14/2017.
 */

public class ElementAppItem extends DataSupport {

    public final static short APP_TYPE_POWER = 0;
    public final static short APP_TYPE_COLOR = 2;
    public final static short APP_TYPE_NET_TIME = 3;
    public final static short APP_TYPE_NAME = 100;
    public final static short APP_TYPE_DENOTE = 101;

    public final static byte LIGHT_POWER_OFF = 0;
    public final static byte LIGHT_POWER_ON = 1;

    private long id;
    private short type;
    private long state;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }

    public long getState() {
        return state;
    }

    public void setState(long state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "ElementAppItem{" +
                "id=" + id +
                ", type=" + type +
                ", state=" + state +
                '}';
    }
}
