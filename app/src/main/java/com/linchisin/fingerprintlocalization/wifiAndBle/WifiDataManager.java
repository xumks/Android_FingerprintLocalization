package com.linchisin.fingerprintlocalization.wifiAndBle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.linchisin.fingerprintlocalization.map.FingerprintMapModel;
import com.linchisin.fingerprintlocalization.algorithms.KNNLocalization;
import com.linchisin.fingerprintlocalization.ui.MainActivity;
import com.linchisin.fingerprintlocalization.util.TransformUtil;

/**
 * @author jiangqideng@163.com
 * @date 2016-6-29 上午12:52:40
 * @description WiFi扫描数据的管理
 */

public class WifiDataManager {

	public static final long WIFI_SCAN_FREQUENCY=50;  //WIFI扫描频率
	public static final long WIFI_SCAN_PERIOD = 1000/WIFI_SCAN_FREQUENCY; //WIFI扫描周期
	public static final long LOCALIZAITON_FREQUENCY=2;  //WIFI扫描频率
	public static final long LOCALIZATION_PERIOD = 1000/LOCALIZAITON_FREQUENCY; //WIFI扫描周期
	public static final String TAG="WifiDataManager:";
	private WifiManager wifiManager;
	public List<ScanResult> scanResults = null;


	//线程池
	public ScheduledExecutorService scheduledExecutorService;

	public  float rssScan[];  //WiFi扫描固定时间后的平均大小
	public List<Float>scanResultsList=null;
	public List<List<Float>>allRssScan=new ArrayList<>();
	public boolean isNormal = true;  //判断检测AP数目是否符合最小数目要求
	public int locateNum=0;  //记录定位次数
	public String deviceModel; //设备型号
	public boolean isExistedInDeviceDiffTable;  //记录该设备是否在设备差异性表中已有记录；

	public HashMap<String,Integer> bssids= FingerprintMapModel.getInstance().bssidsExcel;  //MAC哈希表
	public HashMap<String,DeviceSignalDifference> deviceDiff= FingerprintMapModel.getInstance().deviceDiffExcel;  //设备差异性哈希表
	private static final Boolean WIFI_LOCK =true;

	private volatile static WifiDataManager wifiDataManager = null;

	public static WifiDataManager getInstance() {
		if (wifiDataManager == null) {
			synchronized (WifiDataManager.class) {
				if (wifiDataManager == null) {
					wifiDataManager = new WifiDataManager();
				}
			}
		}
		return wifiDataManager;
	}

	// 初始化WIFI，开启WiFi,并记录设备型号
	public void initWifi() {
		wifiManager = (WifiManager) MainActivity.mainactivity.getApplicationContext()
				.getSystemService(Context.WIFI_SERVICE);
		Log.i(TAG, "initWifi: WiFi Manager被创建："+wifiManager.toString());
		Toast.makeText(MainActivity.mainactivity, "正在开启WiFi...",
				Toast.LENGTH_SHORT).show();
		wifiManager.setWifiEnabled(true);
		deviceModel= Build.MODEL;
		Log.i(TAG, "init: 设备型号为："+deviceModel);
		isExistedInDeviceDiffTable=deviceDiff.containsKey(deviceModel);
	}


	/*
	将Timer及TimerTask更改为ScheduledExecutorService
	 */
	private Runnable runnableScanWifi =new Runnable() {
		@Override
		public void run() {
			synchronized (WIFI_LOCK) {   //使用线程同步，确保allRssScan在定位过程中不发生变化
				wifiManager.startScan();
				scanResults = wifiManager.getScanResults();
				Log.i(TAG, "onReceive: 扫描到的wifi数目为:"+scanResults.size());
				scanResultsList=new TransformUtil().wifiScanResults2List(scanResults,bssids);
				allRssScan.add(scanResultsList);
			}
		}
	};

	private Runnable runnableLocalization =new Runnable() {
		@Override
		public void run() {
			if (isNormal) { //当且仅当wifi工作正常时才定位
				synchronized (WIFI_LOCK) {  //使用线程同步，确保allRssScan在定位过程中不发生变化
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
					rssScan = eliminateDeviceDiff(rssScan, deviceDiff);
					allRssScan.clear();
					locateNum++;
					new KNNLocalization().start();
				}
			}
		}
	};

	/*
	同时整合WiFi扫描与定位结算
	 */
	public void setRunnableScanWifiAndRunnableLocalization(){
		scheduledExecutorService= Executors.newScheduledThreadPool(10);
		scheduledExecutorService.scheduleAtFixedRate(runnableScanWifi,0,WIFI_SCAN_PERIOD, TimeUnit.MILLISECONDS);
		scheduledExecutorService.scheduleAtFixedRate(runnableLocalization,LOCALIZATION_PERIOD,LOCALIZATION_PERIOD, TimeUnit.MILLISECONDS);
	}

	/*
	利用设备差异性表，消除设备差异性
	 */
	public float[] eliminateDeviceDiff(float[]rssScan, HashMap<String,DeviceSignalDifference>deviceDiff){
		if(isExistedInDeviceDiffTable){
			float a=deviceDiff.get(deviceModel).aWifi;
			float b=deviceDiff.get(deviceModel).bWifi;
			for (int i = 0; i < rssScan.length; i++) {
				rssScan[i]=rssScan[i]*a+b;
			}
		}
		return rssScan;
	}


}




