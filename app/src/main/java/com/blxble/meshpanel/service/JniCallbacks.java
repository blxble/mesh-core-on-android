package com.blxble.meshpanel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import android.util.Log;
import android.os.Handler;
import android.os.ParcelUuid;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;

public class JniCallbacks extends MeshService {
		private static final boolean D = true;
		public static final String TAG="JniCallback";
		private BluetoothManager mBluetoothManager;
		private BluetoothAdapter mBluetoothAdapter;
		private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
		private BluetoothLeScanner mBluetoothLeScanner;
		private BluetoothGattServer mGattServer;

		private Handler mHandler;
	    private static final long SCAN_PERIOD = 1000;
		private boolean mScanEnable = false;

		private int mGattMtu = 69;

		private PeerDevice mProvServerDev;
		private PeerDevice mProvClientDev;
		private PeerDevice mProxyDev;

		private static final byte SVC_PROVISION = 0;
		private static final byte SVC_PROXY = 1;
		private static final String UUID_PB_SVC = "00001827-0000-1000-8000-00805F9B34FB";
		private static final String UUID_PB_CHAR_DATA_IN = "00002ADB-0000-1000-8000-00805F9B34FB";
		private static final String UUID_PB_CHAR_DATA_OUT = "00002ADC-0000-1000-8000-00805F9B34FB";
		private static final String UUID_CCCD = "00002902-0000-1000-8000-00805F9B34FB";
		private static final String UUID_PROXY_SVC = "00001828-0000-1000-8000-00805F9B34FB";
		private static final String UUID_PROXY_CHAR_DATA_IN = "00002ADD-0000-1000-8000-00805F9B34FB";
		private static final String UUID_PROXY_CHAR_DATA_OUT = "00002ADE-0000-1000-8000-00805F9B34FB";
		/* jsut used it to occupy the att handle, the max handle of last service will be set to 65535 by android, it is harmful to the device*/
		private static final String UUID_TEST_SVC = "0000FFFF-0000-1000-8000-00805F9B34FB"; 


		public native void initCallbackNative();
		public native void advReportNative(int connType,int addrType,byte[] addr,byte[] data,int rssi);
		public native void connectionChangedNative(byte[] addr,boolean connected, int status);
		public native void provServerPduSentNative();
		public native void provServerPduInNative(byte[] pdu);
		public native void provClientPduSentNative();
		public native void provClientPduInNative(byte[] pdu);
		public native void proxyPduInNative(byte[] pdu);

		public JniCallbacks(BluetoothManager manager) {
			mBluetoothManager = manager;
			mBluetoothAdapter = mBluetoothManager.getAdapter();
		}

		class PeerDevice {
			//private static final String PTAG = "PeerDevice";
			byte mConnectionState;
			BluetoothGatt mGattClient;
			BluetoothGattServer mGattServer;
			BluetoothDevice mDevice;
			BluetoothGattCharacteristic mCharData;
			byte mPeerRole;
			boolean mSending;
			List<byte[]> mSendList;

			public static final byte ROLE_SERVER = 0;
			public static final byte ROLE_CLIENT = 1;
			public static final byte STATE_DISCONNECTED = 0;
		    public static final byte STATE_CONNECTING = 1;
		    public static final byte STATE_CONNECTED = 2;

			PeerDevice(BluetoothGatt gatt) {
				mConnectionState = STATE_DISCONNECTED;
				mPeerRole = ROLE_SERVER;
				mGattClient = gatt;
				mSending = false;
				mSendList = new ArrayList<byte[]>();
			}

			PeerDevice(BluetoothGattServer server) {
				mConnectionState = STATE_DISCONNECTED;
				mPeerRole = ROLE_CLIENT;
				mGattServer = server;
				mSending = false;
				mSendList = null;
			}

			public void setPeerDevice(BluetoothDevice device) {
				mDevice = device;
			}

			public BluetoothDevice getPeerDevice() {
				return mDevice;
			}

