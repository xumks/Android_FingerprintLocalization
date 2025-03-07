package com.linchisin.fingerprintlocalization.sensors;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Message;
import android.util.Log;

import com.linchisin.fingerprintlocalization.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 气压传感器
 */

public class BarometerSensor implements SensorEventListener {

    private static final String TAG="BarometerSensor: ";
    private SensorManager sensorManager;
    private Context context;
    private List<Float>airPressureRecords=new ArrayList<>(); //记录气压
    private Timer barometerTimer;
    private TimerTask barometerTimerTask;
    private static final long HEIGHT_CALCULATE_PERIOD=100; //计算平均高度
    private static final long HEIGHT_CALCULATE_DALAY=1000; //延迟高度计算时间
    private static final Boolean HEIGHT_LOCK=true; //高度计算锁
    private static final float INITIAL_HEIGHT=12f; //默认初始高度
//    private static final float INITIAL_AIR_PRESSURE=1014.3111f; //默认初始楼层的气压
    private List<Float> initialPressureRecords= new ArrayList<>(); //记录初始气压

    private int k=0; //记录采样次数，用于抛弃前1秒的数据
    public float height;


    //构造函数
    public BarometerSensor (Context context){
        this.context=context;
    }



    /**
     * 注册气压计
     * @return 是否支持气压计
     */

    public boolean registerBarometer(){
        boolean isAvailable =false;
        sensorManager= (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        //注册气压计
        //SENSOR_DELAY_GAME 频率为：
        if(sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),SensorManager.SENSOR_DELAY_FASTEST)){
            Log.i(TAG, "registerBarometer: 气压计可用！");
            isAvailable=true;
        }else {
            Log.i(TAG, "registerBarometer: 气压计不可用！");

        }
        //当且仅当气压计可用时，开始周期性计算高度流程
        if(isAvailable){
            barometerTimerTask=new TimerTask() {
                @Override
                public void run() {
                    synchronized (HEIGHT_LOCK){
                        if(k>=50){
                            Log.i(TAG, "run: 进入计算平均气压：");
                            float averageAirPressure=0f;
                            int size=airPressureRecords.size();
                            for (int i = 0; i < size; i++) {
                                averageAirPressure+=airPressureRecords.get(i);
                            }
                            averageAirPressure/=size;
                            Log.i(TAG, "run: 平均气压为："+averageAirPressure);
                            float initialPressure=0;
                            for(float pressure:initialPressureRecords){
                                initialPressure+=pressure;
                            }
                            initialPressure/=initialPressureRecords.size();
                            height=getHeightFromPressure(averageAirPressure,initialPressure,INITIAL_HEIGHT);
                            Log.i(TAG, "run: 计算高度为:"+height);
                            Message message=new Message();
                            message.obj=height;
//                            MainActivity.mainactivity.heightHandler.sendMessage(message);
                            airPressureRecords.clear();
                        }
                    }
                }
            };
            barometerTimer=new Timer();
            barometerTimer.scheduleAtFixedRate(barometerTimerTask,HEIGHT_CALCULATE_DALAY,HEIGHT_CALCULATE_PERIOD);
        }
        return isAvailable;
    }

    /*
     利用气压计算高度
     */
    public float getHeightFromPressure(float airPressure,float initialAirPressure,float initialHeight){
        //转换公式的几个参数
        float g=9.8f;
        float rd=287.05287f;
        float k=273.15f;
        float temperature=24+k;
        //显示一位小数
        float height=initialHeight+((rd*temperature)/g)*(float) Math.log(initialAirPressure/airPressure);
        return (Math.round(height*10)/10f);
    }

    /*
     注销气压监听器
     */

    public void unregisterBarometer() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType()==Sensor.TYPE_PRESSURE){
            //抛弃传感器初始化过程中的异常数据，这里设置为抛弃前30个采样点
            //将第30到第50个采样点设置为初始气压
            if(k>=30){
                synchronized (HEIGHT_LOCK) {
                    if(k<=50){
                        Log.i(TAG, "onSensorChanged: 添加气压，当前气压值："+sensorEvent.values[0]);
                        airPressureRecords.add(sensorEvent.values[0]);
                        initialPressureRecords.add(sensorEvent.values[0]);
                        k++;
                    }else{
                        Log.i(TAG, "onSensorChanged: 添加气压，当前气压值："+sensorEvent.values[0]);
                        airPressureRecords.add(sensorEvent.values[0]);
                    }
                }
            }else{
                k++;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
