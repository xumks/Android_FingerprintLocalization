package com.linchisin.fingerprintlocalization.wifiAndBle;



import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.linchisin.fingerprintlocalization.map.FingerprintMapModel;
import com.linchisin.fingerprintlocalization.ui.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BleDataManager {

    public static final String TAG="BleDataManager:";
    public static final long BLE_SCAN_FREQUENCY =50;  //蓝牙扫描频率
    public static final long BLE_SCAN_PERIOD = 1000/ BLE_SCAN_FREQUENCY; //蓝牙扫描周期
    private BluetoothAdapter bluetoothAdapter;
    private LeDeviceListAdapter leDeviceListAdapter;
    private List<iBeaconClass.iBeacon>leDevices=new ArrayList<>();
    public List<BleScanResult>bleScanResults;


    private Timer bleScanTimer;  //蓝牙扫描周期
    private TimerTask bleScanTimerTask;
    public float rssScan[]; //蓝牙扫描固定时间后的平均大小
    public List<Float>scanResultsList=null;
    public List<List<Float>>allRssScan=new ArrayList<>();
    public boolean isNormal = true;  //判断检测AP数目是否符合最小数目要求
    public int locateNum=0;  //记录定位次数
    public String deviceModel; //设备型号
    public boolean isExistedInDeviceDiffTable;  //记录该设备是否在设备差异性表中已有记录；

    public HashMap<String,Integer> bssids= FingerprintMapModel.getInstance().bssidsExcel;  //MAC哈希表
    public HashMap<String,DeviceSignalDifference> deviceDiff= FingerprintMapModel.getInstance().deviceDiffExcel;  //设备差异性哈希表


    private  static BleDataManager instance = null;

    //获取回调函数；
    private BluetoothAdapter.LeScanCallback leScanCallback=new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            final iBeaconClass.iBeacon iBeacon=iBeaconClass.fromScanData(device,rssi,scanRecord);
            leDeviceListAdapter.addDevice(iBeacon);
        }
    };

    private BleDataManager(){
        //初始化
        leDeviceListAdapter=new LeDeviceListAdapter(MainActivity.mainactivity);
        bluetoothAdapter.enable();
    }
    //单例构造
    public static BleDataManager getInstance(){
        synchronized (BleDataManager.class){
            if(instance==null){
                instance=new BleDataManager();
            }
            return instance;
        }
    }

    //初始化蓝牙
    public void initBle(){

        //获取蓝牙服务
        if (!MainActivity.mainactivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(MainActivity.mainactivity,"蓝牙不可用！",Toast.LENGTH_SHORT).show();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) MainActivity.mainactivity.getSystemService(Context.BLUETOOTH_SERVICE);
       bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(MainActivity.mainactivity,"蓝牙不可用！",Toast.LENGTH_SHORT).show();
            return;
        }
        deviceModel= Build.MODEL;
        Log.i(TAG, "init: 设备型号为："+deviceModel);
        isExistedInDeviceDiffTable=deviceDiff.containsKey(deviceModel);
    }

    // 设置Timer任务，开始扫描蓝牙
    public void startScanBle() {

        bleScanTimer = new Timer();
        bleScanTimerTask = new TimerTask() {
            public void run() {
                Log.i(TAG, "run: 蓝牙开始扫描");
                bluetoothAdapter.startLeScan(leScanCallback);
                Log.i(TAG, "run: 周围蓝牙数目为："+leDeviceListAdapter.getCount());
                bleScanResults=new ArrayList<>();
                for (int i = 0; i < leDeviceListAdapter.getCount(); i++) {
                    iBeaconClass.iBeacon device=leDeviceListAdapter.getDevice(i);
                    bleScanResults.add(new BleScanResult(device.minor,device.rssi));
                }
            }
        };
        bleScanTimer.schedule(bleScanTimerTask, 0, BLE_SCAN_PERIOD);
    }





}
