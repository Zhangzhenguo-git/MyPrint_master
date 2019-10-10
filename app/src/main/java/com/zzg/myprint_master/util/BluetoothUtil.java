package com.zzg.myprint_master.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.zzg.myprint_master.R;
import com.zzg.myprint_master.service.MyBluetoothService;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author Zhangzhenguo
 * @create 2019/9/29
 * @Email 18311371235@163.com
 * @Describe
 */
public class BluetoothUtil {
    private static Context mContext;
    private static BluetoothAdapter adapter;
    private static List<BluetoothDevice> listBluetoothDevices;
    private static Set<BluetoothDevice> setBluetoothDevices;
    private static String MY_UUID="00001101-0000-1000-8000-00805F9B34FB";

    public BluetoothUtil(Context mContext) {
        this.mContext = mContext;
        /**
         * 创建蓝牙适配器，并检查设备是否支持蓝牙
         */
        adapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean checkBluetoothIsSuppor() {
        if (adapter != null) {
            //检查设备是否已开启
            if (adapter.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取adapter
     *
     * @return
     */
    public BluetoothAdapter getAdapter() {
        return adapter;
    }

    /**
     * 转换成List返回
     *
     * @return
     */
    public List<BluetoothDevice> getListPairedBluetooth() {
        Set<BluetoothDevice> devices = getSetPairedBluetooth();
        listBluetoothDevices = new ArrayList<>();
        for (BluetoothDevice item : devices) {
            listBluetoothDevices.add(item);
        }
        return listBluetoothDevices;
    }

    /**
     * 返回Set
     *
     * @return
     */
    public Set<BluetoothDevice> getSetPairedBluetooth() {
        if (setBluetoothDevices == null) {
            setBluetoothDevices = new HashSet<>();
        }
        setBluetoothDevices = adapter.getBondedDevices();
        return setBluetoothDevices;
    }

    /**
     * 查找设备
     *
     * @return
     */
    public void startBluetooth() {
        if (adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }
        adapter.startDiscovery();
    }

    /**
     * 关闭查找
     *
     * @return
     */
    public void closeBluetooth() {
        if (adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }
    }



    /**
     * 进行绑定
     * @param device
     * @return
     */
    public boolean createBond(BluetoothDevice device) {
        return device.createBond();
    }

    /**
     * 取消绑定
     *
     * @param device
     * @return
     */
    public boolean removeBond(BluetoothDevice device) {
        Class btDeviceCls = BluetoothDevice.class;
        Method removeBond = null;
        boolean isTrue = false;
        try {
            removeBond = btDeviceCls.getMethod("removeBond");
            removeBond.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        try {
            isTrue = (boolean) removeBond.invoke(device);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return isTrue;
    }

    /**
     * 启动连接
     * @param device
     * @return
     */
    public BluetoothSocket connectDevice(BluetoothDevice device) {
        BluetoothSocket socket = null;
        UUID uuid = UUID.fromString(MY_UUID);
        Log.d("执行", uuid + "");
        try {
            socket = device.createRfcommSocketToServiceRecord(uuid);
            socket.connect();
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException closeException) {
                return null;
            }
            return null;
        }
        return socket;
    }

//    /**
//     * 配对线程
//     */
//    public static class AcceptThread extends Thread {
//        private final BluetoothServerSocket mmServerSocket;
//        public AcceptThread() {
//            // Use a temporary object that is later assigned to mmServerSocket
//            // because mmServerSocket is final.
//            //创建一个蓝牙服务长连接对象，并初始化为null
//            BluetoothServerSocket tmp = null;
//            try {
//                // MY_UUID is the app's UUID string, also used by the client code.
//                //通过调用 listenUsingRfcommWithServiceRecord() 获取 BluetoothServerSocket，并赋给自己创建的长连接对象
//                UUID uuid = UUID.fromString(MY_UUID);
//                tmp = adapter.listenUsingRfcommWithServiceRecord("测试蓝牙连接", uuid);
//            } catch (IOException e) {
//                Log.d("Socket's listen()1", e.getMessage() + "");
//            }
//            mmServerSocket = tmp;
//        }
//
//        public void run() {
//            BluetoothSocket socket = null;
//            // Keep listening until exception occurs or a socket is returned.
//            //然后使用accept（这是一个阻塞调用），当发生接受连接或异常时，该调用就会返回，如果当远程设备发送包含UUID的请求，
//            // 并且相互匹配，服务器才会接受连接，连接成功后返回BluetooehSecket对象。
//            while (true) {
//                try {
//                    socket = mmServerSocket.accept();
//                } catch (IOException e) {
//                    Log.d("Socket's accept()2", e.getMessage());
//                    break;
//                }
//                //如果匹配成功，会返回一个对象
//                if (socket != null) {
//                    // A connection was accepted. Perform work associated with
//                    // the connection in a separate thread.
//                    new MyBluetoothService.ConnectedThread(socket);
//                    try {
//                        mmServerSocket.close();
//                    } catch (IOException e) {
//                        Log.d("Socket's accept()3", e.getMessage());
//                    }
//                    break;
//                }
//            }
//        }
//
//        // Closes the connect socket and causes the thread to finish.
//        public void cancel() {
//            try {
//                mmServerSocket.close();
//            } catch (IOException e) {
//                Log.d("Could not close4", e.getMessage());
//            }
//        }
//    }
}
