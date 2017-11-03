package com.blxble.meshpanel.adapter;

import android.sax.ElementListener;

import com.blxble.meshpanel.db.ElementAppItem;

public class StateItem {

	private long id;
	private short type;
	private short eltAddr;
	private String name;
	private String state;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	public short getEltAddr() {
		return eltAddr;
	}

	public void setEltAddr(short eltAddr) {
		this.eltAddr = eltAddr;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setState(String state) {
		this.state = state;
	}

	private void init() {
		if (getType() == ElementAppItem.APP_TYPE_NAME){
			this.name = "Name";
		} else if (getType() == ElementAppItem.APP_TYPE_COLOR){
			this.name = "Color";
		} else if (getType() == ElementAppItem.APP_TYPE_POWER){
			this.name = "Power";
		} else if (getType() == ElementAppItem.APP_TYPE_NET_TIME){
			this.name = "Time";
		} else {
			this.name = "Unknown";
		}
	}

	public StateItem(short type, String state) {
		this.type = type;
		this.state = state;
		init();
	}

	public StateItem(long id, short eltAddr, short type, String state){
		this.id = id;
		this.type = type;
		this.eltAddr = eltAddr;
		this.state = state;
		init();
	}

	public StateItem(String name, String state){
		this.type = ElementAppItem.APP_TYPE_DENOTE;
		this.name = name;
		this.state = state;
	}

	public StateItem(byte type, String name, String state){
		this.type = type;
		this.name = name;
		this.state = state;
	}

	public String getState() {
		return state;
	}

}
