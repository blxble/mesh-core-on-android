package com.blxble.meshpanel.db;

import android.database.Cursor;
import android.util.Log;

import com.blxble.meshpanel.service.MeshDeviceInfo;
import com.blxble.meshpanel.service.MeshElementInfo;
import com.blxble.meshpanel.element.ActiveDevice;
import com.bumptech.glide.util.LogTime;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shibin on 9/13/2017.
 */

public class DbManager {

    private static final String TAG = "DbManager";
    public static final int DEVICE_NODE_DEFAULT_POSITION = 0;
    public static final short DEVICE_NODE_DEFAULT_GROUP_INDEX = -1;
    public static final String DEVICE_NODE_DEFAULT_GROUP_NAME = "UnGrouped";
    private List<DeviceNodeGroup> deviceNodeGroupList;

    public DbManager() {
        deviceNodeGroupList = new ArrayList<DeviceNodeGroup>();
    }

    public boolean DeleteAll(){
        if (!deviceNodeGroupList.isEmpty()) {
            Class classList [] = {DeviceNodeGroup.class, DeviceNode.class, DeviceSupportElement.class, DeviceSupportProtocol.class,
                    ElementAppItem.class, ElementSubscribeAddress.class, ElementBoundAppKey.class};
            for (int iIdx = 0; iIdx < classList.length; iIdx++) {
                DataSupport.deleteAll(classList[iIdx]);
            }
            return true;
        }
        return false;
    }

    public List<DeviceNodeGroup> getDeviceNodeGroupList() {
        return deviceNodeGroupList;
    }

    public void getDeviceNodeFromDb() {
        deviceNodeGroupList = DataSupport.findAll(DeviceNodeGroup.class);
        Cursor cursor = null;
        for (int iGroupIdx = 0; iGroupIdx < deviceNodeGroupList.size(); iGroupIdx++) {
            List<DeviceNode> deviceNodeList = DataSupport.where("DeviceNodeGroup_id = ?", deviceNodeGroupList.get(iGroupIdx).getId() + "").find(DeviceNode.class);
            for (int iNodeIdx = 0; iNodeIdx < deviceNodeList.size(); iNodeIdx++) {
                // App layer
                List<DeviceSupportElement> deviceSupportElementList = DataSupport.where("DeviceNode_id = ?", deviceNodeList.get(iNodeIdx).getId() + "").find(DeviceSupportElement.class);

                for (int iAppIdx = 0; iAppIdx < deviceSupportElementList.size(); iAppIdx++) {
                    List<ElementAppItem> elementAppItemList = DataSupport.where("DeviceSupportElement_id = ?", deviceSupportElementList.get(iAppIdx).getId() + "").find(ElementAppItem.class);
                    deviceSupportElementList.get(iAppIdx).setElementAppItemList(elementAppItemList);
                }

                deviceNodeList.get(iNodeIdx).setDeviceSupportElementList(deviceSupportElementList);

                // protocol layer
                List<DeviceSupportProtocol> deviceSupportProtocolList = DataSupport.where("DeviceNode_id = ?", deviceNodeList.get(iNodeIdx).getId() + "").find(DeviceSupportProtocol.class);

                for (int iProtocolIdx = 0; iProtocolIdx < deviceSupportProtocolList.size(); iProtocolIdx++) {
                    List<ElementSubscribeAddress> elementSubscribeAddressList = DataSupport.where("DeviceSupportProtocol_id = ?", deviceSupportProtocolList.get(iProtocolIdx).getId() + "").find(ElementSubscribeAddress.class);
                    deviceSupportProtocolList.get(iProtocolIdx).setElementSubscribeAddressList(elementSubscribeAddressList);

                    List<ElementBoundAppKey> elementBoundAppKeyList = DataSupport.where("DeviceSupportProtocol_id = ?", deviceSupportProtocolList.get(iProtocolIdx).getId() + "").find(ElementBoundAppKey.class);
                    deviceSupportProtocolList.get(iProtocolIdx).setElementBoundAppKeyList(elementBoundAppKeyList);
                }

                deviceNodeList.get(iNodeIdx).setDeviceSupportProtocolList(deviceSupportProtocolList);
            }
            deviceNodeGroupList.get(iGroupIdx).setDeviceNodeList(deviceNodeList);
        }
    }

    private int getDeviceNodeGroupSize() {
        return deviceNodeGroupList.size();
    }