			public void setConnectionState(byte state) {
				mConnectionState = state;

				if (state == STATE_DISCONNECTED) {
					if (mSendList != null) {
						mSendList.clear();
						mSendList = null;
						mSending = false;
					}
				}
			}

			public byte getConnectionState() {
				return mConnectionState;
			}

			public byte getPeerRole() {
				return mPeerRole;
			}

			public BluetoothGatt getGattClient() {
					return mGattClient;
			}

			public BluetoothGattServer getGattServer() {
					return mGattServer;
			}

			public void setDataCharacterisitic(BluetoothGattCharacteristic charData) {
				mCharData = charData;
			}

			public BluetoothGattCharacteristic getDataCharacterisitic() {
				return mCharData;
			}

			public void dataOut(byte[] data) {
				synchronized(this) {
					boolean ret;
					if (mPeerRole == ROLE_SERVER) {
						// local is client
						if (mSendList != null) {
							if (mSending == false) {
								mCharData.setValue(data);
								ret = mGattClient.writeCharacteristic(mCharData);
								//Log.i(TAG, "dataOut, ret = "+ret+", addr="+mDevice.getAddress());
								mSending = true;
							} else {
								mSendList.add(data);
							}
						}
					} else {
						// local is server
						mCharData.setValue(data);
						mGattServer.notifyCharacteristicChanged(mDevice, mCharData, false);
					}
				}
			}			

			public void onDataOut() {
				synchronized(this) {
					byte[] data;
					//Log.i(PTAG, "onDataOut");
					if (mSendList != null) {
						if (mSendList.size() > 0) {
							data = mSendList.remove(0);
							mCharData.setValue(data);
							mGattClient.writeCharacteristic(mCharData);
						} else {
							mSending = false;
						}
					}
				}
			}
		}
		
		public void init(){
			if(mBluetoothAdapter ==  null){
	            return;
			}	
			mHandler = new Handler();

			initCallbackNative();
		}

		public void pbgattServerAdd() {
			mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
			if(mBluetoothLeAdvertiser == null){
				Log.e(TAG, "the device not support peripheral");
			}

			mGattServer = mBluetoothManager.openGattServer(this, mServerCallback);
			BluetoothGattService service = new BluetoothGattService(UUID.fromString(UUID_PB_SVC), 
																BluetoothGattService.SERVICE_TYPE_PRIMARY);
			BluetoothGattCharacteristic char_data_in = new BluetoothGattCharacteristic(UUID.fromString(UUID_PB_CHAR_DATA_IN), 
																BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE, 
																BluetoothGattCharacteristic.PERMISSION_WRITE);
			BluetoothGattCharacteristic char_data_out = new BluetoothGattCharacteristic(UUID.fromString(UUID_PB_CHAR_DATA_OUT), 
																BluetoothGattCharacteristic.PROPERTY_NOTIFY, 
																BluetoothGattCharacteristic.PERMISSION_READ);
			BluetoothGattDescriptor desc_cccd = new BluetoothGattDescriptor(UUID.fromString(UUID_CCCD), 
																 BluetoothGattCharacteristic.PERMISSION_READ | 
																 BluetoothGattCharacteristic.PERMISSION_WRITE);
			byte[] cccd = {1};
			desc_cccd.setValue(cccd);
			char_data_out.addDescriptor(desc_cccd);
			service.addCharacteristic(char_data_in);
			service.addCharacteristic(char_data_out);

			mGattServer.addService(service);

			service = new BluetoothGattService(UUID.fromString(UUID_TEST_SVC), BluetoothGattService.SERVICE_TYPE_PRIMARY);
			mGattServer.addService(service);

			mProvClientDev = new PeerDevice(mGattServer);
			mProvClientDev.setDataCharacterisitic(char_data_out);

		}
		
		public void pbgattAdvertiseStart(byte[] serviceData) {
			
			if(D){
				Log.d(TAG, "pbgattAdvertiseStart");
			}

			ParcelUuid uu = ParcelUuid.fromString(UUID_PB_SVC);
			
			mBluetoothLeAdvertiser.startAdvertising(createAdvSettings(true,0), 
													createAdvertiseData(uu, serviceData),
													mAdvertiseCallback);
		}

