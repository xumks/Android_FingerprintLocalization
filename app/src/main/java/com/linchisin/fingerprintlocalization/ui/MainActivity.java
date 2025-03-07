package com.linchisin.fingerprintlocalization.ui;


import permissions.dispatcher.*;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.linchisin.fingerprintlocalization.R;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.linchisin.fingerprintlocalization.map.FloorMap;
import com.linchisin.fingerprintlocalization.algorithms.Position;
import com.linchisin.fingerprintlocalization.connection.ConnectTimerTask;
import com.linchisin.fingerprintlocalization.file.FileWriterManager;
import com.linchisin.fingerprintlocalization.sensors.BarometerSensor;
import com.linchisin.fingerprintlocalization.sensors.OrientSensor;
import com.linchisin.fingerprintlocalization.wifiAndBle.WifiAndBleDataManager;
import com.linchisin.fingerprintlocalization.sensors.StepSensorAcceleration;
import com.linchisin.fingerprintlocalization.sensors.StepSensorBase;
import com.linchisin.fingerprintlocalization.map.FloorMapManager;
import com.linchisin.fingerprintlocalization.util.SensorUtil;
import com.linchisin.fingerprintlocalization.util.PermissionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import cn.carbswang.android.numberpickerview.library.NumberPickerView;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * @author jiangqideng@163.com
 * @date 2016-6-29 上午12:53:04
 * @description 主界面
 *
 * @author LinChiSin
 * @date 2018-4-19 下午 7:46:02
 * @description 改进地图显示，引入MapView，引入惯导、地磁等传感器，提供航向信息及可能的Wi-Fi融合
 *
 * @author LinChiSin
 * @date 2018-4-20 下午 5:00:22
 * @description 增加Socket通信
 *
 * @author LinChiSin
 * @date 2018-4-28 下午 0:09:17
 * @description 增加记录定位数据，可保存成本地.xls文档
 *
 * @author LinChiSin
 * @date 2018-4-28 下午 0:09:19
 * @description 测试同步问题
 *
 * @author LinChiSin
 * @date 2018-5-12 下午 21:12:34
 * @description 增加蓝牙扫描
 */

@RuntimePermissions
public class MainActivity extends Activity implements
		StepSensorBase.StepCallBack,OrientSensor.OrientCallBack,
		NumberPickerView.OnScrollListener,NumberPickerView.OnValueChangeListener,
		NumberPickerView.OnValueChangeListenerInScrolling {


	private static final int REQUEST_CHANGE_WIFI_STATE = 1;  //权限管理标志位

	/*
	权限管理
	*/
    @NeedsPermission(Manifest.permission.CHANGE_WIFI_STATE)
	void changeWiFi() {
		WifiAndBleDataManager.getInstance().initWifiAndBle();
		Log.i(TAG, "changeWiFi: 允许");
	}
	@OnShowRationale(Manifest.permission.CHANGE_WIFI_STATE)
	void showRationale(final PermissionRequest request) {
		new AlertDialog.Builder(this)
				.setMessage(R.string.permission_change_wifi_rationale)
				.setPositiveButton(R.string.button_allow, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						request.proceed();
					}
				})
				.setNegativeButton(R.string.button_deny, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						request.cancel();
					}
				})
				.show();
	}
	@OnPermissionDenied(Manifest.permission.CHANGE_WIFI_STATE)
	void showDeniedForChangeWiFi() {
		PermissionUtils.showRequestPermissionDialog(this, getString(R.string.permission_change_wifi_denied));
		Toast.makeText(this, R.string.permission_change_wifi_denied, Toast.LENGTH_SHORT).show();
		Log.i(TAG, "changeWiFi: 拒绝");
	}
	@OnNeverAskAgain(Manifest.permission.CHANGE_WIFI_STATE)
	void showNeverAskForChangeWiFi() {
		PermissionUtils.showRequestPermissionDialog(this, getString(R.string.permission_change_wifi_neverask));
		Toast.makeText(this, R.string.permission_change_wifi_neverask, Toast.LENGTH_SHORT).show();
		Log.i(TAG, "changeWiFi: 别再问了");
	}