    public DeviceNodeGroup createNewGroup() {
        DeviceNodeGroup deviceNodeGroup = new DeviceNodeGroup();
        deviceNodeGroup.setGroupIdx(DEVICE_NODE_DEFAULT_GROUP_INDEX);
        deviceNodeGroup.setGroupName(DEVICE_NODE_DEFAULT_GROUP_NAME);
        return deviceNodeGroup;
    }

    public DeviceNodeGroup createNewGroup(short address) {
        return (findDeviceNodeByAddress(address) == null) ? createNewGroup() : null;
    }

    public DeviceNodeGroup findDefaultGroup(boolean bExistIf) {
        int deviceNodeGroupSize = getDeviceNodeGroupSize();
        for (int iGroupIdx = 0; iGroupIdx < deviceNodeGroupSize; iGroupIdx++) {
            DeviceNodeGroup deviceNodeGroup = deviceNodeGroupList.get(iGroupIdx);
            if (deviceNodeGroup.getGroupName() != null && deviceNodeGroup.getGroupName().equals(DEVICE_NODE_DEFAULT_GROUP_NAME)) {
                return deviceNodeGroup;
            }
        }
        return bExistIf ? null : createNewGroup();
    }



    public boolean addNewDeviceNode(short address, MeshDeviceInfo devInfo, MeshElementInfo[] eltInfo,
                                    short appKeyIdx, short netKeyIdx, String bdAddr, byte state) {
        DeviceNodeGroup deviceNodeGroup = createNewGroup(address);
        if (deviceNodeGroup != null) {
            if (state == ActiveDevice.DEVICE_ACTIVE_HOST) {
                deviceNodeGroup.setManager(address);
            }
            if (deviceNodeGroup.addNewDeviceNode(address, devInfo, eltInfo, appKeyIdx, netKeyIdx, bdAddr)) {
                boolean bSave = deviceNodeGroup.save();
                if(bSave) {
                    deviceNodeGroupList.add(0, deviceNodeGroup);
                } else {
                    Log.e(TAG, "addNewDeviceNode: Save");
                }
                return bSave;
            }
        }
        return false;
    }

    public boolean updateDeviceNodeElementMode(short pid, byte elementIdx, long state){
        DeviceNode deviceNode = findDeviceNodeByPid(pid);
        if (deviceNode != null) {
            return deviceNode.updateElementModeInfo(elementIdx, state);
        }
        return false;
    }

    public void updateAppKeyIndex(short address, short netKeyIdx) {

    }

    public DeviceNode findDeviceNodeByAddress(short address) {
        int iGroupCount = deviceNodeGroupList.size();
        for (int iGroupIdx = 0; iGroupIdx < iGroupCount; iGroupIdx++) {
            DeviceNode deviceNode = deviceNodeGroupList.get(iGroupIdx).findDeviceNodeByAddress(address);
            if (deviceNode != null) {
                Log.i(TAG, "findDeviceNodeByAddress: " + address + ", groupIdx = " + iGroupIdx);
                return deviceNode;
            }
        }
        return null;
    }

    public DeviceNode findDeviceNodeByBdAddress(String bdAddress) {
        int iGroupCount = deviceNodeGroupList.size();
        for (int iGroupIdx = 0; iGroupIdx < iGroupCount; iGroupIdx++) {
            DeviceNode deviceNode = deviceNodeGroupList.get(iGroupIdx).findDeviceNodeByBdAddress(bdAddress);
            if (deviceNode != null) {
                return deviceNode;
            }
        }
        return null;
    }

    public DeviceNode findDeviceNodeByPid(short pid) {
        int iGroupCount = deviceNodeGroupList.size();
        for (int iGroupIdx = 0; iGroupIdx < iGroupCount; iGroupIdx++) {
            DeviceNode deviceNode = deviceNodeGroupList.get(iGroupIdx).findDeviceNodeByPid(pid);
            if (deviceNode != null) {
                return deviceNode;
            }
        }
        return null;
    }

    public void showConfigDone(short address, boolean success, byte confOp) {
        DeviceNodeGroup deviceNodeGroup = findDefaultGroup(true);
        if ((deviceNodeGroup != null) && deviceNodeGroup.showConfigDone(address)) {
            Log.i(TAG, "configDoneNotice address=" + address + ", success=" + success);
        }
    }
}
