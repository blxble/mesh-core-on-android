package com.blxble.meshpanel.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.blxble.meshpanel.MeshApplication;
import com.blxble.meshpanel.element.ActiveDevice;

/**
 * Created by Shibin on 9/9/2017.
 */

public class GrantDialog {
    private static final String TAG = "GrantDialog";
    private byte USER_YES = 0;
    private byte USER_NO = 1;
    private Context mContext;
    private byte[] uuid;
    private byte[] bdAddr;

    public GrantDialog(Context context) {
        this.mContext = context;
    }

    private void userChoosed(byte choice) {
        Log.i(TAG, "userChoosed: "+ choice);
        if (choice == USER_YES) {
            MeshApplication.getDeviceManager().setState(ActiveDevice.DEVICE_ACTIVE_CONFIG);
			MeshApplication.getMeshSvc().setProxyClient(false, (short)0, (byte)0, 0, null);
            MeshApplication.getMeshSvc().grantNewProposer(uuid, bdAddr);
        } else if (choice == USER_NO) {
            MeshApplication.getDeviceManager().setState(ActiveDevice.DEVICE_ACTIVE_NEW);
        }
    }

    public void showConfirm(byte[] uuid, byte[] bdAddr) {
        this.uuid = uuid;
        this.bdAddr = bdAddr;

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(false);
        builder.setTitle("New Proposer");
        builder.setMessage("Please Grant [" + Utility.byte2hex(this.bdAddr, ":") + "] Device?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userChoosed(USER_YES);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userChoosed(USER_NO);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
