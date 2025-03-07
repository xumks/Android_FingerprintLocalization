package com.linchisin.fingerprintlocalization.wifiAndBle;

public class BleScanResult {
    public int minor;
    public int rssi;


    public BleScanResult(int minor,int rssi){
        this.minor=minor;
        this.rssi=rssi;
    }
}