		public void advertiseStop(){
			if(D){
				Log.d(TAG, "adv stop");
			}
			mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
		}
		
		private AdvertiseSettings createAdvSettings(boolean connectable, int timeoutMillis) {
			AdvertiseSettings.Builder mSettingsbuilder = new AdvertiseSettings.Builder();
			mSettingsbuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
			mSettingsbuilder.setConnectable(connectable);
			mSettingsbuilder.setTimeout(timeoutMillis);
			mSettingsbuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
			AdvertiseSettings mAdvertiseSettings = mSettingsbuilder.build();
			if(mAdvertiseSettings == null){
				if(D){
					Log.e(TAG,"mAdvertiseSettings == null");
				}
			}
			return mAdvertiseSettings;
		 }	
		
	    private AdvertiseData createAdvertiseData(ParcelUuid uuid, byte[] serviceData){		 
		 	AdvertiseData.Builder mDataBuilder = new AdvertiseData.Builder();
		 	mDataBuilder.addServiceUuid(uuid);
			mDataBuilder.addServiceData(uuid, serviceData);
			AdvertiseData mAdvertiseData = mDataBuilder.build();

			if(mAdvertiseData==null){
				Log.e(TAG, "mAdvertiseSettings == null");
			}
			return mAdvertiseData;
	    }
	    
	    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
			@Override
			public void onStartSuccess(AdvertiseSettings settingsInEffect) {
				super.onStartSuccess(settingsInEffect);
				if (settingsInEffect == null) {
					Log.e(TAG, "onStartSuccess, settingInEffect is null");
				}
			}
			
