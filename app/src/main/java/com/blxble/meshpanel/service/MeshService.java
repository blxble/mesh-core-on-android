package com.blxble.meshpanel.service;

import java.util.ArrayList;
import java.util.List;
import java.lang.String;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.app.Service;

import com.blxble.meshpanel.utils.Utility;


public class MeshService extends Service {

    private static final String TAG = "MeshService";
    public MeshServiceBinder mBinder = new MeshServiceBinder(MeshService.this);
    private List<ElementCallbackInfo> mEltCbkInfoList;
    private Handler mEvtHdl;
    private boolean mOnline = false;

    public static final byte MESH_EVENT_ON_NET_CREATED = 0;
    public static final byte MESH_EVENT_ON_NET_JOINT = 1;
    public static final byte MESH_EVENT_ON_NEW_PROPOSER = 2;
    public static final byte MESH_EVENT_ON_NEW_DEVICE = 3;
    public static final byte MESH_EVENT_ON_CONFIG_DONE = 4;
    public static final byte MESH_EVENT_ON_NETKEY_UPDATED = 5;
    public static final byte MESH_EVENT_ON_APPKEY_UPDATED = 6;
	public static final byte MESH_EVENT_ON_PROXY_STATUS_CHANGED = 7;

    public static final String MESH_ACC_MSG_PAR_KEY_OPCODE = "KEY_OPCODE";
    public static final String MESH_ACC_MSG_PAR_KEY_PARAM = "KEY_PARAM";
    public static final String MESH_ACC_MSG_PAR_KEY_SRCADDR = "KEY_SRCADDR";
    public static final String MESH_ACC_MSG_PAR_KEY_APPKEYIDX = "KEY_APPKEYIDX";
    public static final String MESH_ACC_MSG_PAR_KEY_RSSI = "KEY_RSSI";
    public static final String MESH_EVT_MSG_PAR_KEY_EVENT = "KEY_EVENT";
    public static final String MESH_EVT_MSG_PAR_KEY_UUID = "KEY_UUID";
    public static final String MESH_EVT_MSG_PAR_KEY_BDADDR = "KEY_BDADDR";
    public static final String MESH_EVT_MSG_PAR_KEY_PBFLAG = "KEY_PBFLAG";
    public static final String MESH_EVT_MSG_PAR_KEY_EXTDATA = "KEY_EXTDATA";
    public static final String MESH_EVT_MSG_PAR_KEY_RSSI = "KEY_RSSI";
    public static final String MESH_EVT_MSG_PAR_KEY_DEVADDR = "KEY_DEVADDR";
    public static final String MESH_EVT_MSG_PAR_KEY_ELTINFO = "KEY_ELTINFO";
    public static final String MESH_EVT_MSG_PAR_KEY_SUCCESS = "KEY_SUCCESS";
    public static final String MESH_EVT_MSG_PAR_KEY_CONFOP = "KEY_CONFOP";
    public static final String MESH_EVT_MSG_PAR_KEY_NETKEYIDX = "KEY_NETKEYIDX";
    public static final String MESH_EVT_MSG_PAR_KEY_APPKEYIDX = "KEY_APPKEYIDX";
	public static final String MESH_EVT_MSG_PAR_KEY_PROXYSTATUS = "KEY_PROXYSTATUS";

    public static byte MESH_CONF_OP_UNKOWN = 0;
	public static byte MESH_CONF_OP_GET_COMPOSITION_DATA = 1;
    public static byte MESH_CONF_OP_ADD_APPKEY = 2;
    public static byte MESH_CONF_OP_SET_MOD_PUBLICATION = 3;
    public static byte MESH_CONF_OP_ADD_MOD_SUBSCRIPTION = 4;
    public static byte MESH_CONF_OP_BIND_MOD_APPKEY = 5;
	public static byte MESH_CONF_OP_SET_PROXY = 6;

	public static byte MESH_PROXY_STATE_ENABLE = 0;
	public static byte MESH_PROXY_STATE_DISABLE = 1;

	public static byte MESH_PROXY_STATUS_CONNECTED = 0;
	public static byte MESH_PROXY_STATUS_DISCONNECTED = 1;
	public static byte MESH_PROXY_STATUS_TIMEOUT = 2;

	public static byte MESH_PROXY_CLIENT_POLICY_ANY = 0;
	public static byte MESH_PROXY_CLIENT_POLICY_DEDICATED = 1;

    static {
        System.loadLibrary("MeshCore");
    }

    public native void initSysNative();

    public native void createNetworkNative();

    public native void grantNewProposerNative(byte[] uuid, byte[] bdAddr);

    public native void joinNetworkNative();

	public native void backToNetworkNative();

    public native short newAppkeyNative(short netkeyIdx);

	public native void getCompositionDataNative(short devAddr);

    public native void addDeviceAppkeyNative(short devAddr, short netkeyIdx, short appkeyIdx);

