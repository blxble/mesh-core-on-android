package com.blxble.meshpanel.service;

import android.os.Parcel;
import android.os.Parcelable;

public class MeshElementInfo implements Parcelable {
	public short[] mMids;
	public int[] mVmids;

	public short[] getMids() {
		return mMids;
	}
	public short getMids(int iIdx) {
		return mMids[iIdx];
	}

	private static short[] byteArrayToShortArray(byte[] src) {
		int count = src.length >> 1;
        short[] dest = new short[count];
        for (int i = 0; i < count; i++) {
                dest[i] = (short) (src[i * 2] << 8 | src[2 * i + 1] & 0xff);
        }
        return dest;
	}

	private static byte[] shortArrayToByteArray(short[] src) {
        int count = src.length;
        byte[] dest = new byte[count << 1];
        for (int i = 0; i < count; i++) {
                dest[i * 2] = (byte) (src[i] >> 8);
                dest[i * 2 + 1] = (byte) (src[i] >> 0);
        }

        return dest;
    }

	public static final Parcelable.Creator<MeshElementInfo> CREATOR = new Creator<MeshElementInfo>() {
		public MeshElementInfo createFromParcel(Parcel in) {
			MeshElementInfo eltInfo = new MeshElementInfo();
			int n;
			byte[] tmp;

			n = in.readInt();
			tmp = new byte[n];
			in.readByteArray(tmp);
			eltInfo.mMids = byteArrayToShortArray(tmp);

			n = in.readInt();
			eltInfo.mVmids = new int[n];
			in.readIntArray(eltInfo.mVmids);

			return eltInfo;
		}

		public MeshElementInfo[] newArray(int size) {
			return new MeshElementInfo[size];
		}
	};

	@Override  
    public int describeContents() {  
        return 0;  
    }  

	@Override  
    public void writeToParcel(Parcel parcel, int flags) {  
    	byte[] tmp;
		tmp = shortArrayToByteArray(mMids);
		parcel.writeInt(tmp.length);
        parcel.writeByteArray(tmp);
		parcel.writeInt(mVmids.length);
        parcel.writeIntArray(mVmids);  
    } 
}