//	@Override
//	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//		MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
//		Log.i(TAG, "changeWiFi: 来问啦");
//	}


	private static final long CONNECT_PERIOD =1000;  //与服务器传输周期，1000毫米
	public static MainActivity mainactivity;
	private static final String TAG="MainActivity:";
	private FloorMap floorMap= FloorMapManager.getInstance().floorMaps[8]; //默认地图楼层文件，此时默认金华4层
	private ImageSource imageSource = ImageSource.asset(floorMap.name); //主界面地图源，
	private NumberPickerView pickerView;  //楼层选择器
	private ImageView locate; //定位按钮
	private ImageView reset;   // 清空轨迹
	private ImageView connect; //通信
	private ImageView save; //保存数据
	private TextView statusTextView; //WiFi状态
	private TextView resultsTextView; //定位结果
	private TextView stepTextView; //步数信息
	private TextView orientTextView; //航向信息
	private TextView heightTextView; //高度信息
	private TextView connectTextView; //通信信息
	private MapView mMapView;  //地图
	private StepSensorBase mStepSensor; // 计步传感器
	private OrientSensor mOrientSensor; // 方向传感器
	private BarometerSensor mBarometerSensor; //气压计


	private float mStepLength = 0.5f; // 步长,单位为m
	public int mOrient; //航向
	private float mHeight;  //当前高度
	private int floorNum; //楼层号
	private static final float FLOOR_HEIGHT=4f; //楼层高度

    public String pdrResult; //记录单步定位结果
    public Position currentPosition =new Position(0,0,4); //当前最终定位结果（wifi与PDR融合结果）
	public Position currentPdrPosition =new Position(0,0,4); //当前PDR定位结果
	public Position currentWifiPosition =new Position(0,0,4); //当前wifi定位结果

    //记录wifi信息
	public List<Long> wifiAndBleTimeStampRecords =new ArrayList<>();  //记录wifi定位时间戳
	public List<List<Float>> wifiAndBleSignalLevelRecords =new ArrayList<>(); //记录定位信号量
	public List<Position> wifiAndBlePositionRecords =new ArrayList<>();  //记录wifi定位位置
    //记录pdr信息
    public List<Long> pdrTimeStampRecords =new ArrayList<>();  //记录pdr定位时间戳
    public List<Position>pdrPositionRecords=new ArrayList<>(); //记录pdr定位位置
    public List<Double>pdrStepLengthRecords=new ArrayList<>(); //记录pdr步长
    public List<Double> pdrWaistHeightRecords =new ArrayList<>(); //记录腰部高度
    public static final Boolean PDR_INFO_LOCK=true; //PDR数据锁

	public static final int WIFI_BLE_NUM_MIN = 3;// 定位时wifi及蓝牙热点的最小数量

	//更改通信状态
//    @SuppressLint("HandlerLeak")
    // TODO: 18-5-1 此处有内存泄漏问题
//	public Handler handler=new Handler(){
//		@Override
//		public void handleMessage(Message msg) {
//			switch (msg.what){
//				case 0:
//					connectTextView.setText("通信：连接失败");
//					break;
//				case 1:
//					connectTextView.setText("通信：连接成功");
//					break;
//				case 2:
//					connectTextView.setText("通信：传输正常");
//					break;
//				case 3:
//					connectTextView.setText("通信：传输断开");
//					break;
//			}
//		}
//	};

	//更改航向
//	@SuppressLint("HandlerLeak")
//	// TODO: 18-5-1 此处有内存泄漏问题
//	public Handler orientHandler =new Handler(){
//		@Override
//		public void handleMessage(Message msg) {
//			int orient = msg.arg1;
//			orientTextView.setText("方向:" + orient);
//			mMapView.autoDrawArrow(orient);
//			mOrient = orient;
//		}
//	};
	//显示当前高度
