package com.zzg.myprint_master;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.zzg.myprint_master.adapter.BlueToothAdapter;
import com.zzg.myprint_master.adapter.BlueToothBondedAdapter;
import com.zzg.myprint_master.bean.BlueToothBean;
import com.zzg.myprint_master.util.BluetoothUtil;
import com.zzg.myprint_master.util.MyBroadcastReceiver;
import com.zzg.myprint_master.util.PrintUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BLuetoothActivity extends AppCompatActivity implements MyBroadcastReceiver.MessageDevices {
    @BindView(R.id.bt_Search_Bluetooth)
    Button btSearchBluetooth;
    @BindView(R.id.tv_Bluetooth)
    TextView tvBluetooth;
    @BindView(R.id.rl_Bluetooth_View)
    RecyclerView rlBluetoothView;
    @BindView(R.id.tv_PairedBluetooth)
    TextView tvPairedBluetooth;
    @BindView(R.id.rl_PairedBluetooth_View)
    RecyclerView rlPairedBluetoothView;
    @BindView(R.id.bt_Print)
    Button btPrint;
    @BindView(R.id.ll_button_layout)
    LinearLayout llButtonLayout;


    private BluetoothUtil bluetoothUtil;
    private MyBroadcastReceiver receiver;

    private ProgressDialog progressBar;
    private Context context;
    private List<BluetoothDevice> beanBondedList;
    private List<BluetoothDevice> beanList;

    private BlueToothAdapter blueToothAdapter;
    private BlueToothBondedAdapter blueToothBondedAdapter;
    private BluetoothSocket mSocket;
    private AsyncTask mConnectTask;
    final static int TASK_TYPE_CONNECT = 3;
    final static int TASK_TYPE_PRINT = 6;

    private int mSelectedPosition = 0;
    private BluetoothDevice clickDevice = null;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        ButterKnife.bind(this);
        context = BLuetoothActivity.this;
        receiver = new MyBroadcastReceiver();
        bluetoothUtil = new BluetoothUtil(BLuetoothActivity.this);
        progressBar = new ProgressDialog(context);
        myClick();

    }

    private void myClick() {
        btSearchBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beanList = new ArrayList<>();
                if (!bluetoothUtil.checkBluetoothIsSuppor()) {
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, 1);
                } else {
                    myShowProgressDialog("正在搜索中...");
//                    startActivityForResult(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS),0);
                    bluetoothUtil.startBluetooth();
                }
            }
        });
        btPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectDevice(TASK_TYPE_PRINT);
            }
        });
        progressBar.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (progressBar != null) {
                    progressBar.dismiss();
                }
                bluetoothUtil.closeBluetooth();
            }
        });
    }

    /**
     * 单机打印按钮
     *
     * @param taskType
     */
    private void connectDevice(int taskType) {
        if (clickDevice != null) {
            connectDevice(clickDevice, taskType);
        } else {
            Toast.makeText(context, "还未选择打印设备", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //开启监听蓝牙状态
        startBroadcastReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //关闭监听蓝牙状态
        unregisterReceiver(receiver);
    }

    /**
     * 注册监听蓝牙广播
     */
    private void startBroadcastReceiver() {
        IntentFilter filter1 = new IntentFilter();
        //检测蓝牙状态
        filter1.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, filter1);

        //注册发现设备
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter2);

        //开始扫描远程设备
        IntentFilter filter3 = new IntentFilter();
        filter3.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(receiver, filter3);

        //搜索结束
        IntentFilter filter4 = new IntentFilter();
        filter4.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter4);

        //配对
        IntentFilter filter5 = new IntentFilter();
        filter5.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(receiver, filter5);


    }

    /**
     * 显示已配对蓝牙设备
     */
    private void showBondedBluetooth() {
        beanBondedList = bluetoothUtil.getListPairedBluetooth();
        if (beanBondedList.size() > 0) {
            setBondedAdapterData();
        }
    }

    /**
     * 获取数据并逐一显示出来
     *
     * @param device
     */
    @Override
    public void setDeviceMessage(BluetoothDevice device) {
        int record = 0;
        if (beanList != null && beanList.size() > 0) {
            for (int i = 0; i < beanList.size(); i++) {
                if (beanList.get(i).getName().equals(device.getName().toString())
                        && beanList.get(i).getAddress().equals(device.getAddress().toString())) {
                    record++;
                }
            }
            if (record == 0) {
                beanList.add(device);
            }
        } else {
            beanList.add(device);
        }
        setAdapterData();
    }


    @Override
    public void showInfo(String message) {
        toast(message);
        if (message.equals("蓝牙已开启")) {
            myShowProgressDialog("正在搜索中");
            bluetoothUtil.startBluetooth();
        } else if (message.equals("蓝牙已关闭")) {
            if (beanBondedList != null) {
                beanBondedList = new ArrayList<>();
                if (blueToothBondedAdapter != null) {
                    blueToothBondedAdapter.notifyDataSetChanged();
                }
            }
            if (beanList != null) {
                beanList = new ArrayList<>();
                if (blueToothAdapter != null) {
                    blueToothAdapter.notifyDataSetChanged();
                }
            }
        } else if (message.equals("开始搜索")) {
            showBondedBluetooth();
        } else if (message.equals("搜索完成")) {
            if (progressBar != null) {
                progressBar.dismiss();
            }
        } else if (message.equals("完成配对")) {
            showBondedBluetooth();
        }
    }

    /**
     * 新设备信息
     */
    @SuppressLint("WrongConstant")
    private void setAdapterData() {
        if (blueToothAdapter == null) {
            blueToothAdapter = new BlueToothAdapter(context, beanList);
            LinearLayoutManager manager = new LinearLayoutManager(context);
            manager.setOrientation(OrientationHelper.VERTICAL);
            rlBluetoothView.addItemDecoration(new DividerItemDecoration(context, OrientationHelper.VERTICAL));
            rlBluetoothView.setLayoutManager(manager);
            rlBluetoothView.setAdapter(blueToothAdapter);
        } else {
//            Log.d("執行","刷新");
            blueToothAdapter.notifyDataSetChanged();
        }
        blueToothAdapter.MyOnItemClickListener(new BlueToothAdapter.MyOnItemClickListener() {

            @Override
            public void onItemClick(BluetoothDevice... device) {
                if (bluetoothUtil.createBond(device[0])) {
                    bluetoothUtil.connectDevice(device[0]);
                    clickDevice = device[0];
                }
            }
        });
    }

    /**
     * 打印图片
     *
     * @param socket
     */
    public void doPrintIcon(BluetoothSocket socket) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        PrintUtil.printTest(socket, bitmap);
    }

    /**
     * 已配对设备信息
     */
    @SuppressLint("WrongConstant")
    private void setBondedAdapterData() {
        blueToothBondedAdapter = null;
        if (blueToothBondedAdapter == null) {
            blueToothBondedAdapter = new BlueToothBondedAdapter(context, beanBondedList);
            LinearLayoutManager manager = new LinearLayoutManager(context);
            manager.setOrientation(OrientationHelper.VERTICAL);
            rlPairedBluetoothView.addItemDecoration(new DividerItemDecoration(context, OrientationHelper.VERTICAL));
            rlPairedBluetoothView.setLayoutManager(manager);
            rlPairedBluetoothView.setAdapter(blueToothBondedAdapter);
        } else {
            rlPairedBluetoothView.post(new Runnable() {
                @Override
                public void run() {
                    blueToothBondedAdapter.notifyDataSetChanged();
                }
            });
        }
        blueToothBondedAdapter.MyOnItemClickListener(new BlueToothBondedAdapter.MyOnItemClickListener() {
            @Override
            public void onItemClick(BluetoothDevice... device) {
                connectDevice(device[0], TASK_TYPE_CONNECT);
                clickDevice = device[0];
            }
        });
    }


    protected void myShowProgressDialog(String message) {
        progressBar.setMessage(message);
        if (!progressBar.isShowing()) {
            progressBar.show();
        }
    }

    @Override
    protected void onStop() {
        cancelConnectTask();
        closeSocket();
        super.onStop();
    }

    protected void closeSocket() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                mSocket = null;
                e.printStackTrace();
            }
        }
    }

    protected void cancelConnectTask() {
        if (mConnectTask != null) {
            mConnectTask.cancel(true);
            mConnectTask = null;
        }
    }


    public void connectDevice(BluetoothDevice device, int taskType) {
        if (device != null) {
            mConnectTask = new ConnectBluetoothTask(taskType).execute(device);
        }
    }

    class ConnectBluetoothTask extends AsyncTask<BluetoothDevice, Integer, BluetoothSocket> {
        int mTaskType;

        public ConnectBluetoothTask(int taskType) {
            this.mTaskType = taskType;
        }

        @Override
        protected void onPreExecute() {
            myShowProgressDialog("请稍候...");
            super.onPreExecute();
        }

        @Override
        protected BluetoothSocket doInBackground(BluetoothDevice... params) {
            if (mSocket != null) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mSocket = bluetoothUtil.connectDevice(params[0]);
            //打印
            if (mTaskType == 6) {
                doPrintIcon(mSocket);
            }
            return mSocket;
        }

        @Override
        protected void onPostExecute(BluetoothSocket socket) {
            progressBar.dismiss();
            if (socket == null || !socket.isConnected()) {
                toast("连接打印机失败");
            } else {
                toast("成功！");
            }
            super.onPostExecute(socket);
        }
    }

    /**
     * 吐司提示
     *
     * @param message
     */
    protected void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case 0:
                break;
            case 1:
                break;
            default:
                break;
        }
    }

    /**
     * 查找蓝牙设备
     */
    private void selectBluetooth() {
        bluetoothUtil.getAdapter().startLeScan(new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (device != null) {
                            if (!TextUtils.isEmpty(device.getName())) {
                            }
                        }
                    }
                });
            }
        });
    }

}
