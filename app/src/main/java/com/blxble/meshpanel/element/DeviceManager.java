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
	private static byte mEltIndex = 0;

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
                        (byte[]) msg.getData().getByteArray(MeshService.MESH_EVT_MSG_PAR_KEY_UUID));
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
			case MeshService.MESH_EVENT_ON_PROXY_STATUS_CHANGED:
				onProxyStatusChanged(msg.getData().getByte(MeshService.MESH_EVT_MSG_PAR_KEY_PROXYSTATUS));
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

    private void onNewDevice(short address, byte[] uuid) {
        Log.i(TAG, "onNewDevice, address = " + address);

		storeNewDevice(address, null, null);

		if (getBdAddr() != null) {
			MeshApplication.getMeshSvc().setProxyClient(true, (short)0, MeshService.MESH_PROXY_CLIENT_POLICY_DEDICATED, 500, getBdAddr());
		}

		MeshApplication.getMeshSvc().getCompositionData(getAddress());
    }

	private void onProxyStatusChanged(byte status) {
		Log.i(TAG, "onProxyStatusChanged, status="+status);
		if (status == MeshService.MESH_PROXY_STATUS_CONNECTED) {
		} else if (status == MeshService.MESH_PROXY_STATUS_DISCONNECTED) {
			//MeshApplication.getMeshSvc().setProxyClient(true, (short)0, MeshService.MESH_PROXY_CLIENT_POLICY_ANY, 0, null);
		} else if (status == MeshService.MESH_PROXY_STATUS_TIMEOUT) {
			MeshApplication.getMeshSvc().setProxyClient(true, (short)0, MeshService.MESH_PROXY_CLIENT_POLICY_DEDICATED, 500, getBdAddr());
		}
	}

    private void onConfigDone(short address, boolean success, byte confOp) {
        Log.i(TAG, "onConfigDone address=" + address + ", confOp = "+ confOp +", success = " + success);

		
		if (confOp == MeshService.MESH_CONF_OP_GET_COMPOSITION_DATA) {
			MeshElementInfo[] eltInfo;
			eltInfo = MeshApplication.getMeshSvc().getNodeElementInfo(address);
			MeshDeviceInfo devInfo = new MeshDeviceInfo();
			devInfo.mCid = 0;
			devInfo.mPid = 0;
			devInfo.mVid = 0;
			storeNewDevice(address, devInfo, eltInfo);

			MeshApplication.getMeshSvc().addDeviceAppkey(getAddress(), getNetKeyIdx(), getAppKeyIdx());

			mEltIndex = DeviceSupportElement.SUPPORT_CUSTOM_ELEMENT_INDEX;
		} else if (confOp == MeshService.MESH_CONF_OP_ADD_APPKEY) {
            MeshElementInfo elt = getEltInfo(mEltIndex);
            if (elt.mMids != null) {
                MeshApplication.getMeshSvc().setDeviceModelPublication(
                        getAddress(),   // device address
                        getEltAddr(mEltIndex),   // elt address
                        elt.getMids(0),       // mid
                        Utility.getPubAddr(elt.getMids(0)),   // publish address
                        getAppKeyIdx(), // app Key idx
                        getTTL()        // ttl
                );
            }// endif
        } else if (confOp == MeshService.MESH_CONF_OP_SET_MOD_PUBLICATION) {
                MeshElementInfo elt = getEltInfo(mEltIndex);
                if (elt.mMids != null) {
                    MeshApplication.getMeshSvc().addDeviceModelSubscription(
                            getAddress(), // device address
                            getEltAddr(mEltIndex), // elt address
                            elt.getMids(0),     // mid
                            Utility.getSubsAddr(elt.getMids(0)) // subs address
                    );
                }// endif
        } else if (confOp == MeshService.MESH_CONF_OP_ADD_MOD_SUBSCRIPTION) {
            MeshElementInfo elt = getEltInfo(mEltIndex);
            if (elt.mMids != null) {
                MeshApplication.getMeshSvc().bindDeviceModelAppkey(
                        getAddress(),  // device address
                        getEltAddr(mEltIndex),  // elt address
                        elt.getMids(0),      // mid
                        getAppKeyIdx() // app key idx
                );
            }// endif
        } else if (confOp == MeshService.MESH_CONF_OP_BIND_MOD_APPKEY) {
			mEltIndex++;
			if (mEltIndex >= getEltInfo().length) {
	            // notify user new device arrival
	            boolean bRet = addNewDeviceNode(mDbManager);
	            Log.i(TAG, "onConfigDone address=" + address + ", addNewDeviceNode = "+ bRet + ", State = " + getState());
			} else {
				MeshElementInfo elt = getEltInfo(mEltIndex);
	            if (elt.mMids != null) {
	                MeshApplication.getMeshSvc().setDeviceModelPublication(
	                        getAddress(),   // device address
	                        getEltAddr(mEltIndex),   // elt address
	                        elt.getMids(0),       // mid
	                        Utility.getPubAddr(elt.getMids(0)),   // publish address
	                        getAppKeyIdx(), // app Key idx
	                        getTTL()        // ttl
	                );
	            }// endif
			}
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
