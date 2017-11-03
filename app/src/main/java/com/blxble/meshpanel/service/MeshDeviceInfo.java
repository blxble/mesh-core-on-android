package com.blxble.meshpanel.service;

import android.os.Parcel;
import android.os.Parcelable;

public class MeshDeviceInfo implements Parcelable {
	public short mCid;
	public short mVid;
	public short mPid;

	public static final Parcelable.Creator<MeshDeviceInfo> CREATOR = new Creator<MeshDeviceInfo>() {
		public MeshDeviceInfo createFromParcel(Parcel in) {
			MeshDeviceInfo devInfo = new MeshDeviceInfo();
			byte[] tmp = new byte[2];

			in.readByteArray(tmp);
			devInfo.mCid = (short)(((short)tmp[0]) | (((short)tmp[1]) << 8));
			
			in.readByteArray(tmp);
			devInfo.mVid = (short)(((short)tmp[0]) | (((short)tmp[1]) << 8));

			in.readByteArray(tmp);
			devInfo.mPid = (short)(((short)tmp[0]) | (((short)tmp[1]) << 8));

			return devInfo;
		}

		public MeshDeviceInfo[] newArray(int size) {
			return new MeshDeviceInfo[size];
		}
	};

	@Override  
    public int describeContents() {  
        return 0;  
    }  

	@Override  
    public void writeToParcel(Parcel parcel, int flags) { 
    	byte[] tmp = new byte[2];

		tmp[0] = (byte)(mCid & 0xFF);
		tmp[1] = (byte)((mCid >> 8) & 0xFF);
    	parcel.writeByteArray(tmp);

		tmp[0] = (byte)(mVid & 0xFF);
		tmp[1] = (byte)((mVid >> 8) & 0xFF);
		parcel.writeByteArray(tmp);

		tmp[0] = (byte)(mPid & 0xFF);
		tmp[1] = (byte)((mPid >> 8) & 0xFF);
		parcel.writeByteArray(tmp);
    } 
}