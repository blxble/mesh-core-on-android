package com.blxble.meshpanel.utils;

import com.blxble.meshpanel.element.Model.LightControlModel;
import com.blxble.meshpanel.element.Model.NetTimeModel;

/**
 * Created by Shibin on 2017/9/29.
 */

public class Utility {
    public static String byte2hex(byte [] buffer, String space){
        String h = "";
        if (buffer == null) return h;

        for(int i = 0; i < buffer.length; i++){
            String temp = Integer.toHexString(buffer[i] & 0xFF);
            if(temp.length() == 1){
                temp = "0" + temp;
            }
            if (h.isEmpty()) {
                h = temp.toUpperCase();
            } else {
                h = h + space + temp.toUpperCase();
            }
        }
        return h;
    }

    public static short getPubAddr(int mid) {
        short pubAddr = 0;
        if (mid == NetTimeModel.MESH_NET_TIMER_SERVER_MID){
            pubAddr = (short)0x7FFF;
        } else if (mid == LightControlModel.MESH_NET_LIGHT_MID) {
            pubAddr = LightControlModel.MESH_NET_LIGHT_SUBSCRIBE_ADDRESS;
        } else if (mid == LightControlModel.MESH_NET_LIGHT_CONTROL_MID) {
            pubAddr = LightControlModel.MESH_NET_LIGHT_PUBLIC_ADDRESS;
        }
        return pubAddr;
    }

    public static short getSubsAddr(int mid) {
        short subsAddr = 0;
        if (mid == NetTimeModel.MESH_NET_TIMER_SERVER_MID){
            subsAddr = (short)5;
        } else if (mid == LightControlModel.MESH_NET_LIGHT_CONTROL_MID) {
            subsAddr = LightControlModel.MESH_NET_LIGHT_SUBSCRIBE_ADDRESS;
        } else if (mid == LightControlModel.MESH_NET_LIGHT_MID) {
            subsAddr = LightControlModel.MESH_NET_LIGHT_PUBLIC_ADDRESS;
        }
        return subsAddr;
    }
}
