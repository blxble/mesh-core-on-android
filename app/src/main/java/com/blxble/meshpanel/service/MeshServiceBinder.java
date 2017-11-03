package com.blxble.meshpanel.service;

import android.content.Context;
import android.os.Binder;

/**
 * Created by Shibin on 2017/10/26.
 */

public class MeshServiceBinder extends Binder {
    private MeshService mMeshService;

    public MeshServiceBinder(MeshService meshService) {
        this.mMeshService = meshService;
    }

    public MeshService getService() {
        return mMeshService;
    }
}
