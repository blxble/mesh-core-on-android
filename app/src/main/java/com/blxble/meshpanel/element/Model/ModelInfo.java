package com.blxble.meshpanel.element.Model;

/**
 * Created by Shibin on 2017/10/20.
 */

public class ModelInfo {

    private byte mElementIdx;
    private long mNetTime;
    private byte mState;

    public void setElementIdx(byte mElementIdx) {
        this.mElementIdx = mElementIdx;
    }

    public void setNetTime(long mNetTime) {
        this.mNetTime = mNetTime;
    }

    public void setState(byte mState) {
        this.mState = mState;
    }

    public byte getElementIdx() {
        return mElementIdx;
    }

    public long getNetTime() {
        return mNetTime;
    }

    public byte getState() {
        return mState;
    }

    public void storeElementNetTime(byte mElementIdx, long mNetTime){
        this.mElementIdx = mElementIdx;
        this.mNetTime = mNetTime;
    }

    public void storeElementNetLight(byte mElementIdx, byte state){
        this.mElementIdx = mElementIdx;
        this.mState = state;
    }
}
