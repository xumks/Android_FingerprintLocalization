package com.linchisin.fingerprintlocalization.map;

/**
 * Created by Sin on 2018/4/24.
 */

/**
 *
 * @author LinChiSin
 * @date 2018-4-24 下午17:50:24
 * @description 指纹库文件管理
 */

public class FingerprintMap {
	//指纹库

	public String macFilename;  //指纹库 Mac表 文件名
	public String fingerprintFilename;  //指纹库RP表，AP排列顺序按照Mac表排列

	FingerprintMap(String fingerprintFilename, String macFilename){
		this.fingerprintFilename=fingerprintFilename;
		this.macFilename=macFilename;
	}

}
