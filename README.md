# mesh-core-on-android

Welcome to MeshPanel!

MeshPanel is a demo application to reveal the usage of MeshCore on Android.

MeshCore provides a complete stack of Bluetooth mesh from bearer layer to access layer, also integrates the founditional model.
MeshCore releases easy application interfaces for network establishment and management so that developers could focus on the application-level.

MeshCore is built as a JNI library in this project located at app/src/main/jniLibs with java interfaces located at app/src/main/java/com/blxble/meshpanel/service.
You may notice that there's few interfaces needed.

Current MashCore version is v0.95, following Bluetooth Mesh Profile v1.0.
Features:
*	PB-GATT
*	Proxy
*	Provision without OOB, with Input OOB, Output OOB and static OOB

Because of lack of BLE advertising interfaces, PB-ADV could not be supported. There is only Provision GATT service as well as Proxy GATT client supported.

To get lastest news of MeshCore, see blog http://blog.csdn.net/blxble.
To get Bluetooth Mesh Profile specification, visit https://www.bluetooth.com.

