package com.blxble.meshpanel.db;

import android.util.Log;

import com.blxble.meshpanel.service.MeshDeviceInfo;
import com.blxble.meshpanel.service.MeshElementInfo;

import java.util.ArrayList;
import java.util.List;

import org.litepal.crud.DataSupport;

public class DeviceNodeGroup extends DataSupport{
    public static final String TAG = "DeviceNodeGroup";
    private long id;
	private String groupName;
    private int groupIdx;
    private short manager;
	private List<DeviceNode> deviceNodeList = new ArrayList<DeviceNode>();
	public boolean isGroup;

    public DeviceNodeGroup() {
    }

    public DeviceNodeGroup(List<DeviceNode> deviceNodeList) {
		this.deviceNodeList = deviceNodeList;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

    public int getGroupIdx() {
        return groupIdx;
    }

    public void setGroupIdx(int groupIdx) {
        this.groupIdx = groupIdx;
    }

    public short getManager() {
        return manager;
    }

    public void setManager(short manger) {
        this.manager = manger;
    }

    public List<DeviceNode> getDeviceNodeList() {
		return deviceNodeList;
	}

	public void setDeviceNodeList(List<DeviceNode> deviceNodeList) {
		this.deviceNodeList = deviceNodeList;
	}

	public boolean isGroup() {
		return isGroup;
	}

	public void setGroup(boolean group) {
		isGroup = group;
	}

    public String generateDeviceName(short Pid){
        int iNodeCount = deviceNodeList.size(), iSameNum = 0;
        for (int iNodeIdx = 0; iNodeIdx < iNodeCount; iNodeIdx++){
            DeviceNode deviceNode = deviceNodeList.get(iNodeIdx);
            if (deviceNode.getPid() == Pid)
                iSameNum++;
        }

        String sName;
        if (Pid == DeviceNode.DEV_PID_LIGHT)
            sName = "LIGHT"+(iSameNum+1);
        else if (Pid == DeviceNode.DEV_PID_REMOTE_CONTROL)
            sName = "CTRL"+(iSameNum+1);
        else if (Pid == DeviceNode.DEV_PID_TIME_SERVER)
            sName = "Server"+(iSameNum+1);
        else if (Pid == DeviceNode.DEV_PID_TIME_CLIENT)
            sName = "Client"+(iSameNum+1);
        else
            sName = "unknown";

        return sName;
    }

    public boolean addNewDeviceNode(short address, MeshDeviceInfo devInfo, MeshElementInfo[] eltInfo,
                                    short appKeyIdx, short netKeyIdx, String bdAddr){
        if (findDeviceNodeByAddress(address) == null) {
            DeviceNode deviceNode = new DeviceNode();
            deviceNode.setAddress(address);
            deviceNode.setCid(devInfo.mCid);
            deviceNode.setPid(devInfo.mPid);
            deviceNode.setVid(devInfo.mVid);
            deviceNode.setNetKeyIdx(netKeyIdx);
            deviceNode.setBdAddress(bdAddr);
            deviceNode.setName(generateDeviceName(devInfo.mPid));
            if(!deviceNode.addNewElementInfo(eltInfo, appKeyIdx)){
                return false;
            }
            boolean bSave = deviceNode.save();
            if(bSave) {
                deviceNodeList.add(deviceNode);
            } else {
                Log.e(TAG, "addNewDeviceNode: Save");
            }
            return bSave;
        }
        return false;
    }

    public DeviceNode findDeviceNodeByAddress(short address){
        int iNodeCount = deviceNodeList.size();
        for (int iNodeIdx = 0; iNodeIdx < iNodeCount; iNodeIdx++){
            DeviceNode deviceNode = deviceNodeList.get(iNodeIdx);
            if (deviceNode.getAddress() == address) {
                return deviceNode;
            }
        }
        return null;
    }

    public DeviceNode findDeviceNodeByIndex(short iNodeIdx){
        int iNodeCount = deviceNodeList.size();
        if (iNodeIdx < iNodeCount) {
            DeviceNode deviceNode = deviceNodeList.get(iNodeIdx);
            if (deviceNode != null) {
                return deviceNode;
            }
        }
        return null;
    }

    public DeviceNode findDeviceNodeByBdAddress(String bdAddress){
        int iNodeCount = deviceNodeList.size();
        for (int iNodeIdx = 0; iNodeIdx < iNodeCount; iNodeIdx++){
            DeviceNode deviceNode = deviceNodeList.get(iNodeIdx);
            if ((deviceNode.getBdAddress() != null) &&
                    deviceNode.getBdAddress().equalsIgnoreCase(bdAddress)) {
                return deviceNode;
            }
        }
        return null;
    }

    public DeviceNode findDeviceNodeByPid(short pid){
        int iNodeCount = deviceNodeList.size();
        for (int iNodeIdx = 0; iNodeIdx < iNodeCount; iNodeIdx++){
            DeviceNode deviceNode = deviceNodeList.get(iNodeIdx);
            if (deviceNode.getPid() == pid) {
                return deviceNode;
            }
        }
        return null;
    }

    public boolean showConfigDone(short address){
        DeviceNode deviceNode = findDeviceNodeByAddress(address);
        if (deviceNode != null) {
            deviceNode.toString();
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "DeviceNodeGroup{" +
                "id=" + id +
                ", groupName='" + groupName + '\'' +
                ", groupIdx=" + groupIdx +
                ", manager=" + manager +
                ", isGroup=" + isGroup +
                '}';
    }
}
