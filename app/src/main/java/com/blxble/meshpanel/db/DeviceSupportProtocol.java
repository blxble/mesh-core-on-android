package com.blxble.meshpanel.db;

import android.util.Log;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shibin on 9/12/2017.
 */

public class DeviceSupportProtocol extends DataSupport {
    public static final String TAG = "DeviceSupportProtocol";
    private long id;
    private int elementIdx;
    private int modelId;
    private short publishAddress;
    private List<ElementSubscribeAddress> elementSubscribeAddressList = new ArrayList<ElementSubscribeAddress>();
    private List<ElementBoundAppKey> elementBoundAppKeyList = new ArrayList<ElementBoundAppKey>();

    public DeviceSupportProtocol() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getModelId() {
        return modelId;
    }

    public void setModelId(int modelIdx) {
        this.modelId = modelId;
    }

    public int getElementIdx() {
        return elementIdx;
    }

    public void setElementIdx(int elementIdx) {
        this.elementIdx = elementIdx;
    }

    public short getPublishAddress() {
        return publishAddress;
    }

    public void setPublishAddress(short publishAddress) {
        this.publishAddress = publishAddress;
    }

    public List<ElementSubscribeAddress> getElementSubscribeAddressList() {
        return elementSubscribeAddressList;
    }

    public void setElementSubscribeAddressList(List<ElementSubscribeAddress> elementSubscribeAddressList) {
        this.elementSubscribeAddressList = elementSubscribeAddressList;
    }

    public List<ElementBoundAppKey> getElementBoundAppKeyList() {
        return elementBoundAppKeyList;
    }

    public void setElementBoundAppKeyList(List<ElementBoundAppKey> elementBoundAppKeyList) {
        this.elementBoundAppKeyList = elementBoundAppKeyList;
    }

    public boolean addElementSubscribeAddress(short address) {
        boolean bResult = false;
        ElementSubscribeAddress elementSubscribeAddress = new ElementSubscribeAddress();
        elementSubscribeAddress.setSubscribeAddress(address);
        bResult = elementSubscribeAddress.save();
        if (bResult) {
            elementSubscribeAddressList.add(elementSubscribeAddress);
        } else {
            Log.e(TAG, "addElementBoundAppKey: address = " + address);
        }
        return bResult;
    }

    public boolean addElementBoundAppKey(short keyIdx){
        boolean bResult = false;
        ElementBoundAppKey elementBoundAppKey = new ElementBoundAppKey();
        elementBoundAppKey.setAppKeyIdx(keyIdx);
        bResult = elementBoundAppKey.save();
        if (bResult) {
            elementBoundAppKeyList.add(elementBoundAppKey);
        } else {
            Log.e(TAG, "addElementBoundAppKey: keyIdx = " + keyIdx);
        }
        return bResult;
    }

    @Override
    public String toString() {
        return "DeviceSupportProtocol{" +
                "id=" + id +
                ", elementIdx=" + elementIdx +
                ", modelId=" + modelId +
                ", publishAddress=" + publishAddress +
                '}';
    }
}