    public native void setDeviceModelPublicationNative(short devAddr, short eltAddr, int mid, short pubAddr, short appkeyIdx, byte ttl);

    public native void addDeviceModelSubscriptionNative(short devAddr, short eltAddr, int mid, short subsAddr);

    public native void bindDeviceModelAppkeyNative(short devAddr, short eltAddr, int mid, int appkeyIdx);

	public native void setDeviceProxyStateNative(short devAddr, byte state);

	public native void setProxyClientNative(boolean enable, short netkeyIdx, byte policy, int timeout, byte[] bdAddr);

    public native byte accRegisterElementNative(short[] midArr, int[] vmidArr);

    public native short accGetElementAddressNative(byte eltIdx);

    public native boolean accPublishMessageNative(byte eltIdx, byte modIdx, MeshModelMessageOpcode msgOp, byte[] msgParam);

    public native boolean accRespondMessageNative(byte eltIdx, short dstAddr, int appkeyIdx, MeshModelMessageOpcode msgOp, byte[] msgParam);

    public native boolean accUnicastMessageNative(byte eltIdx, short dstAddr, MeshModelMessageOpcode msgOp, byte[] msgParam);

	public native MeshElementInfo[] dbGetNodeElementInfoNative(short devAddr);

    @Override
    public IBinder onBind(Intent intent) {

        Log.i(TAG, "onBind");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new MeshThread().start();
        mEltCbkInfoList = new ArrayList<ElementCallbackInfo>();
    }

    class MeshThread extends Thread {
        public void run() {
            Log.d("MeshThread", "run");
            initSysNative();
        }
    }

    /*
    class MeshServiceBinder extends Binder {
        public MeshService getService() {
            return MeshService.this;
        }
    }
    */

    private class ElementCallbackInfo {
        byte eltIdx;
        Handler eltHdl;
    }

    public void setManageHandler(Handler hdl) {
        mEvtHdl = hdl;
    }

    public boolean isOnline(){
        return mOnline;
    }

    public void createNetwork() {
        createNetworkNative();
        mOnline = true;
    }

    public void backNetwork() {
		backToNetworkNative();
        mOnline = true;
    }

    public void grantNewProposer(byte[] uuid, byte[] bdAddr) {
        grantNewProposerNative(uuid, bdAddr);
    }

    public void joinNetwork() {
        joinNetworkNative();
    }

	public void setProxyClient(boolean enable, short netkeyIdx, byte policy, int timeout, byte[] bdAddr) {
		setProxyClientNative(enable, netkeyIdx, policy, timeout, bdAddr);
	}

    public short newAppkey(short netkeyIdx) {
        return newAppkeyNative(netkeyIdx);
    }

	public void getCompositionData(short devAddr) {
		getCompositionDataNative(devAddr);
	}

    public void addDeviceAppkey(short devAddr, short netkeyIdx, short appkeyIdx) {
        addDeviceAppkeyNative(devAddr, netkeyIdx, appkeyIdx);
    }

    public void setDeviceModelPublication(short devAddr, short eltAddr, int mid, short pubAddr, short appkeyIdx, byte ttl) {
        setDeviceModelPublicationNative(devAddr, eltAddr, mid, pubAddr, appkeyIdx, ttl);
    }

    public void addDeviceModelSubscription(short devAddr, short eltAddr, int mid, short subsAddr) {
        addDeviceModelSubscriptionNative(devAddr, eltAddr, mid, subsAddr);
    }

    public void bindDeviceModelAppkey(short devAddr, short eltAddr, int mid, int appkeyIdx) {
        bindDeviceModelAppkeyNative(devAddr, eltAddr, mid, appkeyIdx);
    }

	public void setDeviceProxyState(short devAddr, byte state) {
		setDeviceProxyState(devAddr, state);
	}

    public byte registerElement(short[] mids, int[] vmids, Handler eltHdl) {
        ElementCallbackInfo eltCbkInfo = new ElementCallbackInfo();
        byte idx;

        idx = accRegisterElementNative(mids, vmids);
        eltCbkInfo.eltIdx = idx;
        eltCbkInfo.eltHdl = eltHdl;
        mEltCbkInfoList.add(eltCbkInfo);

        return idx;
    }

    public short getElementAddress(byte eltIdx) {
        return accGetElementAddressNative(eltIdx);
    }

    public boolean publishMessage(byte eltIdx, byte modIdx, MeshModelMessageOpcode msgOp, byte[] msgParam) {
        return accPublishMessageNative(eltIdx, modIdx, msgOp, msgParam);
    }

    public boolean respondMessage(byte eltIdx, short dstAddr, int appkeyIdx, MeshModelMessageOpcode msgOp, byte[] msgParam) {
        return accRespondMessageNative(eltIdx, dstAddr, appkeyIdx, msgOp, msgParam);
    }

    public boolean unicastMessage(byte eltIdx, short dstAddr, MeshModelMessageOpcode msgOp, byte[] msgParam) {
        return accUnicastMessageNative(eltIdx, dstAddr, msgOp, msgParam);
    }

