package com.linchisin.fingerprintlocalization.sensors;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
/**
 * 加速度传感器
 */
public class StepSensorAcceleration extends StepSensorBase {
    private final String TAG = "StepSensorAcceleration";
    double acc_z, h_betweenPeaks,SL;
    ArrayList<Double> accz_betweenPeaks =new ArrayList<Double>();

    //存放三轴数据
    final int valueNum = 5;
    //用于存放计算阈值的波峰波谷差值
    float[] tempValue = new float[valueNum];
    ///////////////////////用于存放两次波峰之间的z轴加速度

    int tempCount = 0;
    //是否上升的标志位
    boolean isDirectionUp = false;
    //持续上升次数
    int continueUpCount = 0;
    //上一点的持续上升的次数，为了记录波峰的上升次数
    int continueUpFormerCount = 0;
    //上一点的状态，上升还是下降
    boolean lastStatus = false;
    //波峰值
    float peakOfWave = 0;
    //波谷值
    float valleyOfWave = 0;
    /////////////////////////////此次波峰的时间
    long timeOfThisPeak = 0;
    /////////////////////////////上次波峰的时间
    long timeOfLastPeak = 0;
    //当前的时间
    long timeOfNow = 0;
    //当前传感器的值
    float gravityNew = 0;
    //上次传感器的值
    float gravityOld = 0;
    //动态阈值需要动态的数据，这个值用于这些动态数据的阈值
    final float initialValue = (float) 1.7;
    //初始阈值
    float ThreadValue = (float) 2.0;
    //初始范围
    float minValue = 11f;
    float maxValue = 19.6f;
    /**
     * 0-准备计时   1-计时中  2-正常计步中
     */
    private int CountTimeState = 0;
    public static int TEMP_STEP = 0;
    private int lastStep = -1;
    //用x、y、z轴三个维度算出的平均值
    public static float average = 0;
    private Timer timer;
    // 倒计时3.5秒，3.5秒内不会显示计步，用于屏蔽细微波动
    private long duration = 3500;
    private TimeCount time;