			@Override
			public void onStartFailure(int errorCode) {
				super.onStartFailure(errorCode);
				Log.e(TAG,"onStartFailure errorCode" + errorCode);
		    }
		};

		private final BluetoothGattServerCallback mServerCallback = new BluetoothGattServerCallback() {
			@Override
	        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
				if (mProvClientDev != null) {
					if (mProvClientDev.getPeerDevice() != null) {
						if (mProvClientDev.getPeerDevice().equals(device)) {
							String strAddr = device.getAddress();
							byte[] addr = {0, 0, 0, 0, 0, 0};
							addr[0] = (byte)Integer.decode("0x"+strAddr.substring(0,2)).intValue();
							addr[1] = (byte)Integer.decode("0x"+strAddr.substring(3,5)).intValue();
							addr[2] = (byte)Integer.decode("0x"+strAddr.substring(6,8)).intValue();
							addr[3] = (byte)Integer.decode("0x"+strAddr.substring(9,11)).intValue();
							addr[4] = (byte)Integer.decode("0x"+strAddr.substring(12,14)).intValue();
							addr[5] = (byte)Integer.decode("0x"+strAddr.substring(15,17)).intValue();
							
				            if (newState == BluetoothProfile.STATE_CONNECTED) {
								if (mProvClientDev.getConnectionState() == PeerDevice.STATE_DISCONNECTED) {
									mProvClientDev.setPeerDevice(device);
									mProvClientDev.setConnectionState(PeerDevice.STATE_CONNECTED);
									if (D) {
					                	Log.i(TAG, "Connected to GATT Client.");
									}

									connectionChangedNative(addr, true, status);
								}

				            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				                mProvClientDev.setConnectionState(PeerDevice.STATE_DISCONNECTED);
								if (D) {
				                	Log.i(TAG, "Disconnected from GATT Client.");
								}

								advertiseStop();
								connectionChangedNative(addr, false, status);

								mGattServer.close();
				            }
						}
					}
				}
	        }

			@Override
			public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, 
													boolean preparedWrite, boolean responseNeeded, int offset, byte[] value)
			{
				super.onDescriptorWriteRequest(device, requestId, descriptor,preparedWrite, responseNeeded, offset, value);
				
				mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
				//mPbCharDataOutDescCccd.setValue(value);
			}

			@Override
			public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, 
													BluetoothGattCharacteristic characteristic, boolean preparedWrite, 
													boolean responseNeeded, int offset, byte[] value) {
				super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
						
				mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);

				if (mProvClientDev.getPeerDevice().equals(device)) {
					if (characteristic.getUuid().compareTo(UUID.fromString(UUID_PB_CHAR_DATA_IN)) == 0)
					{
						provServerPduInNative(value);
					}
				}
			}
		};

		public void gattClientAdd() {
			if (mBluetoothLeScanner == null)
			{
				mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
			}
		}

	    public void scanLeDevice(final boolean enable) {
			//Log.i(TAG, "Scan "+(enable ? "enable" : "disable"));
	        if (enable == true && mScanEnable == false) {
	            mHandler.postDelayed(new Runnable() {
	                @Override
	                public void run() {                 
	                	mBluetoothLeScanner.startScan(createScanFilters(), createScanSettings(), mScanCallback);
	                }
	            }, SCAN_PERIOD);
	            mBluetoothLeScanner.startScan(createScanFilters(), createScanSettings(), mScanCallback);
				mScanEnable = true; // keep scanning
	        } else {
	        	//Log.i(TAG, "stop Scan");
	        	//mBluetoothLeScanner.stopScan(mScanCallback);
	        }
	    }

		private ScanSettings createScanSettings() {
			ScanSettings.Builder builder = new ScanSettings.Builder();
			ScanSettings settings;

			builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
			builder.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);
			builder.setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT);
			settings = builder.build();

			return settings;
		}

		private List<ScanFilter> createScanFilters() {
			return null;
		}

	    public ScanCallback mScanCallback=new ScanCallback() {
	    	@Override
	        public void onScanResult(int callbackType, final ScanResult result) {
	            //Log.i(TAG, "Scan Result:"+result.toString()); 

	    		int connType = 3;
				int addrType = 0;
				
				String strAddr=result.getDevice().getAddress();
				byte[] addr = {0, 0, 0, 0, 0, 0};
				addr[0] = (byte)Integer.decode("0x"+strAddr.substring(0,2)).intValue();
				addr[1] = (byte)Integer.decode("0x"+strAddr.substring(3,5)).intValue();
				addr[2] = (byte)Integer.decode("0x"+strAddr.substring(6,8)).intValue();
				addr[3] = (byte)Integer.decode("0x"+strAddr.substring(9,11)).intValue();
				addr[4] = (byte)Integer.decode("0x"+strAddr.substring(12,14)).intValue();
				addr[5] = (byte)Integer.decode("0x"+strAddr.substring(15,17)).intValue();
				
	            byte[] data = result.getScanRecord().getBytes();
	            int rssi = result.getRssi();
	            
	            advReportNative(connType, addrType, addr, data, rssi);
	        }
	 
	        @Override
	        public void onBatchScanResults(List<ScanResult> results) {
	            for (ScanResult sr : results) {
	                Log.i(TAG, "BatchScanResult: "+sr.toString());
	            }
	        }
	 
	        @Override
	        public void onScanFailed(int errorCode) {
	            //Log.e(TAG, "Scan Failed, Error Code: " + errorCode);
	        }
		};
		/**
	     * Connects to the GATT server hosted on the Bluetooth LE device.
	     *
	     * @param addr The device address of the destination device.
	     *
	     * @return Return true if the connection is initiated successfully. The connection result
	     *         is reported asynchronously through the
	     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	     *         callback.
	     */
	    public boolean createConnection(byte[] addr, byte svc) {
	    	BluetoothGatt gatt;
	    	String address = String.format("%02X:%02X:%02X:%02X:%02X:%02X", addr[0], addr[1], addr[2], addr[3], addr[4], addr[5]);

			if (D) {
				Log.d(TAG, "createConnection addr = "+address);
			}

	        if (mBluetoothAdapter == null || address == null) {
	            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
	            return false;
	        }

			PeerDevice peer_dev = (svc == SVC_PROVISION) ? mProvServerDev : mProxyDev;

			if (peer_dev != null) { 
				if (peer_dev.getConnectionState() != PeerDevice.STATE_DISCONNECTED) {
					if (D) {
						Log.w(TAG, "Already connected");
					}
					return false;
				}
			}

	        // Previously connected device.  Try to reconnect.
	        if (peer_dev != null) { 
				if (peer_dev.getPeerDevice() != null) { 
					if (peer_dev.getPeerDevice().getAddress().compareTo(address) == 0) {
						if (D) {
			            	Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
						}
						
						gatt = peer_dev.getGattClient();
			            if (gatt.connect()) {
			                peer_dev.setConnectionState(PeerDevice.STATE_CONNECTING);
			                return true;
			            } else {
			                return false;
			            }
					}
				}
	        }

	        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
	        if (device == null) {
	            Log.w(TAG, "Device not found.  Unable to connect.");
	            return false;
	        }
			//advertiseStop();
			scanLeDevice(false);
			
	        gatt = device.connectGatt(this, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);

			peer_dev = new PeerDevice(gatt);
			peer_dev.setPeerDevice(device);
	        peer_dev.setConnectionState(PeerDevice.STATE_CONNECTING);

			if (svc == SVC_PROVISION) {
				mProvServerDev = peer_dev;
			} else {
				mProxyDev = peer_dev;
			}
			
	        return true;
	    }

		public void disconnect(byte svc) {
			BluetoothGattServer server;
			BluetoothGatt client;
			PeerDevice dev = null;
			
			if (svc == SVC_PROVISION) {
				if (mProvClientDev != null) {
					if (mProvClientDev.getConnectionState() != PeerDevice.STATE_DISCONNECTED) {
						server = (BluetoothGattServer)mProvClientDev.getGattServer();
						server.cancelConnection(mProvClientDev.getPeerDevice());
					}
					return;
				}
				if (mProvServerDev != null) {
					dev = mProvServerDev;
				}
				
			} else {
				dev = mProxyDev;
			}

			if (dev != null) {
				if (dev.getConnectionState() != PeerDevice.STATE_DISCONNECTED) {
					client = dev.getGattClient();
					client.disconnect();
				}
			}
		}

		private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
	        @Override
	        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
	        	if (newState == BluetoothProfile.STATE_CONNECTED) {
					//gatt.discoverServices();
					gatt.requestMtu(69);
					
					if (mProvServerDev != null) {
						if (mProvServerDev.getGattClient() != null) {
							if (mProvServerDev.getGattClient().equals(gatt)) {
								//mProvServerDev.setConnectionState(PeerDevice.STATE_CONNECTED);
							}
						}
					}
					if (mProxyDev != null) {
						if (mProxyDev.getGattClient() != null) {
							if (mProxyDev.getGattClient().equals(gatt)) {
								//mProxyDev.setConnectionState(PeerDevice.STATE_CONNECTED);
							}
						}
					}
					if (D) {
						Log.i(TAG, "Connected to GATT server.");
					}
					
	        	} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
					boolean retry = false;
	        		if (mProvServerDev != null) { 
						if (mProvServerDev.getGattClient() != null) {
							if (mProvServerDev.getGattClient().equals(gatt)) {
								if (mProvServerDev.getConnectionState() == PeerDevice.STATE_CONNECTING) {
									Log.i(TAG, "connect failed, try again, status="+status);
									gatt.connect();
									retry = true;
								} else {
									mProvServerDev.setConnectionState(PeerDevice.STATE_DISCONNECTED);
									gatt.close();
									mProvServerDev = null;
								}
							}
						}
					}
					if (mProxyDev != null) { 
						if (mProxyDev.getGattClient() != null) {
							if (mProxyDev.getGattClient().equals(gatt)) {
								if (mProxyDev.getConnectionState() == PeerDevice.STATE_CONNECTING) {
									Log.i(TAG, "connect failed, try again, status="+status);
									gatt.connect();
									retry = true;
								} else {
									mProxyDev.setConnectionState(PeerDevice.STATE_DISCONNECTED);
									gatt.close();
									mProxyDev = null;
								}
							}
						}
					}

					if (retry == false) {
						if (D) {
							Log.i(TAG, "Disconnected from GATT server.");
						}

						String strAddr=gatt.getDevice().getAddress();
						byte[] addr = {0, 0, 0, 0, 0, 0};
						addr[0] = (byte)Integer.decode("0x"+strAddr.substring(0,2)).intValue();
						addr[1] = (byte)Integer.decode("0x"+strAddr.substring(3,5)).intValue();
						addr[2] = (byte)Integer.decode("0x"+strAddr.substring(6,8)).intValue();
						addr[3] = (byte)Integer.decode("0x"+strAddr.substring(9,11)).intValue();
						addr[4] = (byte)Integer.decode("0x"+strAddr.substring(12,14)).intValue();
						addr[5] = (byte)Integer.decode("0x"+strAddr.substring(15,17)).intValue();

						connectionChangedNative(addr, false, status);
					}
	        	}
	        }

	        @Override
	        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
	            if (status == BluetoothGatt.GATT_SUCCESS) {
					String strAddr=gatt.getDevice().getAddress();
					byte[] addr = {0, 0, 0, 0, 0, 0};
					addr[0] = (byte)Integer.decode("0x"+strAddr.substring(0,2)).intValue();
					addr[1] = (byte)Integer.decode("0x"+strAddr.substring(3,5)).intValue();
					addr[2] = (byte)Integer.decode("0x"+strAddr.substring(6,8)).intValue();
					addr[3] = (byte)Integer.decode("0x"+strAddr.substring(9,11)).intValue();
					addr[4] = (byte)Integer.decode("0x"+strAddr.substring(12,14)).intValue();
					addr[5] = (byte)Integer.decode("0x"+strAddr.substring(15,17)).intValue();

					if (mProxyDev != null) {
						if (mProxyDev.getConnectionState() == PeerDevice.STATE_CONNECTING) {
							mProxyDev.setConnectionState(PeerDevice.STATE_CONNECTED);
							// check proxy service
							BluetoothGattService proxy_svc;
							BluetoothGattCharacteristic proxy_data_out;
							BluetoothGattCharacteristic proxy_data_in;
							BluetoothGattDescriptor proxy_data_out_cccd;

							proxy_svc = gatt.getService(UUID.fromString(UUID_PROXY_SVC));
							if (proxy_svc != null) {
								proxy_data_in = proxy_svc.getCharacteristic(UUID.fromString(UUID_PROXY_CHAR_DATA_IN));
								proxy_data_out = proxy_svc.getCharacteristic(UUID.fromString(UUID_PROXY_CHAR_DATA_OUT));
								proxy_data_out_cccd = proxy_data_out.getDescriptor(UUID.fromString(UUID_CCCD));
								if (proxy_data_out_cccd != null) {
									byte[] val = {1};
									proxy_data_out_cccd.setValue(val);
									gatt.setCharacteristicNotification(proxy_data_out, true);
									gatt.writeDescriptor(proxy_data_out_cccd);
								}

								mProxyDev.setDataCharacterisitic(proxy_data_in);
							}
						}
					}

					if (mProvServerDev != null) {
						if (mProvServerDev.getConnectionState() == PeerDevice.STATE_CONNECTING) {
							mProvServerDev.setConnectionState(PeerDevice.STATE_CONNECTED);
							// check provision service
							BluetoothGattService prov_svc;
							BluetoothGattCharacteristic prov_data_out;
							BluetoothGattCharacteristic prov_data_in;
							BluetoothGattDescriptor prov_data_out_cccd;

							prov_svc = gatt.getService(UUID.fromString(UUID_PB_SVC));
							if (prov_svc != null) {
								prov_data_in = prov_svc.getCharacteristic(UUID.fromString(UUID_PB_CHAR_DATA_IN));
								prov_data_out = prov_svc.getCharacteristic(UUID.fromString(UUID_PB_CHAR_DATA_OUT));
								prov_data_out_cccd = prov_data_out.getDescriptor(UUID.fromString(UUID_CCCD));
								if (prov_data_out_cccd != null) {
									byte[] val = {1};
									prov_data_out_cccd.setValue(val);
									gatt.setCharacteristicNotification(prov_data_out, true);
									gatt.writeDescriptor(prov_data_out_cccd);
								}

								mProvServerDev.setDataCharacterisitic(prov_data_in);
							}
						}
					}

					//connectionChangedNative(addr, true, status);
	            } else {
	                Log.w(TAG, "onServicesDiscovered received, error: " + status);
	            }
	        }

			@Override
			public void onMtuChanged (BluetoothGatt gatt, int mtu, int status) {
				if (status == BluetoothGatt.GATT_SUCCESS) {
					mGattMtu = mtu;
					gatt.discoverServices();
				}
			}

	        @Override
	        public void onCharacteristicChanged(BluetoothGatt gatt,
	                                            BluetoothGattCharacteristic characteristic) {
	            if (characteristic.getUuid().compareTo(UUID.fromString(UUID_PROXY_CHAR_DATA_OUT)) == 0) {
					proxyPduInNative(characteristic.getValue());
	           	} else if (characteristic.getUuid().compareTo(UUID.fromString(UUID_PB_CHAR_DATA_OUT)) == 0) {
	           		provClientPduInNative(characteristic.getValue());
	           	}
	        }

			@Override
			public void onCharacteristicWrite (BluetoothGatt gatt, 
                								BluetoothGattCharacteristic characteristic, 
                								int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);

				if (characteristic.getUuid().compareTo(UUID.fromString(UUID_PROXY_CHAR_DATA_IN)) == 0) {
					if (mProxyDev != null && mProxyDev.getConnectionState() == PeerDevice.STATE_CONNECTED) {
						mProxyDev.onDataOut();
					}
				} else if (characteristic.getUuid().compareTo(UUID.fromString(UUID_PB_CHAR_DATA_IN)) == 0) {
					if (mProvServerDev != null && mProvServerDev.getConnectionState() == PeerDevice.STATE_CONNECTED) {
						provClientPduSentNative();
						mProvServerDev.onDataOut();
					}
				}
			}

			@Override
			public void onDescriptorWrite(BluetoothGatt gatt, 
                							BluetoothGattDescriptor descriptor, 
                							int status) {
                String strAddr=gatt.getDevice().getAddress();
				byte[] addr = {0, 0, 0, 0, 0, 0};
				addr[0] = (byte)Integer.decode("0x"+strAddr.substring(0,2)).intValue();
				addr[1] = (byte)Integer.decode("0x"+strAddr.substring(3,5)).intValue();
				addr[2] = (byte)Integer.decode("0x"+strAddr.substring(6,8)).intValue();
				addr[3] = (byte)Integer.decode("0x"+strAddr.substring(9,11)).intValue();
				addr[4] = (byte)Integer.decode("0x"+strAddr.substring(12,14)).intValue();
				addr[5] = (byte)Integer.decode("0x"+strAddr.substring(15,17)).intValue();
				
                if (descriptor.getUuid().compareTo(UUID.fromString(UUID_CCCD)) == 0) {
					connectionChangedNative(addr, true, status);
                }
			}
	    };

		public void sendProvisionPdu(byte[] pdu) {
			if (mProvServerDev != null) {
				if (mProvServerDev.getConnectionState() == PeerDevice.STATE_CONNECTED) {
					mProvServerDev.dataOut(pdu);
				}
			} else if (mProvClientDev != null) { 
				if( mProvClientDev.getConnectionState() == PeerDevice.STATE_CONNECTED) {
					mProvClientDev.dataOut(pdu);
					provServerPduSentNative();
				}
			}
		}

		public void sendProxyPdu(byte[] pdu) {
			if (mProxyDev != null) { 
				if ( mProxyDev.getConnectionState() == PeerDevice.STATE_CONNECTED) {
					mProxyDev.dataOut(pdu);
				}
			}
		}

		public int getMtu() {
			return mGattMtu;
		}

		public byte[] aesEncrypt(byte[] key, byte[] plainText) throws Exception{
			byte[] result;
			result = AES128.encrypt(key, plainText);
			return result;
		}
}