//	@SuppressLint("HandlerLeak")
//	// TODO: 18-5-1 此处有内存泄漏问题
//	public Handler heightHandler =new Handler(){
//		@Override
//		public void handleMessage(Message msg) {
//			float height= (Float) msg.obj;
//			mHeight=height;
//			floorNum=getFloorNumFromHeight(mHeight);
//			heightTextView.setText("高度: "+height+" 楼层："+floorNum);
//		}
//	};


	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SensorUtil.getInstance().printAllSensor(this); // 打印所有可用传感器
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		mainactivity = this;
		//权限申请
//		MainActivityPermissionsDispatcher.changeWiFiWithPermissionCheck(this);
//		checkPermission();

		FileWriterManager.getInstance().createFolderDir();//创建文件夹
//		WifiDataManager.getInstance().initWifi();
//		BleDataManager.getInstance().initBle();
//		WifiDataManager.getInstance().setRunnableScanWifiAndRunnableLocalization(); //将扫描与定位集成

        // WifiDataManager.getInstance().startScanWifi();    //扫描
//		WifiDataManager.getInstance().startLocalization(); //定位

		WifiAndBleDataManager.getInstance().initWifiAndBle(); //wifi及蓝牙初始化
		WifiAndBleDataManager.getInstance().setRunnableScanAndRunnableLocalization(); //将扫描与定位集成

		// 注册计步监听
//		mStepSensor = new StepSensorAcceleration(this, this);
//		if (!mStepSensor.registerStep()) {
//			Toast.makeText(this, "计步功能不可用！", Toast.LENGTH_SHORT).show();
//		}

		// 注册方向监听
//		mOrientSensor = new OrientSensor(this, this);
//		if (!mOrientSensor.registerOrient()) {
//			Toast.makeText(this, "方向功能不可用！", Toast.LENGTH_SHORT).show();
//		}

		// 注册气压监听
//		mBarometerSensor= new BarometerSensor(this);
//		boolean isAvailable =mBarometerSensor.registerBarometer();
//		if(!isAvailable){
//			Toast.makeText(this,"气压计不可用！",Toast.LENGTH_SHORT).show();
//		}

		intiResources();
	}

	private void intiResources() {
		statusTextView = findViewById(R.id.statusText);  //状态
		resultsTextView = findViewById(R.id.resultText); //结果
//		heightTextView = findViewById(R.id.heightText); //高度
//		orientTextView=findViewById(R.id.orientText);//航向
//		stepTextView=findViewById(R.id.stepText); //步数
//		connectTextView=findViewById(R.id.connectionText); //通信
		mMapView =  findViewById(R.id.stepView);
		pickerView=findViewById(R.id.picker);
		reset=findViewById(R.id.reset);
		locate=findViewById(R.id.locate);
//		connect=findViewById(R.id.connect);
//		save=findViewById(R.id.save);
		mMapView.setImage(imageSource);

		//楼层选择器动作事件监听器
		pickerView.setOnScrollListener(this);
		pickerView.setOnValueChangedListener(this);
		pickerView.setOnValueChangeListenerInScrolling(this);
		String[] floorNameDisplay=getResources().getStringArray(R.array.floor_name);
		pickerView.refreshByNewDisplayedValues(floorNameDisplay);
		pickerView.setValue(8); //默认为

		//重置按钮事件监听器
		reset.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mMapView.reset();  //清空当前轨迹点
			}
		});

		//保存按钮事件监听器
//		save.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				//将数据写入文本
//				FileWriterManager.getInstance().savePdrDataToSDCards(pdrTimeStampRecords,pdrPositionRecords,pdrStepLengthRecords,pdrWaistHeightRecords);
//				FileWriterManager.getInstance().saveWifiAndBleDataToSDCards(wifiAndBleTimeStampRecords, wifiAndBlePositionRecords, wifiAndBleSignalLevelRecords);
//			}
//		});

		//定位按钮事件监听器
		locate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				uiUpdate();
            //使图形焦点聚焦于当前定位坐标
				// TODO: 2018/4/28 此处暂缺定位聚焦功能具体实现，需要查看SubscaleImageView文档
			}
		});

		//与服务器连接
