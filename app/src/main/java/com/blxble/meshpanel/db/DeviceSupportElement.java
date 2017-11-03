package com.blxble.meshpanel.db;

import android.util.Log;

import com.blxble.meshpanel.element.Model.LightControlModel;
import com.blxble.meshpanel.element.Model.NetTimeModel;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shibin on 9/12/2017.
 */

public class DeviceSupportElement extends DataSupport {
    public static final String TAG = "DeviceSupportElement";
    public static short SUPPORT_SYSTEM_ELEMENT_INDEX = 0;
    public static short SUPPORT_CUSTOM_ELEMENT_INDEX = 1;
    private long id;
    private short elementIdx;
    private short modelId;
    private List<ElementAppItem> elementAppItemList = new ArrayList<ElementAppItem>();

    public DeviceSupportElement() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public short getModelId() {
        return modelId;
    }

    public void setModelId(short modelId) {
        this.modelId = modelId;
    }

    public short getElementIdx() {
        return elementIdx;
    }

    public void setElementIdx(short elementIdx) {
        this.elementIdx = elementIdx;
    }

    public List<ElementAppItem> getElementAppItemList() {
        return elementAppItemList;
    }

    public void setElementAppItemList(List<ElementAppItem> elementAppItemList) {
        this.elementAppItemList = elementAppItemList;
    }

    private boolean addNewElementAppState(short appType) {
        ElementAppItem elementAppItem = new ElementAppItem();
        elementAppItem.setType(appType);
        boolean bSave = elementAppItem.save();
        if (bSave) {
            elementAppItemList.add(elementAppItem);
        } else {
            Log.e(TAG, "addNewElementAppState: " + appType);
        }
        return bSave;
    }

    public boolean addNewAppState() {
        if (modelId == LightControlModel.MESH_NET_LIGHT_MID){
            if(!addNewElementAppState(ElementAppItem.APP_TYPE_POWER)) {
                return false;
            }
            if(!addNewElementAppState(ElementAppItem.APP_TYPE_COLOR)) {
                return false;
            }
        } else if (modelId == NetTimeModel.MESH_NET_TIMER_SERVER_MID) {
            if(!addNewElementAppState(ElementAppItem.APP_TYPE_NET_TIME)){
                return false;
            }
        }
        return true;
    }

    public boolean updateModeState(long state){
        ElementAppItem elementAppItem = findAppItemByType(ElementAppItem.APP_TYPE_NET_TIME);
        if (elementAppItem != null) {
            ElementAppItem appItem = new ElementAppItem();
            appItem.setState(state);
            appItem.update(elementAppItem.getId());
            elementAppItem.setState(state);
            return true;
        }
        return false;
    }

    private ElementAppItem findAppItemByType(short type){
        int iItemCount = elementAppItemList.size();
        for (int iItem = 0; iItem < iItemCount; iItem++){
            ElementAppItem elementAppItem = elementAppItemList.get(iItem);
            if (elementAppItem.getType() == type) {
                return elementAppItem;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "DeviceSupportElement{" +
                "id=" + id +
                ", elementIdx=" + elementIdx +
                ", modelId=" + modelId +
                '}';
    }
}