    public StepSensorAcceleration(Context context, StepCallBack stepCallBack) {
        super(context, stepCallBack);
    }
    @Override
    protected void registerStepListener() {
        // 注册加速度传感器
        isAvailable = sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
        if (isAvailable) {
            Log.i(TAG, "加速度传感器可用！");
        } else {
            Log.i(TAG, "加速度传感器不可用！");
        }
    }
    @Override
    public void unregisterStep() {
        sensorManager.unregisterListener(this);
    }
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        synchronized (this) {
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                calc_step(event);//计算三轴合加速度
            }
        }
    }
    //计算三轴合加速度
    synchronized private void calc_step(SensorEvent event) {
        acc_z = event.values[2];
        average = (float) Math.sqrt(Math.pow(event.values[0], 2)
                + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2));
        accz_betweenPeaks.add(acc_z);//把z轴方向分量存进数组缓存中
        detectorNewStep(average);//average用来检测步频，accz_betweenPeaks用来计算步长
    }
    /*
     * 检测步子，并开始计步
	 * 1.传入sersor中的数据（三轴合加速度）
	 * 2.如果检测到了波峰，并且符合时间差以及阈值的条件，则判定为1步
	 * 3.符合时间差条件，波峰波谷差值大于initialValue，则将该差值纳入阈值的计算中
	 * */
    public void detectorNewStep(float values) {
        double[]vec_betweenPeaks =new double[accz_betweenPeaks.size()];
        //double h_betweenPeaks;
        //double SL;
        if (gravityOld == 0) {
            gravityOld = values;
        } else {
            if (DetectorPeak(values, gravityOld)) {
                timeOfLastPeak = timeOfThisPeak;
                timeOfNow = System.currentTimeMillis();
                if (timeOfNow - timeOfLastPeak >= 200
                        && (peakOfWave - valleyOfWave >= ThreadValue) && (timeOfNow - timeOfLastPeak) <= 2000) {
                    /*
                     * 检测到迈步之后:利用腰部位移模型计算步长
                     * 1.数组acc_TwoPeaks做积分---速度
                     * 2.峰值时刻速度置零
                     * 3.数组vec_TwoPeaks做积分---腰部位移
                     * 4.带入腿长计算步长
                     */

                    System.out.println("Z-ACC:"+accz_betweenPeaks.toString());
                    vec_betweenPeaks=trapz_first();
                    trapz_second(vec_betweenPeaks);/////////////输出腰部重心高度变化

                    SL=2*Math.sqrt(2*1*h_betweenPeaks-h_betweenPeaks*h_betweenPeaks);
                    System.out.println("Z-VEC:"+vec_betweenPeaks.toString());
                    System.out.println("h:"+h_betweenPeaks);
                    System.out.println("SL:"+SL);
                    timeOfThisPeak = timeOfNow;
                    //更新界面的处理，不涉及到算法
                    preStep(SL,h_betweenPeaks);
                    accz_betweenPeaks.clear();
                    h_betweenPeaks=0;
                    SL=0;

                }
                if (timeOfNow - timeOfLastPeak >= 200
                        && (peakOfWave - valleyOfWave >= initialValue)) {
                    timeOfThisPeak = timeOfNow;
                    ThreadValue = Peak_Valley_Thread(peakOfWave - valleyOfWave);
                }
            }
        }
        gravityOld = values;
    }


    synchronized private double calSum(ArrayList<Double> arr,int index){
        double res=0;
        for(int i=0;i<=index;i++){
            res+=arr.get(i);
        }
        return res;
    }
    synchronized private double calSum(double[] arr,int index){
        double res=0;
        for(int i=0;i<=index;i++){
            res+=arr[i];
        }
        return res;
    }
    synchronized private double[] trapz_first(){
        int n=0;
        double []vec_TwoPeaks=new double[accz_betweenPeaks.size()];
        //sensor_delay_game 频率为50Hz左右
        for(;n<accz_betweenPeaks.size();n++){
            vec_TwoPeaks[n]=(calSum(accz_betweenPeaks,n)-(accz_betweenPeaks.get(0)+accz_betweenPeaks.get(n))/2)/50;
        }
        /*for(int j=n;j>n-2;j--) {
            vec_TwoPeaks[j] = 0.0;
        }*/
        return vec_TwoPeaks;
    }
    synchronized private void  trapz_second(double[] vec_betweenPeaks){
        int n=0;
        double []h_TwoPeaks=new double[vec_betweenPeaks.length];
        //sensor_delay_game 频率为50Hz
        for(;n<vec_betweenPeaks.length;n++){
            h_TwoPeaks[n]=(calSum(vec_betweenPeaks,n)-(vec_betweenPeaks[0]+vec_betweenPeaks[n])/2)/50;
        }
        h_betweenPeaks= h_TwoPeaks[n-2];
        h_betweenPeaks=h_betweenPeaks/100;

        //return  h_betweenPeaks;
    }




    private void preStep(double SL,double h_betweenPeaks) {
//        if (CountTimeState == 0) {
//            // 开启计时器
//            time = new TimeCount(duration, 700);
//            time.start();
//            CountTimeState = 1;
//            Log.v(TAG, "开启计时器");
//        } else if (CountTimeState == 1) {
//            TEMP_STEP++;
//            Log.v(TAG, "计步中 TEMP_STEP:" + TEMP_STEP);
//        } else if (CountTimeState == 2) {
        StepSensorBase.CURRENT_SETP++;
        StepSensorBase.CURRENT_SL=SL;
        StepSensorBase.CURRENT_h=h_betweenPeaks;
//            if (stepCallBack != null) {
        stepCallBack.getStep(StepSensorBase.CURRENT_SETP,StepSensorBase.CURRENT_SL,StepSensorBase.CURRENT_h);
//            }
//        }



    }
    /*
     * 检测波峰
     * 以下四个条件判断为波峰：
     * 1.目前点为下降的趋势：isDirectionUp为false
     * 2.之前的点为上升的趋势：lastStatus为true
     * 3.到波峰为止，持续上升大于等于2次
     * 4.波峰值大于1.2g,小于2g
     * 记录波谷值
     * 1.观察波形图，可以发现在出现步子的地方，波谷的下一个就是波峰，有比较明显的特征以及差值
     * 2.所以要记录每次的波谷值，为了和下次的波峰做对比
     * */
    public boolean DetectorPeak(float newValue, float oldValue) {
        lastStatus = isDirectionUp;
        if (newValue >= oldValue) {
            isDirectionUp = true;//上升段
            continueUpCount++;
        } else {
            continueUpFormerCount = continueUpCount;
            continueUpCount = 0;
            isDirectionUp = false;
        }

//        Log.v(TAG, "oldValue:" + oldValue);
        if (!isDirectionUp && lastStatus
                && (continueUpFormerCount >= 2 && (oldValue >= minValue && oldValue < maxValue))) {
            peakOfWave = oldValue;
            return true;
        } else if (!lastStatus && isDirectionUp) {//判定迈出一步
            valleyOfWave = oldValue;
            return false;
        } else {
            return false;
        }
    }
    /*
     * 阈值的计算
     * 1.通过波峰波谷的差值计算阈值
     * 2.记录4个值，存入tempValue[]数组中
     * 3.在将数组传入函数averageValue中计算阈值
     * */
    public float Peak_Valley_Thread(float value) {
        float tempThread = ThreadValue;
        if (tempCount < valueNum) {
            tempValue[tempCount] = value;
            tempCount++;
        } else {
            tempThread = averageValue(tempValue, valueNum);
            for (int i = 1; i < valueNum; i++) {
                tempValue[i - 1] = tempValue[i];
            }
            tempValue[valueNum - 1] = value;
        }
        return tempThread;

    }
    /*
     * 梯度化阈值
     * 1.计算数组的均值
     * 2.通过均值将阈值梯度化在一个范围里
     * */
    public float averageValue(float value[], int n) {
        float ave = 0;
        for (int i = 0; i < n; i++) {
            ave += value[i];
        }
        ave = ave / valueNum;
        if (ave >= 8) {
//            Log.v(TAG, "超过8");
            ave = (float) 4.3;
        } else if (ave >= 7 && ave < 8) {
//            Log.v(TAG, "7-8");
            ave = (float) 3.3;
        } else if (ave >= 4 && ave < 7) {
//            Log.v(TAG, "4-7");
            ave = (float) 2.3;
        } else if (ave >= 3 && ave < 4) {
//            Log.v(TAG, "3-4");
            ave = (float) 2.0;
        } else {
//            Log.v(TAG, "else");
            ave = (float) 1.7;
        }
        return ave;
    }

    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            // 如果计时器正常结束，则开始计步
            time.cancel();
            StepSensorBase.CURRENT_SETP += TEMP_STEP;
            lastStep = -1;
            Log.v(TAG, "计时正常结束");

            timer = new Timer(true);
            TimerTask task = new TimerTask() {
                public void run() {
                    if (lastStep == StepSensorBase.CURRENT_SETP) {
                        timer.cancel();
                        CountTimeState = 0;
                        lastStep = -1;
                        TEMP_STEP = 0;
                        Log.v(TAG, "停止计步：" + StepSensorBase.CURRENT_SETP);
                    } else {
                        lastStep = StepSensorBase.CURRENT_SETP;
                    }
                }
            };
            timer.schedule(task, 0, 2000);
            CountTimeState = 2;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (lastStep == TEMP_STEP) {
                Log.v(TAG, "onTick 计时停止:" + TEMP_STEP);
                time.cancel();
                CountTimeState = 0;
                lastStep = -1;
                TEMP_STEP = 0;
            } else {
                lastStep = TEMP_STEP;
            }
        }
    }
}
