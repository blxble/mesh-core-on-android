package com.blxble.meshpanel.element;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.blxble.meshpanel.db.DeviceSupportElement;
import com.blxble.meshpanel.service.MeshDeviceInfo;
import com.blxble.meshpanel.service.MeshElementInfo;
import com.blxble.meshpanel.db.DbManager;
import com.blxble.meshpanel.db.DeviceNode;
import com.blxble.meshpanel.element.Model.ModelInfo;
import com.blxble.meshpanel.element.Model.LightControlModel;
import com.blxble.meshpanel.element.Model.NetTimeModel;
import com.blxble.meshpanel.utils.Utility;

/**
 * Created by Shibin on 9/18/2017.
 */

public class ActiveDevice extends ModelInfo {
    private static final String TAG = "ActiveDevice";
    public static final byte DEVICE_ACTIVE_NEW = 0;
    public static final byte DEVICE_ACTIVE_GRANT = 1;
    public static final byte DEVICE_ACTIVE_CONFIG = 2;
    public static final byte DEVICE_ACTIVE_HOST = 3;
    public static final byte DEVICE_ACTIVE_DONE = 4;

    public final static byte NOTIFY_DEVICE_NODE_INSERT = 0;
    public final static byte NOTIFY_DEVICE_NODE_REMOVE = 1;
    public final static byte NOTIFY_DEVICE_INFO_CHANGED = 10;
    public final static byte NOTIFY_DEVICE_INFO_CHANGED_STATE = 11;
    public final static byte NOTIFY_DEVICE_INFO_WITHOUT = 127;

    public final static int DEV_INVALID_CHILD = 256;

    private Handler mNotifyHandle;
    private byte mState;
    private byte[] mUUID;
    private byte[] mBdAddr;
    private byte[] mExtData;
    private byte mRSSI;
    private short mAddress;
    private MeshDeviceInfo mDevInfo;
    private MeshElementInfo[] mEltInfo;
    private short mEltAddr;
    private short mAppKeyIdx;
    private short mNetKeyIdx;
    private byte mTTL;

    public ActiveDevice() {
        setState(DEVICE_ACTIVE_NEW);
    }

    public void setNotifyHandle(Handler mNotifyHandle) {
        this.mNotifyHandle = mNotifyHandle;
    }

    public byte getState() {
        return mState;
    }

    public void setState(byte mState) {
        this.mState = mState;
    }

    public byte[] getUUID() {
        return mUUID;
    }

    public byte[] getBdAddr() {
        return mBdAddr;
    }

    public short getAddress() {
        return mAddress;
    }

    public MeshDeviceInfo getDevInfo() {
        return mDevInfo;
    }

    public MeshElementInfo[] getEltInfo() {
        return mEltInfo;
    }

    public MeshElementInfo getEltInfo(int iIdx) {
        return mEltInfo[iIdx];
    }

    public short getEltAddr(short elementIdx) {
        mEltAddr = (short)(mAddress + elementIdx);
        return mEltAddr;
    }

    public short getAppKeyIdx() {
        return mAppKeyIdx;
    }

    public short getNetKeyIdx() {
        return mNetKeyIdx;
    }

    public byte getTTL() {
        return mTTL;
    }

    public void setAppKeyIdx(short mAppKeyIdx) {
        this.mAppKeyIdx = mAppKeyIdx;
    }

    public void setNetKeyIdx(short mNetKeyIdx) {
        this.mNetKeyIdx = mNetKeyIdx;
    }

    public boolean storeNewProposer(byte[] uuid, byte[] bdAddr, byte[] extData, byte rssi) {
        boolean bNotifyAlert = false;
        if (getState() == DEVICE_ACTIVE_NEW) {
            setState(DEVICE_ACTIVE_GRANT);
            this.mUUID = uuid;
            this.mBdAddr = bdAddr;
            this.mExtData = extData;
            this.mRSSI = rssi;
            bNotifyAlert = true;
        }
        return bNotifyAlert;
    }

    public void storeNewDevice(short address, MeshDeviceInfo devInfo, MeshElementInfo[] eltInfo){
        this.mAddress = address;
        this.mDevInfo = devInfo;
        this.mEltInfo = eltInfo;
        this.mNetKeyIdx = 0;
        this.mAppKeyIdx = 0;
        this.mTTL = 5;

        if (getState() == DEVICE_ACTIVE_NEW) {
            setState(DEVICE_ACTIVE_HOST);
        }

		if (eltInfo != null) {
	        for (short iElementIdx = DeviceSupportElement.SUPPORT_CUSTOM_ELEMENT_INDEX;
	             iElementIdx < eltInfo.length; iElementIdx++) {
	            MeshElementInfo elt = eltInfo[iElementIdx];
	            if (elt.mMids != null) {
	                for (short n = 0; n < elt.mMids.length; n++) {
	                    Log.i(TAG, "MID: 0x" + Integer.toHexString(elt.mMids[n]&0xFFFF));
	                }
	            }
	        }
		}
    }

    public boolean addNewDeviceNode(DbManager dbManager){
        boolean bRet = false;
        bRet = dbManager.addNewDeviceNode(mAddress, mDevInfo, mEltInfo, mAppKeyIdx, mNetKeyIdx,
                Utility.byte2hex(mBdAddr, ":"), mState);
        if (bRet) {
            setState(DEVICE_ACTIVE_DONE);
            notifyNewDeviceNode();
        }
        setState(DEVICE_ACTIVE_NEW);
        return bRet;
    }

    public boolean isExistDeviceNode(DbManager dbManager){
        //return (dbManager.findDeviceNodeByBdAddress(Utility.byte2hex(mBdAddr, ":")) != null);
        return false;
    }

    public boolean updateDeviceNodeElementNetTime(DbManager dbManager){
        return dbManager.updateDeviceNodeElementMode(DeviceNode.DEV_PID_TIME_SERVER, getElementIdx(), getNetTime());
    }

    public boolean updateDeviceNodeElementLight(DbManager dbManager){
        return dbManager.updateDeviceNodeElementMode(DeviceNode.DEV_PID_LIGHT, getElementIdx(), getNetTime());
    }

    public void notifyNewDeviceNode() {
        notifyDeviceNode(NOTIFY_DEVICE_NODE_INSERT);
    }

    public void notifyRemoveDeviceNode() {
        notifyDeviceNode(NOTIFY_DEVICE_NODE_REMOVE);
    }

    public void notifyUpdateDeviceNode() {
        notifyDeviceNode(NOTIFY_DEVICE_INFO_CHANGED);
    }

    public void notifyUpdateDeviceNodeState(byte state, short addr) {
        notifyDeviceNode(NOTIFY_DEVICE_INFO_CHANGED_STATE, state, addr);
    }

    private void notifyDeviceNode(byte notify_event){
        notifyDeviceNode(notify_event, NOTIFY_DEVICE_INFO_WITHOUT, NOTIFY_DEVICE_INFO_WITHOUT);
    }

    private void notifyDeviceNode(byte notify_event, byte notify_state, short notify_addr){
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putByte("Event", notify_event);
        bundle.putByte("State", notify_state);
        bundle.putShort("Addr", notify_addr);
        msg.setData(bundle);
        mNotifyHandle.sendMessage(msg);
    }
}
