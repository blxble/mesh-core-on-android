package com.blxble.meshpanel.service;

import android.os.Parcel;
import android.os.Parcelable;

public class MeshModelMessageOpcode implements Parcelable {
	public byte mOp0;
	public byte mOp1;
	public byte mOp2;

	public static final Parcelable.Creator<MeshModelMessageOpcode> CREATOR = new Creator<MeshModelMessageOpcode>() {
		public MeshModelMessageOpcode createFromParcel(Parcel in) {
			MeshModelMessageOpcode msgOp = new MeshModelMessageOpcode();

			msgOp.mOp0 = in.readByte();
			msgOp.mOp1 = in.readByte();
			msgOp.mOp2 = in.readByte();

			return msgOp;
		}

		public MeshModelMessageOpcode[] newArray(int size) {
			return new MeshModelMessageOpcode[size];
		}
	};

	@Override  
    public int describeContents() {  
        return 0;  
    }  

	@Override  
    public void writeToParcel(Parcel parcel, int flags) { 
		parcel.writeByte(mOp0);
		parcel.writeByte(mOp1);
		parcel.writeByte(mOp2);
    } 
}