package com.blxble.meshpanel.db;

import android.icu.util.ULocale;
import android.util.Log;

import com.blxble.meshpanel.service.MeshElementInfo;
import com.blxble.meshpanel.R;
import com.blxble.meshpanel.element.Model.LightControlModel;
import com.blxble.meshpanel.utils.Utility;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class DeviceNode extends DataSupport{

	public static final String TAG = "DeviceNode";

	public static final short DEV_PID_LIGHT = 0;
	public static final short DEV_PID_REMOTE_CONTROL = 1;
    public static final short DEV_PID_TIME_SERVER = 2;
    public static final short DEV_PID_TIME_CLIENT = 3;

	private long id;
    private int resId;
	private String name;
	private short address;
	private short netKeyIdx;
	private short pid; // device type
	private short vid;
	private short cid;
	private String bdAddress;
	private List<DeviceSupportElement> deviceSupportElementList;
	private List<DeviceSupportProtocol> deviceSupportProtocolList;
	private DeviceNodeGroup parent;

	public DeviceNode() {
		deviceSupportElementList = new ArrayList<DeviceSupportElement>();
		deviceSupportProtocolList = new ArrayList<DeviceSupportProtocol>();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNameEx() {
		if(pid == DeviceNode.DEV_PID_LIGHT) {
			this.name = "Light";
		} else if(pid == DeviceNode.DEV_PID_TIME_SERVER) {
			this.name = "Time";
		} else if(pid == DeviceNode.DEV_PID_REMOTE_CONTROL) {
			this.name = "Remote";
		}
	}

	public short getAddress() {
		return address;
	}

	public void setAddress(short address) {
		this.address = address;
	}

	public short getNetKeyIdx() {
		return netKeyIdx;
	}

	public void setNetKeyIdx(short netKeyIdx) {
		this.netKeyIdx = netKeyIdx;
	}

	public short getPid() {
		return pid;
	}

	public void setPid(short pid) {
		this.pid = pid;
        if(pid == DeviceNode.DEV_PID_LIGHT) {
            setResId(R.drawable.role_light);
        } else if(pid == DeviceNode.DEV_PID_TIME_SERVER) {
            setResId(R.drawable.role_time);
        } else if(pid == DeviceNode.DEV_PID_REMOTE_CONTROL) {
            setResId(R.drawable.role_remote);
        }
	}

	public short getCid() {
		return cid;
	}

	public void setCid(short cid) {
		this.cid = cid;
	}

	public short getVid() {
		return vid;
	}

	public void setVid(short vid) {
		this.vid = vid;
	}

	public String getDevInfo() {
		return "PID: "+ pid + ", CID: " + cid + ", VID: " + vid;
	}

	public String getBdAddress() {
		return bdAddress;
	}

	public void setBdAddress(String bdAddress) {
		this.bdAddress = bdAddress;
	}

	public List<DeviceSupportProtocol> getDeviceSupportProtocolList() {
		return deviceSupportProtocolList;
	}

	public void setDeviceSupportProtocolList(List<DeviceSupportProtocol> deviceSupportProtocolList) {
		this.deviceSupportProtocolList = deviceSupportProtocolList;
	}

	public List<DeviceSupportElement> getDeviceSupportElementList() {
		return deviceSupportElementList;
	}

	public void setDeviceSupportElementList(List<DeviceSupportElement> deviceSupportElementList) {
		this.deviceSupportElementList = deviceSupportElementList;
	}

	public DeviceNodeGroup getParent() {
		return parent;
	}

	public void setParent(DeviceNodeGroup parent) {
		this.parent = parent;
	}

	private boolean addNewElementInfo(short elementIdx, int iMid, short pubAddr, short appKeyIdx, short subsAddr) {
		boolean bResult = false;
		DeviceSupportProtocol deviceSupportProtocol = new DeviceSupportProtocol();
		deviceSupportProtocol.setModelId(elementIdx);
        deviceSupportProtocol.setModelId(iMid);
		deviceSupportProtocol.setPublishAddress(pubAddr);
		if (!deviceSupportProtocol.addElementBoundAppKey(appKeyIdx)) {
			return false;
		}
		if (!deviceSupportProtocol.addElementSubscribeAddress(subsAddr)) {
			return false;
		}
		bResult = deviceSupportProtocol.save();
		if (bResult) {
			deviceSupportProtocolList.add(deviceSupportProtocol);
		} else {
			Log.e(TAG, "addNewElementInfo: elementIdx = "+ elementIdx + ", MID = " + iMid);
		}
		return bResult;
    }

	public boolean addNewElementItem(short elementIdx, short iMid) {
		boolean bResult = false;
        DeviceSupportElement deviceSupportElement = new DeviceSupportElement();
		deviceSupportElement.setElementIdx(elementIdx);
        deviceSupportElement.setModelId(iMid);
		bResult = deviceSupportElement.addNewAppState();
        if(!bResult) {
			return bResult;
		}
		bResult = deviceSupportElement.save();
		if (bResult) {
			deviceSupportElementList.add(deviceSupportElement);
		} else {
			Log.e(TAG, "addNewElementItem: elementIdx = "+ elementIdx + ", MID = " + iMid);
		}
		return bResult;
    }

	public boolean addNewElementInfo(MeshElementInfo[] eltInfo, short appKeyIdx){
		short subsAddr = 0, pubAddr = 0;
		for (short iElementIdx = DeviceSupportElement.SUPPORT_CUSTOM_ELEMENT_INDEX;
			 iElementIdx < eltInfo.length; iElementIdx++) {
			MeshElementInfo elt = eltInfo[iElementIdx];
			if (elt.mMids != null) {
				for (short n = 0; n < elt.mMids.length; n++) {
					Log.i(TAG, "MID: " + elt.mMids[n]);
					subsAddr = Utility.getSubsAddr(elt.mMids[n]);
					pubAddr = Utility.getPubAddr(elt.mMids[n]);

					if(!addNewElementInfo(iElementIdx, elt.mMids[n], pubAddr, appKeyIdx, subsAddr)) {
						return false;
					}
                    if(!addNewElementItem(iElementIdx, elt.mMids[n])) {
						return false;
					}
				}
			}
			if (elt.mVmids != null) {
				for (short n = 0; n < elt.mVmids.length; n++) {
					Log.i(TAG, "VMID: " + elt.mVmids[n]);
					subsAddr = Utility.getSubsAddr(elt.mMids[n]);
					pubAddr = Utility.getPubAddr(elt.mMids[n]);

					if(!addNewElementInfo(iElementIdx, elt.mVmids[n], pubAddr, appKeyIdx, subsAddr)){
						return false;
					}
				}
			}
		}
		return true;
	}

	public boolean updateElementModeInfo(byte elementIdx, long state){
		DeviceSupportElement deviceSupportElement = findSupportElementByElementIndex(elementIdx);
		if (deviceSupportElement != null) {
			return deviceSupportElement.updateModeState(state);
		}
		return false;
	}

	public DeviceSupportElement findSupportElementByElementIndex(byte modeId){
		int iElementCount = deviceSupportElementList.size();
		for (int iElement = 0; iElement < iElementCount; iElement++) {
			DeviceSupportElement deviceSupportElement = deviceSupportElementList.get(iElement);
			if (deviceSupportElement.getModelId() == modeId) {
				return deviceSupportElement;
			}
		}
		return null;
	}

	public void showConfigDone(short address) {

    }

	@Override
	public String toString() {
		return "DeviceNode{" +
				"id=" + id +
				", name='" + name + '\'' +
				", address=" + address +
				", netKeyIdx=" + netKeyIdx +
				", pid=" + pid +
				", vid=" + vid +
				", cid=" + cid +
				'}';
	}
}
