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
 * Created by Sin on 2018/4/19.
 */

/**
 * 方向传感器
 */

/**
 * 2018/5/3号
 * 0.
 * 改进方向变化过快的问题
 */

public class OrientSensor implements SensorEventListener {

	private static final String TAG = "OrientSensor";
	private SensorManager sensorManager;
	private OrientCallBack orientCallBack;
	private Context context;
	private float[] accelerometerValues = new float[3];
	private float[] magneticValues = new float[3];
	private List<Integer> orientRecords =new ArrayList<>(); //记录一段时间内通过磁场变化计算出来的角度，最终返回平均值
	private Timer orientTimer;
	private TimerTask orientTimerTask;
	private static final long ORIENT_CALCULATE_PERIOD =100; //计算平均航向
	private static final Boolean ORIENT_LOCK =true; //航向锁

	public OrientSensor(Context context, OrientCallBack orientCallBack) {
		this.context = context;
		this.orientCallBack = orientCallBack;
	}

	public interface OrientCallBack {
		/**
		 * 方向回调
		 */
		void getOrient(int orient);
	}

	/**
	 * 注册加速度传感器和地磁场传感器
	 * @return 是否支持方向功能
	 */
	public Boolean registerOrient() {
		Boolean isAvailable = true;
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

		// 注册加速度传感器
		if (sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST)) {
			Log.i(TAG, "加速度传感器可用！");
		} else {
			Log.i(TAG, "加速度传感器不可用！");
			isAvailable = false;
		}

		// 注册地磁场传感器
		if (sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_FASTEST)) {
			Log.i(TAG, "地磁传感器可用！");
		} else {
			Log.i(TAG, "地磁传感器不可用！");
			isAvailable = false;
		}
		//当且仅当传感器可用时
		if(isAvailable){
			//开始重复计算平均航向定时任务
			orientTimerTask=new TimerTask() {
				@Override
				public void run() {
					synchronized (ORIENT_LOCK) {
						int averageOrient=0;
						int size=orientRecords.size();
                        if(size>0){
                            //计算平均航向
                            for (int i = 0; i <size ; i++) {
                                averageOrient+=orientRecords.get(i);
                            }
                            averageOrient/=size;
                            if(averageOrient<0) averageOrient+=360;  //保证航向为正
                            //计算平均航向后回调
                            Message message=new Message();
                            message.arg1=averageOrient;
//                            MainActivity.mainactivity.orientHandler.sendMessage(message);
//					orientCallBack.getOrient(averageOrient);  //无法在计时器线程中更改UI线程
                            //清空记录
                            orientRecords.clear();
                        }
					}
				}
			};
			orientTimer=new Timer();
			orientTimer.scheduleAtFixedRate(orientTimerTask, ORIENT_CALCULATE_PERIOD, ORIENT_CALCULATE_PERIOD);
		}

		return isAvailable;
	}

	/**
	 * 注销方向监听器
	 */
	public void unregisterOrient() {
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			accelerometerValues = event.values.clone();
		} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			magneticValues = event.values.clone();
		}
		//航向计算锁
		synchronized (ORIENT_LOCK) {
			float[] R = new float[9];
			float[] values = new float[3];
			SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticValues);
			SensorManager.getOrientation(R, values);
			int degree = (int) Math.toDegrees(values[0]);//旋转角度
			orientRecords.add(degree);
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
}
