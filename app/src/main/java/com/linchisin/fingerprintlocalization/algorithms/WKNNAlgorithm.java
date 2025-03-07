package com.linchisin.fingerprintlocalization.algorithms;

import android.os.AsyncTask;
import android.util.Log;

import com.linchisin.fingerprintlocalization.map.FingerprintMapModel;
import com.linchisin.fingerprintlocalization.wifiAndBle.WifiAndBleDataManager;
import com.linchisin.fingerprintlocalization.ui.MainActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author LinChiSin
 * @date 2018-4-24 上午11:23:23
 * @description 实现WKNN算法
 */

public class WKNNAlgorithm {

	private boolean isBusy = false;
	private static final float EPSILON=0.0001f; //防止计算权重时距离为0出现异常

	public void start(){
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
				int k = 3;        //WKNN算法，K指定为4
				localizationAlgorithm(
						WifiAndBleDataManager.getInstance().rssScan,
						FingerprintMapModel.getInstance().radioMapExcel, k);
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

	public void localizationAlgorithm(float rss[], float radioMap[][], int K) {
		int M = radioMap.length;
		int N = rss.length;
		Log.i("定位算法的N和M", "M = " + String.valueOf(M) + "N= " + N);
		// 计算曼哈顿距离
		float mDistance[] = new float[M];
		for (int i = 0; i < M; i++) {
			mDistance[i] = 0;// 初始化为0
			for (int j = 0; j <N; j++) {
				//前四列为X、Y、Z坐标及方向
//				mDistance[i]+=Math.abs(rss[j]- radioMap[i][j+3]);//曼哈顿距离
				mDistance[i]+=Math.pow(rss[j]- radioMap[i][j+3],2);//欧氏距离
			}
//			mDistance[i] = mDistance[i];
			mDistance[i]=(float) Math.sqrt(mDistance[i]);
            Log.i("欧氏距离：", String.valueOf(mDistance[i]));
		}
		//将距离与序号进行关联，使得对距离进行排序时，序号也能对应排序
		LinkedHashMap<Integer,Float> linkedHashMap=new LinkedHashMap<>();
		for (int i = 0; i < M; i++) {
			linkedHashMap.put(i,mDistance[i]);
		}
		List<Map.Entry<Integer,Float>>linkedList=new LinkedList<>(linkedHashMap.entrySet());
		Collections.sort(linkedList, new Comparator<Map.Entry<Integer, Float>>() {
			@Override
			public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		//获取按距离排序后前K个的序号
		int []index=new int[K];
		float [] distance=new float[K];
		float sumWeight=0;
		for (int i = 0; i < K; i++) {
			index[i]=linkedList.get(i).getKey();
			distance[i]=linkedList.get(i).getValue();
			sumWeight+=1/(distance[i]+EPSILON);
		}

//		//按照距离计算权重
		float [] weight=new float[K];
		for (int i = 0; i < K; i++) {
			weight[i]=(1/(distance[i]+EPSILON))/sumWeight;
		}
		//KNN
//		float [] weight=new float[K];
//		for (int i = 0; i < K; i++) {
//			weight[i]=(float)1/K;
//		}

		// 计算位置
		float x = 0;
		float y = 0;
		float z = 0;


		for (int i = 0; i < K; i++) {

			x += weight[i]*radioMap[index[i]][0];  // radioMap 的长度一定是N+2的，多一个x, 一个y, 一个z
			y += weight[i]*radioMap[index[i]][1];
			z += weight[i]*radioMap[index[i]][2];
            Log.i("参考点：", "("+String.valueOf(radioMap[index[i]][0])+","+String.valueOf(radioMap[index[i]][1])+","+
                    String.valueOf(radioMap[index[i]][2])+")");
            Log.i("权值：", String.valueOf(weight[i]));
		}

//		x = x / K;
//		y = y / K;
//		z = z / K;

		//更新时间记录、信号量记录、位置记录
		MainActivity.mainactivity.wifiAndBleTimeStampRecords.add(System.currentTimeMillis());
		List<Float>rssList=new ArrayList<>();
		for (float rs : rss) {
			rssList.add(rs);
		}
		MainActivity.mainactivity.wifiAndBleSignalLevelRecords.add(rssList);
		MainActivity.mainactivity.wifiAndBlePositionRecords.add(MainActivity.mainactivity.currentWifiPosition =new Position(x,y,z));
		Log.i("位置", "X = " + String.valueOf(x) + "Y=" +String.valueOf(y)+"Z="+String.valueOf(z));
	}

}
