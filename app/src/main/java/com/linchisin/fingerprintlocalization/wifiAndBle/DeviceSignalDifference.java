package com.linchisin.fingerprintlocalization.wifiAndBle;

/**
 * Created by Sin on 2018/4/26.
 */

/*
设备信号差异类
y=ax+bWifi,
记录a,b值
 */

public class DeviceSignalDifference {
	public float aWifi;
	public float bWifi;
	public float aBle;
	public float bBle;

	public DeviceSignalDifference(float aWifi, float bWifi, float aBle, float bBle){
		this.aWifi = aWifi;
		this.bWifi = bWifi;
		this.aBle=aBle;
		this.bBle=bBle;
	}
}
