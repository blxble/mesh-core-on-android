package com.blxble.meshpanel.element.Model;

import android.os.Handler;
import android.os.Message;

import com.blxble.meshpanel.service.MeshService;
import com.blxble.meshpanel.service.MeshModelMessageOpcode;
import com.blxble.meshpanel.MeshApplication;

public class NetTimeModel {
	private byte mElementIdx;
	public static short MESH_NET_TIMER_SERVER_MID = (short)0x8006;
	public static MeshModelMessageOpcode mNetTimeGetOp;
	public static MeshModelMessageOpcode mNetTimeStatusOp;

	public NetTimeModel() {
		mNetTimeGetOp = new MeshModelMessageOpcode();
		
		mNetTimeGetOp.mOp0 = (byte)0xA3;
		mNetTimeGetOp.mOp1 = (byte)0x00;
		mNetTimeGetOp.mOp2 = (byte)0x00;

		mNetTimeStatusOp = new MeshModelMessageOpcode();
		mNetTimeStatusOp.mOp0 = (byte)0xA3;
		mNetTimeStatusOp.mOp1 = (byte)0x01;
		mNetTimeStatusOp.mOp2 = (byte)0x00;
	}

	public void startServer() {
		short[] mids = {MESH_NET_TIMER_SERVER_MID};

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
	}

	private void onAccessMessageIndication(MeshModelMessageOpcode msgOp, byte[] msgParam, 
																short srcAddr, int appkeyIdx, byte rssi) {
		if (msgOp == mNetTimeGetOp) {
			long netTime = getLocalTime();
			int peerTime = unpackNetTimeGet(msgParam);
			byte[] param = packNetTimeStatus((int)netTime, peerTime);

			MeshApplication.getMeshSvc().respondMessage(mElementIdx, srcAddr, appkeyIdx, mNetTimeStatusOp, param);
			MeshApplication.getDeviceManager().setNetTimeInfo(mElementIdx, netTime);
		}
	}

	private long getLocalTime() {
		return System.currentTimeMillis();
	}

	private byte[] packNetTimeStatus(int netTime, int peerTime) {
		byte[] msgParam = new byte[8];
		
		msgParam[0] = (byte)(peerTime & 0xFF);
		msgParam[1] = (byte)((peerTime >> 8) & 0xFF);
		msgParam[2] = (byte)((peerTime >> 16) & 0xFF);
		msgParam[3] = (byte)((peerTime >> 24) & 0xFF);

		msgParam[4] = (byte)(netTime & 0xFF);
		msgParam[5] = (byte)((netTime >> 8) & 0xFF);
		msgParam[6] = (byte)((netTime >> 16) & 0xFF);
		msgParam[7] = (byte)((netTime >> 24) & 0xFF);

		return msgParam;
	}

	private int unpackNetTimeGet(byte[] param) {
		int time;

		time = param[0] | (((int)param[1]) << 8) |
			   (((int)param[2]) << 16) | (((int)param[3]) << 24);

		return time;
	}

}
