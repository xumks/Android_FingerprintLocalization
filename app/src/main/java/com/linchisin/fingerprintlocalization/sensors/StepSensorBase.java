package com.linchisin.fingerprintlocalization.sensors;

import android.content.Context;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.linchisin.fingerprintlocalization.util.SensorUtil;

/**
 * 计步传感器抽象类，子类分为加速度传感器、或计步传感器
 */
public abstract class StepSensorBase implements SensorEventListener {
    private Context context;
    protected StepCallBack stepCallBack;
    protected SensorManager sensorManager;
    protected static int CURRENT_SETP = 0;
    protected static double CURRENT_SL = 0;
    protected static double CURRENT_h=0;
    protected boolean isAvailable = false;

    public StepSensorBase(Context context, StepCallBack stepCallBack) {
        this.context = context;
        this.stepCallBack = stepCallBack;
    }

    public interface StepCallBack {
        /**
         * 计步回调
         */
        void getStep(int stepNum, double stepLength, double height);

    }

    /**
     * 开启计步
     */
    public boolean registerStep() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            sensorManager = null;
        }
//        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorManager = SensorUtil.getInstance().getSensorManager(context);
        registerStepListener();
        return isAvailable;
    }

    /**
     * 注册计步监听器
     */
    protected abstract void registerStepListener();

    /**
     * 注销计步监听器
     */
    public abstract void unregisterStep();
}