//		connect.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				AlertDialog.Builder connectBuilder=new AlertDialog.Builder(MainActivity.mainactivity);
//				connectBuilder.setTitle("与服务连接");
//				ViewGroup connectGroup= (ViewGroup) MainActivity.this.getLayoutInflater().inflate(R.layout.connect,null);
//				 final EditText ipEditText= (EditText) connectGroup.getChildAt(2);
//				 final EditText portEditText= (EditText) connectGroup.getChildAt(4);
//				connectBuilder.setView(connectGroup);
//
//				connectBuilder.setNegativeButton("连接", new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						Log.i(TAG, "onClick: +portText="+portEditText.getText());
//						Log.i(TAG, "onClick: +portText="+String.valueOf(portEditText.getText()));
//						Log.i(TAG, "onClick: "+Integer.parseInt(String.valueOf(portEditText.getText())));
//						//与服务器连接
//						connectWithServer(String.valueOf(ipEditText.getText()),Integer.parseInt(String.valueOf(portEditText.getText())));
//					}
//				});
//				connectBuilder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						Toast.makeText(getApplicationContext(), "-.-", Toast.LENGTH_SHORT).show();
//					}
//				});
//				AlertDialog alertDialog = connectBuilder.create();
//				alertDialog.show();
//
//			}
//		});

		//点按事件监听器
		final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if (mMapView.isReady()) {
					PointF imageCoord=new PointF(e.getX(),e.getY());
					//从屏幕像素坐标转化为地图图片像素坐标
					imageCoord=mMapView.viewToSourceCoord(imageCoord);
					//从地图图片像素坐标转换为实际坐标
					PointF trueCoord=floorMap.imageCoordToTrueCoord(imageCoord);
					//更新当前位置点
					currentPdrPosition.x=trueCoord.x;
					currentPdrPosition.y=trueCoord.y;
					PointF showPoint=new PointF(currentPdrPosition.x, currentPdrPosition.y);
					showPoint=floorMap.trueCoordToImageCoord(showPoint);
					mMapView.autoDrawPoint(showPoint); //传入MapView绘图
				}
				return true;
			}

		});

		//StepView选择路径初始点
        // TODO: 18-5-1 应当增加机制，判断无WiFi、仅能依靠PDR定位时才能手动选择点，最好有个提示AlertDialog
		mMapView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				return gestureDetector.onTouchEvent(motionEvent);
			}
		});
	}

    /*
     与服务器通信具体实现
     */
