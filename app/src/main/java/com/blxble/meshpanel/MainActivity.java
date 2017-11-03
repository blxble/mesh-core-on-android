package com.blxble.meshpanel;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.anarchy.classify.ClassifyView;
import com.blxble.meshpanel.element.AttributeActivity;
import com.blxble.meshpanel.element.ModelManager;
import com.blxble.meshpanel.element.ActiveDevice;
import com.blxble.meshpanel.element.DeviceManager;
import com.blxble.meshpanel.adapter.MockFolderAdapter;
import com.blxble.meshpanel.service.JniCallbacks;
import com.blxble.meshpanel.service.MeshService;
import com.blxble.meshpanel.service.MeshServiceBinder;
import com.blxble.meshpanel.utils.PermissionUtils;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

    public static final String TAG = "MainActivity";
    private MockFolderAdapter mMockFolderAdapter;
    public MeshService mMeshSvc;
    public MeshServiceConnection mConnection;
    public DeviceManager mDeviceManager;
    public ModelManager mModelManager;
    JniCallbacks mJniCallbacks;
    private DrawerLayout mDrawerLayout;
    private Intent mIntent;
    private ClassifyView mClassifyView;
    private SwipeRefreshLayout mSwipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSelfPermission();
        initNavigation();
        initLocalData();
        initAdapterData();
        initLocalService();
    }

    private void initAdapterData() {
        mClassifyView = (ClassifyView) findViewById(R.id.classify_view);
        mMockFolderAdapter = new MockFolderAdapter(MeshApplication.getDeviceDataGroup());
        mClassifyView.setAdapter(mMockFolderAdapter);
        mClassifyView.setDebugAble(true);
        mMockFolderAdapter.setOnItemClickListener(new MockFolderAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int mainPosition, int subPosition) {
                MeshApplication.setClickPosition(mainPosition, subPosition);
                Intent intent = new Intent(MainActivity.this, AttributeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    private void initLocalData() {
        mDeviceManager = new DeviceManager(MainActivity.this);
        MeshApplication.setDeviceManager(mDeviceManager);
    }

    private void initNavigation() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
		navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_devices:
                        break;
                    case R.id.nav_lab:
                        break;
                    case R.id.nav_information:
                        break;
                    case R.id.nav_settings:
                        break;
                    case R.id.nav_about:
                        break;
                }
                mDrawerLayout.closeDrawers();
                return true;
            }
        });

        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        mSwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mMeshSvc != null && !mMeshSvc.isOnline()) {
                    refreshDeviceNode();
                }
            }
        });
    }

    private void initLocalService() {
        if (!OpenBluetoothModule()){
            finish();
        }

        mJniCallbacks = new JniCallbacks(((BluetoothManager) getSystemService(BLUETOOTH_SERVICE)));
        initNative();
        mJniCallbacks.init();
        mIntent = new Intent(this, MeshService.class);
        mConnection = new MainActivity.MeshServiceConnection();
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
    }

    private boolean OpenBluetoothModule(){
        boolean bEnable = false;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null){
            if (!(bEnable = bluetoothAdapter.isEnabled())) {
                bEnable = bluetoothAdapter.enable();
            }
        }
        return bEnable;
    }

    private void initLocalElement() {
        mModelManager = new ModelManager(MainActivity.this);
        mModelManager.initModeManager();
    }

    private void initDeviceManager() {
        mDeviceManager.init();
        mDeviceManager.setManagerNotify(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleDeviceNodeMessage(msg);
            }
        });
    }

    private void initSelfPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android M Permission check
            PermissionUtils.requestMultiPermissions(this, mPermissionGrant);
        }
    }

    private PermissionUtils.PermissionGrant mPermissionGrant = new PermissionUtils.PermissionGrant() {
        @Override
        public void onPermissionGranted(int requestCode) {
            switch (requestCode) {
                case PermissionUtils.CODE_ACCESS_COARSE_LOCATION:
                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_ACCESS_COARSE_LOCATION", Toast.LENGTH_SHORT).show();
                    break;
                case PermissionUtils.CODE_READ_EXTERNAL_STORAGE:
                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_READ_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show();
                    break;
                case PermissionUtils.CODE_WRITE_EXTERNAL_STORAGE:
                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_WRITE_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionUtils.requestPermissionsResult(this, requestCode, permissions, grantResults, mPermissionGrant);
    }


    private void createNetwork() {
        mDeviceManager.reset();
        if (mMeshSvc != null) {
            mMeshSvc.createNetwork();
            Toast.makeText(MainActivity.this, "Create MeshNet", Toast.LENGTH_LONG).show();
        }
    }

    private void backNetwork() {
        if (mMeshSvc != null) {
            mMeshSvc.backNetwork();
            Toast.makeText(this, "Back Network", Toast.LENGTH_LONG).show();
        }
    }

    private void refreshDeviceNode() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        createNetwork();
                        mSwipeRefresh.setRefreshing(false);
                        mSwipeRefresh.setEnabled(false);
                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mMeshSvc != null && mMeshSvc.isOnline()) {
            menu.findItem(R.id.action_create).setVisible(false);
            menu.findItem(R.id.action_back).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mesh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.action_create:
                createNetwork();
                break;
            case R.id.action_back:
                backNetwork();
                break;
            /*
            case R.id.action_start:
                startService(mIntent);
                Toast.makeText(this, "Start BLE Mesh", Toast.LENGTH_LONG).show();
                break;
                */
        }
        return super.onOptionsItemSelected(item);
    }

    class MeshServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMeshSvc = ((MeshServiceBinder) service).getService();
            MeshApplication.setMeshSvc(mMeshSvc);
            initDeviceManager();
            initServiceNative();
            initLocalElement();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMeshSvc = null;
        }
    }
	
	void handleDeviceNodeMessage(Message msg) {
        Log.w(TAG, "handleMessage = " + msg.getData().getByte("Event"));
        switch (msg.getData().getByte("Event")) {
            case ActiveDevice.NOTIFY_DEVICE_NODE_INSERT:
                mMockFolderAdapter.notifyItemInsert(0);
                break;
            case ActiveDevice.NOTIFY_DEVICE_NODE_REMOVE:
                mMockFolderAdapter.notifyItemRemoved();
                break;
            case ActiveDevice.NOTIFY_DEVICE_INFO_CHANGED:
                mMockFolderAdapter.notifyDataSetChanged();
                break;
            case ActiveDevice.NOTIFY_DEVICE_INFO_CHANGED_STATE:
                mModelManager.setModelState(msg.getData().getByte("State"),
                        msg.getData().getShort("Addr"));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    public native void initNative();

    public native void initServiceNative();


}
