package com.linchisin.fingerprintlocalization.wifiAndBle;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.linchisin.fingerprintlocalization.algorithms.WKNNAlgorithm;
import com.linchisin.fingerprintlocalization.map.FingerprintMapModel;
import com.linchisin.fingerprintlocalization.ui.MainActivity;
import com.linchisin.fingerprintlocalization.util.TransformUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author LinChiSin
 * @date 2018-5-12 下午 11：40:31
 * @description  统一管理wifi和蓝牙扫描数据
 */

public class WifiAndBleDataManager {

    public static final String TAG="WifiAndBleDataManager:";
    private static final long SCAN_FREQUENCY = 10;  //扫描频率
    private static final long SCAN_PERIOD = 1000/ SCAN_FREQUENCY; //扫描周期
    private static final long LOCALIZATION_FREQUENCY =2;  //定位频率
    private static final long LOCALIZATION_PERIOD = 1000/ LOCALIZATION_FREQUENCY; //定位周期
    private static final int WIFI_AP_NUM =36;  //wifi AP数目
    private static final int BLE_AP_NUM =0;  //蓝牙 AP数目

    private WifiManager wifiManager;
    public List<ScanResult> wifiScanResults ;   //wifi当次扫描数据
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bleScanner;
    private ScanSettings bleSet;
    private LeDeviceListAdapter leDeviceListAdapter;
    public List<BleScanResult>bleScanResults;   //蓝牙当次扫描数据

    //线程池
    public ScheduledExecutorService scheduledExecutorService;

    public float rssScan[]; //扫描固定时间后的平均大小
    public List<Float>scanResultsList=null;   //记录wifi及蓝牙当次扫描数据
    public List<List<Float>>allRssScan=new ArrayList<>(); //记录wifi及蓝牙一定时间内扫描数据
    public boolean isNormal = true;  //判断检测AP数目是否符合最小数目要求
    public int locateNum=0;  //记录定位次数
    public String deviceModel; //设备型号
    public boolean isExistedInDeviceDiffTable;  //记录该设备是否在设备差异性表中已有记录；

    public HashMap<String,Integer> bssids= FingerprintMapModel.getInstance().bssidsExcel;  //MAC哈希表
    public HashMap<String,DeviceSignalDifference> deviceDiff= FingerprintMapModel.getInstance().deviceDiffExcel;  //设备差异性哈希表
    private static final Boolean SCAN_LOCK =true;

    private  static WifiAndBleDataManager instance = null;

    //获取蓝牙回调函数；
    private ScanCallback bleScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice bleDevice = result.getDevice();
            int rssi = result.getRssi();
            byte[] scanData = result.getScanRecord().getBytes();
            final iBeaconClass.iBeacon ibeacon = iBeaconClass.fromScanData(bleDevice,rssi,scanData);
            leDeviceListAdapter.addDevice(ibeacon);
        }
    };
