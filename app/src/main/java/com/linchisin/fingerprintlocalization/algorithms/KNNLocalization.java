package com.linchisin.fingerprintlocalization.algorithms;

import android.os.AsyncTask;
import android.util.Log;

import com.linchisin.fingerprintlocalization.map.FingerprintMapModel;
import com.linchisin.fingerprintlocalization.wifiAndBle.WifiAndBleDataManager;
import com.linchisin.fingerprintlocalization.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jiangqideng@163.com
 * @date 2016-6-29 上午12:49:54
 * @description 基于knn的定位算法
 *
 * @author LinChiSin
 * @date 2018-4-24 上午11:23:23
 * @description 改进定位算法
 */

public class KNNLocalization {
	private boolean isBusy = false;
	float last_x = 0f;
	float last_y = 0f;

	public void start() {
		new LocalizationAlgorithmTask().execute();
	}

	// TODO: 2018/5/8  存在内存泄漏
	private class LocalizationAlgorithmTask extends
			AsyncTask<String, Void, Integer> {
		/**
		 * The system calls this to perform work in a worker thread and delivers
		 * it the parameters given to AsyncTask.execute()
		 */

		/*
		根据本案例改进的定位解算
		 */
		protected Integer doInBackground(String... urls) {
			int mapNum = 0;
			if (!isBusy) {
				isBusy = true;
				int k = 1;        //KNN算法，K指定为1
				float D_Threshold = 12f;    //距离阈值？
				localizationAlgorithm(
						WifiAndBleDataManager.getInstance().rssScan,
						FingerprintMapModel.getInstance().radioMapExcel, k, FingerprintMapModel.getInstance().bssidsExcel.size(),
						D_Threshold, last_x, last_y);
				mapNum = 1;
			}
			isBusy = false;
			return mapNum;
		}

		/**
		 * The system calls this to perform work in the UI thread and delivers
		 * the result from doInBackground()
		 */
		protected void onPostExecute(Integer results) {
			MainActivity.mainactivity.uiUpdate();
		}
	}

	public void localizationAlgorithm(float rss[], float radioMap[][], int K,
									 int n_AP, float D_Threshold, float x0, float y0) {
		int M = radioMap.length;
		int N = rss.length;
		Log.i("定位算法的N和M", "M = " + String.valueOf(M) + "N= " + N);
		int index[] = new int[N];
		for (int i = 0; i < N; i++) {
			index[i] = i;
		}

		for (int i = 0; i < n_AP; i++) {          //冒泡排序
			for (int j = 0; j < N - 1 - i; j++) {
				if (rss[j] > rss[j + 1]) {
					float tmp = rss[j];
					rss[j] = rss[j + 1];
					rss[j + 1] = tmp;
					int temp = index[j];
					index[j] = index[j + 1];
					index[j + 1] = temp;
				}
			}
		}// 这时rss数组的右端为最大的rss值，数组index的右端为相应的index

		// 设定搜索范围
		int searchRange[] = new int[M];
		int L_search;

		if (x0 == 0 && y0 == 0) {// 初始定位，没有先验信息，全局搜索
			L_search = M;
			for (int i = 0; i < M; i++) {
				searchRange[i] = i;
			}
		} else {
			// 缩减搜索范围
			L_search = 0;
			for (int i = 0; i < M; i++) {
				if (Math.abs(radioMap[i][0] - x0)      //第1列与第2列表示X、Y坐标
						+ Math.abs(radioMap[i][1] - y0) <= D_Threshold) {
					searchRange[L_search] = i;
					L_search++;
				}
			}
			if (L_search < K) {
				L_search = M;
				for (int i = 0; i < M; i++) {
					searchRange[i] = i;
				}
			}
		}
		Log.i("搜索范围L_search", "L_search = " + L_search);

		// 计算曼哈顿距离
		float mDistance[] = new float[L_search];
		for (int i = 0; i < L_search; i++) {
			mDistance[i] = 0;// 初始化为0
			for (int j = 0; j < n_AP; j++) {
				mDistance[i] = mDistance[i]
						+ Math.abs(rss[N - 1 - j]
								- radioMap[searchRange[i]][index[N - 1 - j]+4]);  //前四列为X、Y、Z坐标及方向
			}
			mDistance[i] = mDistance[i] / n_AP;
		}
		// 选出K个距离最小的指纹，对searchRange和mDistance一起排序
		for (int i = 0; i < K; i++) {
			for (int j = 0; j < L_search - 1 - i; j++) {
				if (mDistance[j] < mDistance[j + 1]) {
					float tmp = mDistance[j];
					mDistance[j] = mDistance[j + 1];
					mDistance[j + 1] = tmp;
					int temp = searchRange[j];
					searchRange[j] = searchRange[j + 1];
					searchRange[j + 1] = temp;
				}
			}
		}// 现在数组mDistance的右端是最小的K个距离，searchRange数组的右端是相应的指纹在radiomap中的index

		// 计算位置
		float x = 0;
		float y = 0;
		float z = 0;

		for (int i = 0; i < K; i++) {
			x += radioMap[searchRange[L_search - 1 - i]][0];  // radioMap 的长度一定是N+2的，多一个x, 一个y, 一个z
			y += radioMap[searchRange[L_search - 1 - i]][1];
			z += radioMap[searchRange[L_search - 1 - i]][2];
		}

		x = x / K;
		y = y / K;
		z = z / K;


		//修改，去除前后步融合

//		x = 0.7f * x + 0.3f * x0;         //位置计算: 与上一步结果进行加权，一定程度上避免大幅度跳变，引入定位误差偏大新问题
//		y = 0.7f * y + 0.3f * y0;
//
//		last_x=x;  //更新结果
//		last_y=y;

		//更新有关值,并存储
//		MainActivity.mainactivity.result_x0 = x;
//		MainActivity.mainactivity.result_y0 = y;
//		MainActivity.mainactivity.result_z0 = z;
		//更新时间记录、信号量记录、位置记录
		MainActivity.mainactivity.wifiAndBleTimeStampRecords.add(System.currentTimeMillis());
		List<Float>list=new ArrayList<>();
		for (float rs : rss) {
			list.add(rs);
		}
		MainActivity.mainactivity.wifiAndBleSignalLevelRecords.add(list);
		MainActivity.mainactivity.wifiAndBlePositionRecords.add(MainActivity.mainactivity.currentWifiPosition =new Position(x,y,z));
		Log.i("位置", "X = " + String.valueOf(x) + "Y=" +String.valueOf(y)+"Z="+String.valueOf(z));
	}

}
