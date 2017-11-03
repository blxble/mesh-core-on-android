package com.blxble.meshpanel.element;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.os.Handler;
import android.os.Message;

import com.blxble.meshpanel.db.DeviceSupportElement;
import com.blxble.meshpanel.service.MeshDeviceInfo;
import com.blxble.meshpanel.service.MeshElementInfo;
import com.blxble.meshpanel.service.MeshService;
import com.blxble.meshpanel.MeshApplication;
import com.blxble.meshpanel.db.DbManager;
import com.blxble.meshpanel.db.DeviceNodeGroup;
import com.blxble.meshpanel.utils.GrantDialog;
import com.blxble.meshpanel.utils.Utility;


public class DeviceManager extends ActiveDevice{
    private static final String TAG = "DeviceManager";
    private DbManager mDbManager;
    private Context mParent;

    public DeviceManager(Context context) {
        this.mParent = context;
        this.mDbManager = new DbManager();
        this.mDbManager.getDeviceNodeFromDb();
    }

    public List<DeviceNodeGroup> getListGroup() {
        return mDbManager.getDeviceNodeGroupList() ;
    }

    public void setManagerNotify(Handler handler){
        setNotifyHandle(handler);
    }

    public void reset() {
        if(mDbManager.DeleteAll()) {
            notifyRemoveDeviceNode();
        }
        setState(DEVICE_ACTIVE_NEW);
    }

    public void init() {
        MeshApplication.getMeshSvc().setManageHandler(new Handler() {
                                                          @Override
                                                          public void handleMessage(Message msg) {
                                                              handleEventMessage(msg);
                                                          }
                                                      }
        );
    }

    private void handleEventMessage(Message msg) {
        switch (msg.getData().getByte(MeshService.MESH_EVT_MSG_PAR_KEY_EVENT)) {
            case MeshService.MESH_EVENT_ON_NET_CREATED:
                onNetCreated();
                break;
            case MeshService.MESH_EVENT_ON_NEW_PROPOSER:
                onNewProposer(msg.getData().getByteArray(MeshService.MESH_EVT_MSG_PAR_KEY_UUID),
                        msg.getData().getByteArray(MeshService.MESH_EVT_MSG_PAR_KEY_BDADDR),
                        msg.getData().getByteArray(MeshService.MESH_EVT_MSG_PAR_KEY_EXTDATA),
                        msg.getData().getByte(MeshService.MESH_EVT_MSG_PAR_KEY_RSSI));
                break;
            case MeshService.MESH_EVENT_ON_NEW_DEVICE:
                onNewDevice(msg.getData().getShort(MeshService.MESH_EVT_MSG_PAR_KEY_DEVADDR),
                        (MeshDeviceInfo) msg.getData().getParcelable(MeshService.MESH_EVT_MSG_PAR_KEY_DEVINFO),
                        (MeshElementInfo[]) msg.getData().getParcelableArray(MeshService.MESH_EVT_MSG_PAR_KEY_ELTINFO));
                break;
            case MeshService.MESH_EVENT_ON_CONFIG_DONE:
                boolean success = (msg.getData().getByte(MeshService.MESH_EVT_MSG_PAR_KEY_SUCCESS) == 0) ? false : true;
                onConfigDone(msg.getData().getShort(MeshService.MESH_EVT_MSG_PAR_KEY_DEVADDR), success,
                        msg.getData().getByte(MeshService.MESH_EVT_MSG_PAR_KEY_CONFOP));
                break;
            case MeshService.MESH_EVENT_ON_NETKEY_UPDATED:
                onNetKeyUpdated(msg.getData().getShort(MeshService.MESH_EVT_MSG_PAR_KEY_NETKEYIDX));
                break;
            case MeshService.MESH_EVENT_ON_APPKEY_UPDATED:
                onAppKeyUpdated(msg.getData().getShort(MeshService.MESH_EVT_MSG_PAR_KEY_APPKEYIDX));
                break;
        }
    }

    private void onNetCreated() {
    }

    private void onNewProposer(byte[] uuid, byte[] bdAddr, byte[] extData, byte rssi) {
        Log.i(TAG, "onNewProposer bd address = " + Utility.byte2hex(bdAddr, ":") + " State = " + getState());
        if(storeNewProposer(uuid, bdAddr, extData, rssi)){
            if (!isExistDeviceNode(mDbManager)) {
                Log.i(TAG, "onNewProposer: DEVICE_ACTIVE_GRANT");
                GrantDialog grandDialog = new GrantDialog(mParent);
                grandDialog.showConfirm(getUUID(), getBdAddr());
            } else {
                setState(DEVICE_ACTIVE_NEW);
                Log.i(TAG, "onNewProposer: DEVICE_ACTIVE_NEW");
            }
        }
    }

