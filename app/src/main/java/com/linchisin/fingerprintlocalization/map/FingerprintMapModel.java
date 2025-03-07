package com.linchisin.fingerprintlocalization.map;

import android.util.Log;

import java.util.HashMap;

import com.linchisin.fingerprintlocalization.file.FileReaderManager;
import com.linchisin.fingerprintlocalization.wifiAndBle.DeviceSignalDifference;

/**
 * @author jiangqideng@163.com
 * @date 2016-6-29 上午12:50:59
 * @description 加载Radio Map，包括bssid，长度、个数
 *
 * @author LinChiSin
 * @date 2018-4-21 上午10:30:24
 * @description 删除利用TXT建立指纹库的原始方法，改为直接读取Excel文件
 */


public class FingerprintMapModel {
	public float radioMapExcel[][];  //从Excel表中获取的指纹库
	public HashMap<String,Integer> bssidsExcel;  //从Excel表中获取的MAC表(哈希表)
	public HashMap<String,DeviceSignalDifference> deviceDiffExcel;  //从Excel表中获取的设备差异性表(哈希表)

	private FingerprintMapModel(){}  //懒汉式单例设计模式

	private volatile static FingerprintMapModel fingerprintMapModel = null;
	private static String TAG = "com.linchisin.fingerprintlocalization.map";

	public static FingerprintMapModel getInstance() {
		if (fingerprintMapModel == null) {
			synchronized (FingerprintMapModel.class) {
				if (fingerprintMapModel == null) {
					fingerprintMapModel = new FingerprintMapModel();
				}
			}
		}
		return fingerprintMapModel;
	}

	public void init(FingerprintMap fingerprintMap) {
		radioMapExcel=new FileReaderManager().getRadioMapFromExcel(fingerprintMap.fingerprintFilename);  //获取RadioMap
		Log.d(TAG, "macFileName :"+fingerprintMap.macFilename);
		bssidsExcel =new FileReaderManager().getBssidFromExcel(fingerprintMap.macFilename); //获取对应的Mac地址表
		deviceDiffExcel =new FileReaderManager().getDeviceDiffFromExcel("DeviceDiff3.xls");
	}

}