	public MeshElementInfo[] getNodeElementInfo(short devAddr) {
		return dbGetNodeElementInfoNative(devAddr);
	}

    public void onNetCreated() {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putByte(MESH_EVT_MSG_PAR_KEY_EVENT, MESH_EVENT_ON_NET_CREATED);
        msg.setData(bundle);
        mEvtHdl.sendMessage(msg);
    }

    public void onNetJoint() {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putByte(MESH_EVT_MSG_PAR_KEY_EVENT, MESH_EVENT_ON_NET_JOINT);
        msg.setData(bundle);
        mEvtHdl.sendMessage(msg);
    }

    public void onNewProposer(byte[] uuid, byte[] bdAddr, byte[] extData, byte rssi) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putByte(MESH_EVT_MSG_PAR_KEY_EVENT, MESH_EVENT_ON_NEW_PROPOSER);
        bundle.putByteArray(MESH_EVT_MSG_PAR_KEY_UUID, uuid);
        bundle.putByteArray(MESH_EVT_MSG_PAR_KEY_BDADDR, bdAddr);
        bundle.putByteArray(MESH_EVT_MSG_PAR_KEY_EXTDATA, extData);
        bundle.putByte(MESH_EVT_MSG_PAR_KEY_RSSI, rssi);
        msg.setData(bundle);
        mEvtHdl.sendMessage(msg);
    }

    public void onNewDevice(short devAddr, byte[] uuid) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putByte(MESH_EVT_MSG_PAR_KEY_EVENT, MESH_EVENT_ON_NEW_DEVICE);
        bundle.putShort(MESH_EVT_MSG_PAR_KEY_DEVADDR, devAddr);
        bundle.putByteArray(MESH_EVT_MSG_PAR_KEY_UUID, uuid);
        msg.setData(bundle);
        mEvtHdl.sendMessage(msg);
    }

    public void onConfigDone(short addr, boolean success, byte confOp) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putByte(MESH_EVT_MSG_PAR_KEY_EVENT, MESH_EVENT_ON_CONFIG_DONE);
        bundle.putShort(MESH_EVT_MSG_PAR_KEY_DEVADDR, addr);
        Byte succ;
        if (success) {
            succ = 1;
        } else {
            succ = 0;
        }
        bundle.putByte(MESH_EVT_MSG_PAR_KEY_SUCCESS, succ);
        bundle.putByte(MESH_EVT_MSG_PAR_KEY_CONFOP, confOp);
        msg.setData(bundle);
        mEvtHdl.sendMessage(msg);
    }

    public void onNetkeyUpdated(short netkeyIdx) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putByte(MESH_EVT_MSG_PAR_KEY_EVENT, MESH_EVENT_ON_NETKEY_UPDATED);
        bundle.putShort(MESH_EVT_MSG_PAR_KEY_NETKEYIDX, netkeyIdx);
        msg.setData(bundle);
        mEvtHdl.sendMessage(msg);
    }

    public void onAppkeyUpdated(short appkeyIdx) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putByte(MESH_EVT_MSG_PAR_KEY_EVENT, MESH_EVENT_ON_APPKEY_UPDATED);
        bundle.putShort(MESH_EVT_MSG_PAR_KEY_APPKEYIDX, appkeyIdx);
        msg.setData(bundle);
        mEvtHdl.sendMessage(msg);
    }

	public void onProxyStatusChanged(byte status) {
		Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putByte(MESH_EVT_MSG_PAR_KEY_EVENT, MESH_EVENT_ON_PROXY_STATUS_CHANGED);
        bundle.putByte(MESH_EVT_MSG_PAR_KEY_PROXYSTATUS, status);
        msg.setData(bundle);
        mEvtHdl.sendMessage(msg);
	}

    public void onAccessMessageIndication(byte eltIdx, MeshModelMessageOpcode msgOp, byte[] msgParam,
                                          short srcAddr, int appkeyIdx, byte rssi) {
        ElementCallbackInfo eltCbkInfo;
        Message msg = new Message();
        Bundle bundle = new Bundle();

        bundle.putParcelable(MESH_ACC_MSG_PAR_KEY_OPCODE, msgOp);
        bundle.putByteArray(MESH_ACC_MSG_PAR_KEY_PARAM, msgParam);
        bundle.putShort(MESH_ACC_MSG_PAR_KEY_SRCADDR, srcAddr);
        bundle.putInt(MESH_ACC_MSG_PAR_KEY_APPKEYIDX, appkeyIdx);
        bundle.putByte(MESH_ACC_MSG_PAR_KEY_RSSI, rssi);

        msg.setData(bundle);

        for (int i = 0; i < mEltCbkInfoList.size(); i++) {
            eltCbkInfo = mEltCbkInfoList.get(i);
            if (eltCbkInfo.eltIdx == eltIdx) {
                eltCbkInfo.eltHdl.sendMessage(msg);
                break;
            }
        }
    }
}