//    private BluetoothAdapter.LeScanCallback leScanCallback=new BluetoothAdapter.LeScanCallback() {
//        @Override
//        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//            final iBeaconClass.iBeacon iBeacon=iBeaconClass.fromScanData(device,rssi,scanRecord);
//            leDeviceListAdapter.addDevice(iBeacon);
//        }
//    };

    //私有化构造函数
    private WifiAndBleDataManager(){
    }

    //单例构造
    public static WifiAndBleDataManager getInstance(){
        synchronized (WifiAndBleDataManager.class){
            if(instance==null){
                instance=new WifiAndBleDataManager();
            }
            return instance;
        }
    }

    //初始化Wifi及蓝牙
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void initWifiAndBle(){

        //初始化Wifi
        wifiManager = (WifiManager) MainActivity.mainactivity.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        Log.i(TAG, "initWifi: WiFi Manager被创建："+wifiManager.toString());
//        Toast.makeText(MainActivity.mainactivity, "正在开启WiFi...", Toast.LENGTH_SHORT).show();
        wifiManager.setWifiEnabled(true);

        //获取蓝牙服务
        if (!MainActivity.mainactivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(MainActivity.mainactivity,"蓝牙不可用！",Toast.LENGTH_SHORT).show();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) MainActivity.mainactivity.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bleScanner = bluetoothAdapter.getBluetoothLeScanner();
        bleSet = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        if (bluetoothAdapter == null) {
            Toast.makeText(MainActivity.mainactivity,"蓝牙不可用！",Toast.LENGTH_SHORT).show();
            return;
        }

        //初始化
        leDeviceListAdapter=new LeDeviceListAdapter(MainActivity.mainactivity);
        bluetoothAdapter.enable();

        //记录设备型号
        deviceModel= Build.MODEL;
        Log.i(TAG, "initWifiAndBle: 设备型号为："+deviceModel);
        isExistedInDeviceDiffTable=deviceDiff.containsKey(deviceModel);
    }


    private Runnable runnableScan =new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            synchronized (SCAN_LOCK) {   //使用线程同步，确保allRssScan在定位过程中不发生变化
                //扫描wifi
                Log.i(TAG, "run: WiFi开始扫描");
                wifiManager.startScan();
                wifiScanResults = wifiManager.getScanResults();
                Log.i(TAG, "run: 扫描到的wifi数目为:"+ wifiScanResults.size());

                //扫描蓝牙
                Log.i(TAG, "run: 蓝牙开始扫描");
//                bluetoothAdapter.startLeScan(leScanCallback);

                bleScanner.startScan(null,bleSet,bleScanCallback);
                Log.i(TAG, "run: 周围蓝牙数目为："+leDeviceListAdapter.getCount());
                bleScanResults=new ArrayList<BleScanResult>();
                for (int i = 0; i < leDeviceListAdapter.getCount(); i++) {
                    iBeaconClass.iBeacon device=leDeviceListAdapter.getDevice(i);
                    bleScanResults.add(new BleScanResult(device.minor,device.rssi));
                }
                scanResultsList=new TransformUtil().scanResults2List(wifiScanResults,bleScanResults,bssids);
                allRssScan.add(scanResultsList);
            }
        }
    };

    private Runnable runnableLocalization =new Runnable() {
        @Override
        public void run() {
            if (isNormal) { //当且仅当wifi及蓝牙工作正常时才定位
                synchronized (SCAN_LOCK) {  //使用线程同步，确保allRssScan在定位过程中不发生变化
                    rssScan = new float[bssids.size()];
                    for (int i = 0; i < rssScan.length; i++) {
                        float sum = 0f;
                        int listSize = allRssScan.size();
                        for (int j = 0; j < listSize; j++) {
                            sum += allRssScan.get(j).get(i);
                        }
                        rssScan[i] = sum / listSize;
                    }

                    //消除设备差异性
//                    rssScan = eliminateDeviceDiff(rssScan, deviceDiff);

                    allRssScan.clear();
                    locateNum++;
//                    new KNNLocalization().start();
                    new WKNNAlgorithm().start();
                }
            }
        }
    };

    /*
    同时整合WiFi扫描与定位结算
     */
    public void setRunnableScanAndRunnableLocalization(){
        scheduledExecutorService= Executors.newScheduledThreadPool(10);
        scheduledExecutorService.scheduleAtFixedRate(runnableScan,0,SCAN_PERIOD, TimeUnit.MILLISECONDS);
        scheduledExecutorService.scheduleAtFixedRate(runnableLocalization,LOCALIZATION_PERIOD,LOCALIZATION_PERIOD, TimeUnit.MILLISECONDS);
    }


    /*
    利用设备差异性表，消除设备差异性
    */
    public float[] eliminateDeviceDiff(float[]rssScan, HashMap<String,DeviceSignalDifference>deviceDiff){
        if(isExistedInDeviceDiffTable){
            /*
            wifi设备差异性
             */
            float aWifi=deviceDiff.get(deviceModel).aWifi;
            float bWifi=deviceDiff.get(deviceModel).bWifi;
            for (int i = 0; i < WIFI_AP_NUM; i++) {
                rssScan[i]=rssScan[i]*aWifi+bWifi;
            }
            /*
            蓝牙设备差异性
             */
            float aBle=deviceDiff.get(deviceModel).aBle;
            float bBle=deviceDiff.get(deviceModel).bBle;
            for (int i = 0; i < BLE_AP_NUM; i++) {
                rssScan[i+WIFI_AP_NUM]=rssScan[i+WIFI_AP_NUM]*aBle+bBle;
            }
        }
        return rssScan;
    }


}
