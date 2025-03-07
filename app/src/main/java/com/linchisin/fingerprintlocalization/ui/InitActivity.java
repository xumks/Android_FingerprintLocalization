package com.linchisin.fingerprintlocalization.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;

import com.linchisin.fingerprintlocalization.R;
import com.linchisin.fingerprintlocalization.map.FingerprintMapDatabase;
import com.linchisin.fingerprintlocalization.map.FingerprintMap;
import com.linchisin.fingerprintlocalization.map.FingerprintMapModel;
import com.linchisin.fingerprintlocalization.map.FloorMapManager;

import cn.carbswang.android.numberpickerview.library.NumberPickerView;

/**
 * @author jiangqideng@163.com
 * @date 2016-6-29 上午12:53:22
 * @description 欢迎界面，这里好几秒，用来加载耗时的指纹库及地图信息
 *
 * @author LinChiSin
 * @date 2018-4-24 下午18:57:34
 * @description 新增选择指纹库
 */
public class InitActivity extends Activity implements NumberPickerView.OnScrollListener,NumberPickerView.OnValueChangeListener,
		NumberPickerView.OnValueChangeListenerInScrolling {
	private static final String TAG="InitActivity:";
	public static InitActivity initActivity;
	private NumberPickerView databasePicker;
	private FingerprintMap fingerprintMap = FingerprintMapDatabase.FINGERPRINT_FILE_6; //默认指纹库为1；
	private final long SPLASH_LENGTH = 0;  //等待时长

	Handler handler = new Handler();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splash);
		initActivity = this;

		AlertDialog.Builder chooseDatabase=new AlertDialog.Builder(InitActivity.initActivity);
		ViewGroup viewGroup= (ViewGroup) InitActivity.this.getLayoutInflater().inflate(R.layout.choose_fingerprint_db,null);
		databasePicker= (NumberPickerView) viewGroup.getChildAt(1);

		//楼层选择器动作事件监听器
		databasePicker.setOnScrollListener(this);
		databasePicker.setOnValueChangedListener(this);
		databasePicker.setOnValueChangeListenerInScrolling(this);
		String[] databaseDisplay=getResources().getStringArray(R.array.fingerprint_database);
		databasePicker.refreshByNewDisplayedValues(databaseDisplay);
		databasePicker.setValue(0);


		chooseDatabase.setView(viewGroup);
		chooseDatabase.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {

						Log.i(TAG, "run: 选择指纹库："+ fingerprintMap.fingerprintFilename+" "+ fingerprintMap.macFilename);
						FingerprintMapModel.getInstance().init(fingerprintMap); //初始化指纹库
						FloorMapManager.getInstance().init(); //初始化地图管理器
						Intent intent = new Intent(InitActivity.this,
								MainActivity.class);
						InitActivity.this.startActivity(intent);
						InitActivity.this.finish();

					}
				}, SPLASH_LENGTH);// 2秒后跳转
			}
		});
		AlertDialog alertDialog = chooseDatabase.create();
		alertDialog.show();
	}

	@Override
	public void onScrollStateChange(NumberPickerView view, int scrollState) {
		Log.d(TAG, "onScrollStateChange : " + scrollState);

	}

	@Override
	public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
		String[] content = picker.getDisplayedValues();
		if (content != null){
			changeDatabase(databasePicker.getValue());
			Log.i(TAG, "onValueChange: 选择指纹库："+databasePicker.getValue());
		}
	}

	private void changeDatabase(int databaseNum) {
		switch (databaseNum){
			case 0:
				fingerprintMap = FingerprintMapDatabase.FINGERPRINT_FILE_1;
				break;
			case 1:
				fingerprintMap = FingerprintMapDatabase.FINGERPRINT_FILE_2;
				break;
			case 2:
				fingerprintMap = FingerprintMapDatabase.FINGERPRINT_FILE_3;
				break;
			case 3:
				fingerprintMap = FingerprintMapDatabase.FINGERPRINT_FILE_4;
				break;
			case 4:
				fingerprintMap = FingerprintMapDatabase.FINGERPRINT_FILE_5;
				break;
			case 5:
				fingerprintMap = FingerprintMapDatabase.FINGERPRINT_FILE_6;
				break;
		}
	}

	@Override
	public void onValueChangeInScrolling(NumberPickerView picker, int oldVal, int newVal) {
		Log.d(TAG, "onValueChangeInScrolling oldVal : " + oldVal + " newVal : " + newVal);
	}
}
