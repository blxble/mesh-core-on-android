package com.blxble.meshpanel;

import org.litepal.LitePal;

import android.app.Application;
import android.content.Context;

import com.blxble.meshpanel.db.DeviceNode;
import com.blxble.meshpanel.element.DeviceManager;
import com.blxble.meshpanel.db.DeviceNodeGroup;
import com.blxble.meshpanel.service.MeshService;

import java.util.List;

public class MeshApplication extends Application{
	private static final String TAG = "MeshApplication";
	public static Context mContext;
	public static MeshService mMeshSvc;
	public static DeviceManager mDevManager;
	public static int mGroupPosition;
	public static int mChildPosition;


	@Override
	public void onCreate() {
		mContext = getApplicationContext();
		super.onCreate();
		LitePal.initialize(this);
	}

	public static Context getContext() {
		return mContext;
	}
	
	public static int getGroupPosition() {
		return mGroupPosition;
	}

	public static int getChildPosition() {
		return mChildPosition;
	}

	public static MeshService getMeshSvc() {
		return mMeshSvc;
	}

	public static void setMeshSvc(MeshService svc) {
		mMeshSvc = svc;
	}

	public static DeviceManager getDeviceManager() {
		return mDevManager;
	}

	public static void setDeviceManager(DeviceManager dm) {
		mDevManager = dm;
	}

	public static List<DeviceNodeGroup> getDeviceDataGroup() {
		return mDevManager.getListGroup();
	}

	public static DeviceNode getDeviceNode(){
		return  getDeviceDataGroup().get(mGroupPosition).getDeviceNodeList().get(getChildPosition());
	}

	public static void setClickPosition(int mainPosition, int subPosition) {
		if (getDeviceDataGroup().size() < mainPosition) {
			mGroupPosition = mainPosition;
			if (getDeviceDataGroup().get(mGroupPosition).getDeviceNodeList().size() >= subPosition){
				subPosition = 0;
			}
			mChildPosition = subPosition;
		}
	}
}
