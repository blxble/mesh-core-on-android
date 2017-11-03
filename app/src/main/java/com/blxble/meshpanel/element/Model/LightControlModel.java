package com.blxble.meshpanel.element.Model;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.blxble.meshpanel.MeshApplication;
import com.blxble.meshpanel.service.MeshModelMessageOpcode;
import com.blxble.meshpanel.service.MeshService;

public class LightControlModel {
	private byte mElementIdx;
	public static short MESH_NET_LIGHT_MID = (short)0x1000;
	public static short MESH_NET_LIGHT_CONTROL_MID = (short)0x1001;
	public static short MESH_NET_LIGHT_PUBLIC_ADDRESS = (short)0x8000;
	public static short MESH_NET_LIGHT_SUBSCRIBE_ADDRESS = (short)0x8001;
	public static MeshModelMessageOpcode mNetLightGetOp;
	public static MeshModelMessageOpcode mNetLightStatusOp;

	public LightControlModel() {
		mNetLightGetOp = new MeshModelMessageOpcode();

		mNetLightGetOp.mOp0 = (byte)0x82;
		mNetLightGetOp.mOp1 = (byte)0x02;
		mNetLightGetOp.mOp2 = (byte)0x00;

		mNetLightStatusOp = new MeshModelMessageOpcode();
		mNetLightStatusOp.mOp0 = (byte)0x82;
		mNetLightStatusOp.mOp1 = (byte)0x04;
		mNetLightStatusOp.mOp2 = (byte)0x00;
	}

	public void startServer() {
		short[] mids = {MESH_NET_LIGHT_CONTROL_MID};

		mElementIdx = MeshApplication.getMeshSvc().registerElement(mids, null, new Handler() {
				@Override
				public void handleMessage(Message msg) {
					onAccessMessageIndication((MeshModelMessageOpcode)msg.getData().getParcelable(MeshService.MESH_ACC_MSG_PAR_KEY_OPCODE),
											  msg.getData().getByteArray(MeshService.MESH_ACC_MSG_PAR_KEY_PARAM),
											  msg.getData().getShort(MeshService.MESH_ACC_MSG_PAR_KEY_SRCADDR),
											  msg.getData().getInt(MeshService.MESH_ACC_MSG_PAR_KEY_APPKEYIDX),
											  msg.getData().getByte(MeshService.MESH_ACC_MSG_PAR_KEY_RSSI));
				}
			}
		);

		Log.i("LightControlModel", "startServer, mElementIdx="+mElementIdx);
	}

	private void onAccessMessageIndication(MeshModelMessageOpcode msgOp, byte[] msgParam, 
																short srcAddr, int appkeyIdx, byte rssi) {
		if (msgOp == mNetLightStatusOp) {
			unpackNetLightStatus(msgParam);
		}
	}

	private void unpackNetLightStatus(byte[] param) {
		byte presentState = param[0];
		byte targetState = param[1];
		byte remainingTime = param[2];

		MeshApplication.getDeviceManager().setNetLightInfo(mElementIdx, presentState);
	}

	private byte[] packNetLightGet(byte state, byte tid) {
		byte[] msgParam = new byte[4];
		msgParam[0] = state;
		msgParam[1] = tid;
		msgParam[2] = 0;
		msgParam[3] = 0;
		return msgParam;
	}

	public void turnOnOffLight(byte state, short eltAddr) {
		byte[] param = packNetLightGet(state, (byte)0);
		MeshApplication.getMeshSvc().publishMessage(mElementIdx, (byte)0, mNetLightGetOp, param);
	}
}