    private void onNewDevice(short address, MeshDeviceInfo devInfo, MeshElementInfo[] eltInfo) {
        Log.i(TAG, "NewDevice, address = " + address +
                ", dev_info = " + devInfo.mCid + "," + devInfo.mPid + "," + devInfo.mVid + ", eltInfo = " + eltInfo.length);
        // init parameter
        storeNewDevice(address, devInfo, eltInfo);
        MeshApplication.getMeshSvc().addDeviceAppkey(getAddress(), getNetKeyIdx(), getAppKeyIdx());
    }

    private void onConfigDone(short address, boolean success, byte confOp) {
        Log.i(TAG, "onConfigDone address=" + address + ", confOp = "+ confOp +", success = " + success);
        if (confOp == MeshService.MESH_CONF_OP_ADD_APPKEY) {
            for (short iElementIdx = DeviceSupportElement.SUPPORT_CUSTOM_ELEMENT_INDEX;
                 iElementIdx < getEltInfo().length; iElementIdx++) {
                MeshElementInfo elt = getEltInfo(iElementIdx);
                if (elt.mMids != null) {
                    for (int iModeIdx = 0; iModeIdx < elt.getMids().length; iModeIdx++){
                        MeshApplication.getMeshSvc().setDeviceModelPublication(
                                getAddress(),   // device address
                                getEltAddr(iElementIdx),   // elt address
                                elt.getMids(iModeIdx),       // mid
                                Utility.getPubAddr(elt.getMids(iModeIdx)),   // publish address
                                getAppKeyIdx(), // app Key idx
                                getTTL()        // ttl
                        );
                    }// end for mode
                }// endif
            }// end for element
        } else if (confOp == MeshService.MESH_CONF_OP_SET_MOD_PUBLICATION) {
            for (short iElementIdx = DeviceSupportElement.SUPPORT_CUSTOM_ELEMENT_INDEX;
                 iElementIdx < getEltInfo().length; iElementIdx++) {
                MeshElementInfo elt = getEltInfo(iElementIdx);
                if (elt.mMids != null) {
                    for (int iModeIdx = 0; iModeIdx < elt.getMids().length; iModeIdx++){
                        MeshApplication.getMeshSvc().addDeviceModelSubscription(
                                getAddress(), // device address
                                getEltAddr(iElementIdx), // elt address
                                elt.getMids(iModeIdx),     // mid
                                Utility.getSubsAddr(elt.getMids(iModeIdx)) // subs address
                        );
                    }// end for mode
                }// endif
            }// end for element
        } else if (confOp == MeshService.MESH_CONF_OP_ADD_MOD_SUBSCRIPTION) {
            for (short iElementIdx = DeviceSupportElement.SUPPORT_CUSTOM_ELEMENT_INDEX;
                 iElementIdx < getEltInfo().length; iElementIdx++) {
                MeshElementInfo elt = getEltInfo(iElementIdx);
                if (elt.mMids != null) {
                    for (int iModeIdx = 0; iModeIdx < elt.getMids().length; iModeIdx++){
                        MeshApplication.getMeshSvc().bindDeviceModelAppkey(
                                getAddress(),  // device address
                                getEltAddr(iElementIdx),  // elt address
                                elt.getMids(iModeIdx),      // mid
                                getAppKeyIdx() // app key idx
                        );
                    }// end for mode
                }// endif
            }// end for element
        } else if (confOp == MeshService.MESH_CONF_OP_BIND_MOD_APPKEY) {
            // notify user new device arrival
            boolean bRet = addNewDeviceNode(mDbManager);
            Log.i(TAG, "onConfigDone address=" + address + ", addNewDeviceNode = "+ bRet + ", State = " + getState());
        }
     }

    private void onNetKeyUpdated(short netKeyIdx) {
        setNetKeyIdx(netKeyIdx);
    }

    private void onAppKeyUpdated(short appKeyIdx) {
        setAppKeyIdx(appKeyIdx);
    }

    public void setNetTimeInfo(byte elementIdx, long netTime) {
        storeElementNetTime(elementIdx, netTime);
        boolean bRet = updateDeviceNodeElementNetTime(mDbManager);
    }

    public void setNetLightInfo(byte elementIdx, byte state) {
        storeElementNetLight(elementIdx, state);
        boolean bRet = updateDeviceNodeElementNetTime(mDbManager);
    }
}
