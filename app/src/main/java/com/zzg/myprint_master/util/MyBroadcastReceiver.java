package com.zzg.myprint_master.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhangzhenguo
 * @create 2019/10/8
 * @Email 18311371235@163.com
 * @Describe
 */
public class MyBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "执行";
    private MessageDevices message;
    private List<BluetoothDevice> devices;
    private BluetoothDevice device;
    private Handler mHandler;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.message = (MessageDevices) context;
        String action = intent.getAction();
        devices = new ArrayList<>();
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            switch (state) {
                case BluetoothAdapter.STATE_TURNING_OFF:
//                    Log.d("执行","蓝牙已关闭");
                    message.showInfo("蓝牙已关闭");
//                    message.hideDeviceMessage();
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
//                    Log.d("执行","蓝牙已开启");
                    message.showInfo("蓝牙已开启");
                    break;
            }
            //搜索到蓝牙设备
        } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device == null || device.getName() == null || device.getName().equals("")) {
                return;
            }
//            Log.d("执行","获取蓝牙中");
            message.setDeviceMessage(device);

            //开始搜索
        } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
//            Log.d("执行","开始搜索");
            message.showInfo("开始搜索");
//            message.showDeviceMessage();
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//            Log.d("执行","搜索完成");
            message.showInfo("搜索完成");
//            message.dismiss();
        } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            switch (device.getBondState()) {
                case BluetoothDevice.BOND_BONDING://正在配对
//                    Log.d("执行","正在配对");
                    message.showInfo("正在配对......");
                    break;
                case BluetoothDevice.BOND_BONDED://配对结束
//                    Log.d("执行","完成配对");
                    message.showInfo("完成配对");
                    break;
                case BluetoothDevice.BOND_NONE://取消配对/未配对
//                    Log.d("执行","取消配对");
                    message.showInfo("取消配对");
                default:
                    break;
            }
        }
    }

    public interface MessageDevices {
        void setDeviceMessage(BluetoothDevice device);
        void showInfo(String message);
    }
}
