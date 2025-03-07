package com.linchisin.fingerprintlocalization.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.net.wifi.ScanResult;

import com.linchisin.fingerprintlocalization.ui.MainActivity;
import com.linchisin.fingerprintlocalization.wifiAndBle.BleScanResult;
import com.linchisin.fingerprintlocalization.wifiAndBle.WifiAndBleDataManager;
import com.linchisin.fingerprintlocalization.wifiAndBle.WifiDataManager;

/**
 * @author jiangqideng@163.com
 * @date 2016-6-29 上午12:54:09
 * @description 几个共用的函数
 */
public class TransformUtil {
	private static final String TAG="TransformUtil: ";

	public List<Float> wifiScanResults2List(List<ScanResult> scanResults, HashMap<String, Integer> bssidsExcel) {
		List<Float>rssScan=new ArrayList<>();
		int bsscount = 0;   //检测到的存在于指纹库中的AP数目
		if (scanResults != null) {
			// 初始化
			for (int i = 0; i < bssidsExcel.size(); i++) {
				rssScan.add(-199f);
			}
			for (int j = 0; j < scanResults.size(); j++) {
				String bssid = scanResults.get(j).BSSID;
				if (bssidsExcel.containsKey(bssid)) {   //如没有检测到已有指纹，则默认指纹对应的信号量为-199
					int idx = bssidsExcel.get(bssid);
					rssScan.set(idx,(float)scanResults.get(j).level);
					bsscount++;
				}
			}
		}
		WifiDataManager.getInstance().isNormal = bsscount >= MainActivity.WIFI_BLE_NUM_MIN;
		return rssScan;
	}

	/*
	将wifi及蓝牙扫描数据转换为链表
	 */
	public List<Float> scanResults2List(List<ScanResult> wifiScanResults, List<BleScanResult> bleScanResults, HashMap<String, Integer> bssidsExcel) {
		List<Float>rssScan=new ArrayList<>();
		int bsscount = 0;   //检测到的存在于指纹库中的AP数目

		if (!(wifiScanResults == null&&bleScanResults==null)) {
			// 初始化
			for (int i = 0; i < bssidsExcel.size(); i++) {
				rssScan.add(-100f);
			}
			//先读取wifi数据
			for (int j = 0; j < wifiScanResults.size(); j++) {
				String bssid = wifiScanResults.get(j).BSSID;
				if (bssidsExcel.containsKey(bssid)) {   //如没有检测到已有指纹，则默认指纹对应的信号量为-999
					int idx = bssidsExcel.get(bssid);
					rssScan.set(idx,(float)wifiScanResults.get(j).level);
					bsscount++;
				}
			}
			//再读取蓝牙数据
			for (int j = 0; j < bleScanResults.size(); j++) {
				int minor = bleScanResults.get(j).minor;
				String bssid=String.valueOf(minor);
				if (bssidsExcel.containsKey(bssid)) {   //如没有检测到已有指纹，则默认指纹对应的信号量为-999
					int idx = bssidsExcel.get(bssid);
					rssScan.set(idx,(float)bleScanResults.get(j).rssi);
					bsscount++;
				}
			}
		}
		WifiAndBleDataManager.getInstance().isNormal = bsscount >= MainActivity.WIFI_BLE_NUM_MIN;
		return rssScan;
	}


}
