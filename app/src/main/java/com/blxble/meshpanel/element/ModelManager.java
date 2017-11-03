package com.blxble.meshpanel.element;

import android.content.Context;

import com.blxble.meshpanel.element.Model.LightControlModel;
import com.blxble.meshpanel.element.Model.NetTimeModel;

/**
 * Created by Shibin on 2017/10/20.
 */

public class ModelManager {
    private static final String TAG = "ModelManager";
    private Context mContext;
    private NetTimeModel mNetTime;
    private LightControlModel mNetLight;

    public ModelManager(Context mContext) {
        this.mContext = mContext;
    }

    public void initModeManager(){
        /*
        mNetTime = new NetTimeModel();
        mNetTime.startServer();
        */

        mNetLight = new LightControlModel();
        mNetLight.startServer();

    }

    public void setModelState(byte state, short addr){
        if (mNetLight != null) {
            mNetLight.turnOnOffLight(state, addr);
        }
    }
}