//	private void connectWithServer( String ip, int port) {
//		if(ip == null){
//			Toast.makeText(MainActivity.this,"please input Server IP",Toast.LENGTH_SHORT).show();
//		}
//		Timer connectTimer=new Timer();
//		ConnectTimerTask connectTimerTask=new ConnectTimerTask(ip,port);
//		connectTimer.schedule(connectTimerTask,0,CONNECT_PERIOD);
//	}


	// WiFi定位结果，更改UI界面

	public void uiUpdate() {
		if(WifiAndBleDataManager.getInstance().isNormal){
			String outString = "定位坐标 x:  " + currentWifiPosition.x + "  y: " + currentWifiPosition.y+"  z: "+ currentWifiPosition.z;
			resultsTextView.setText(outString);
			String tips;
			if (WifiAndBleDataManager.getInstance().isNormal) {
				tips = "正常";
			} else {
				tips = "数据库中匹配不到足够的wifi";
			}
			outString = "当前WiFi个数:" + WifiAndBleDataManager.getInstance().wifiScanResults.size() + " "
					   +"当前蓝牙个数:" + WifiAndBleDataManager.getInstance().bleScanResults.size() + " " + tips+ "定位次数  "+ WifiAndBleDataManager.getInstance().locateNum;
			statusTextView.setText(outString);
            //将当前实际坐标转换为地图图片像素坐标，并绘出
			PointF showPoint=new PointF(currentWifiPosition.x, currentWifiPosition.y);
			showPoint=floorMap.trueCoordToImageCoord(showPoint);
			mMapView.autoDrawPoint(showPoint);
		}else{
			resultsTextView.setText("定位异常或无法定位");
			statusTextView.setText("WiFi或蓝牙个数未达到定位要求");
		}

	}

	@Override
	public void getOrient(int orient) {
		// 方向回调
//		orientTextView.setText("方向:" + orient);
//		mMapView.autoDrawArrow(orient);
//		mOrient=orient;
	}

	@Override
	public void getStep(int stepNum, double stepLength, double height) {

        mStepLength=(float)stepLength;
//		mStepLength =(float)0.5f;
		//  计步回调
//		stepTextView.setText("步数:" + stepNum+" 步长："+ mStepLength);

		/*
		测试wifi及蓝牙，关闭pdr
		 */

		//根据步长及航向计算X\Y变化
//		float deltaX= (mStepLength * (float)Math.sin(Math.toRadians(mOrient)));
//		float deltaY= (mStepLength * (float)Math.cos(Math.toRadians(mOrient)));
//		//更新当前位置坐标
//		currentPdrPosition.x+=deltaX;
//		currentPdrPosition.y+=deltaY;
//        //记录pdr信息
//        synchronized (PDR_INFO_LOCK) {
//            pdrTimeStampRecords.add(System.currentTimeMillis());
//            pdrPositionRecords.add(currentPdrPosition);
//            pdrStepLengthRecords.add(stepLength);
//            pdrWaistHeightRecords.add(height);
//        }
//
//        PointF showPoint=new PointF(currentPdrPosition.x, currentPdrPosition.y);
//		showPoint=floorMap.trueCoordToImageCoord(showPoint);
//		mMapView.autoDrawPoint(showPoint); //传入MapView绘图
//		pdrResult="△x="+deltaX+" △y="+deltaY;
//		resultsTextView.setText(pdrResult); //更新状态
//		String tips = "数据库中匹配不到足够的wifi";
//		statusTextView.setText(tips);


	}


	@Override
	public void onScrollStateChange(NumberPickerView view, int scrollState) {
		Log.d(TAG, "onScrollStateChange : " + scrollState);
	}

	@Override
	public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
		String[] content = picker.getDisplayedValues();
		if (content != null){
		    //根据picker内容切换楼层
			changeFloor(pickerView.getValue());
		}
	}

	@Override
	public void onValueChangeInScrolling(NumberPickerView picker, int oldVal, int newVal) {
		Log.d(TAG, "onValueChangeInScrolling oldVal : " + oldVal + " newVal : " + newVal);
	}

	//切换楼层
	private void changeFloor(int floorNum){
	    floorMap=FloorMapManager.getInstance().floorMaps[floorNum];
		imageSource= ImageSource.asset(floorMap.name);
		mMapView.setImage(imageSource);
		mMapView.reset();
	}

	//根据高度计算楼层号
	private int getFloorNumFromHeight(float mHeight) {
		return (Math.round(mHeight/FLOOR_HEIGHT)+1);
	}

	/*
	检查权限的另一种方式
	 */
	private void checkPermission() {
		//检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE)
				!= PackageManager.PERMISSION_GRANTED) {
			//用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
					.CHANGE_WIFI_STATE)) {
				Toast.makeText(this, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
			}
			//申请权限
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CHANGE_WIFI_STATE}, REQUEST_CHANGE_WIFI_STATE);

		} else {
			Toast.makeText(this, "授权成功！", Toast.LENGTH_SHORT).show();
			Log.e(TAG, "checkPermission: 已经授权！");
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 注销传感器监听
//		mStepSensor.unregisterStep();
//		mOrientSensor.unregisterOrient();
//		mBarometerSensor.unregisterBarometer();
		//关闭wifi及蓝牙扫描定位
		WifiAndBleDataManager.getInstance().scheduledExecutorService.shutdownNow();
//		//保存定位数据
//        FileWriterManager.getInstance().savePdrDataToSDCards(pdrTimeStampRecords,pdrPositionRecords,pdrStepLengthRecords,pdrWaistHeightRecords);
//        FileWriterManager.getInstance().saveWifiAndBleDataToSDCards(wifiAndBleTimeStampRecords, wifiAndBlePositionRecords, wifiAndBleSignalLevelRecords);
	}



}
