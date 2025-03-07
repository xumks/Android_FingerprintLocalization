package com.linchisin.fingerprintlocalization.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import android.util.Log;
import android.widget.Toast;

import com.linchisin.fingerprintlocalization.ui.InitActivity;
import com.linchisin.fingerprintlocalization.ui.MainActivity;
import com.linchisin.fingerprintlocalization.wifiAndBle.DeviceSignalDifference;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;


/**
 * @author jiangqideng@163.com
 * @date 2016-6-29 上午12:50:32
 * @description 读取文件
 *
 * @author LinChiSin
 * @date 2018-4-17,下午16:42:42
 * @description 从Assets文件中读取Excel  //调用JXL API
 */
public class FileReaderManager {
	private static final String TAG="FileReaderManager:";

   /*
   读取Excel文件，获取指纹库，调用JXL API 实现
    */
	public float[][] getRadioMapFromExcel(String fileName){
		InputStream in;
		try {
			in = InitActivity.initActivity.getResources().getAssets().open(fileName);
			try {
				Workbook workbook=Workbook.getWorkbook(in);
				Sheet sheet=workbook.getSheet(0);
				//获取Excel的行列数
				int rows=sheet.getRows();
				int cols=sheet.getColumns();
				Log.i(TAG, "getRadioMapFromExcel: "+"表格行数为："+rows);
				Log.i(TAG, "getRadioMapFromExcel: "+"表格列数为："+cols);
				//本案例中，去除前两列（序号、设备号），第三列到第五列分别为X、Y、Z（楼层号）
				//亦即，调整后的数组每行对应一个指纹数据，前三列对应RP位置，第4列代表采集RP时的方向，后(cols-4)列代表（cols-4）个AP的RSSI
				float [][] radioMap=new float[rows][cols];
				//读取Excel内容
				for (int i = 0; i <cols ; i++) {  //从第三列开始读取
					for (int j = 0; j <rows; j++) {
						radioMap[j][i]=Float.parseFloat(sheet.getCell(i,j).getContents());
					}
				}
				return radioMap;
			} catch (BiffException e) {
				Toast.makeText(MainActivity.mainactivity, "JXL读取失败",
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		} catch (IOException e) {
			Toast.makeText(MainActivity.mainactivity, "文件读取失败",
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		return null;
	}

	/*
  读取Excel文件，获取MAC地址库，调用JXL API 实现,并直接返回哈希表
   */
	public HashMap<String,Integer> getBssidFromExcel(String fileName){
		InputStream in;
		try {
			in = InitActivity.initActivity.getResources().getAssets().open(fileName);
			try {
				Workbook workbook=Workbook.getWorkbook(in);
				Sheet sheet=workbook.getSheet(0);
				//获取Excel的行列数
				int rows=sheet.getRows();
				int cols=sheet.getColumns();
				Log.i(TAG, "getBssidFromExcel: "+"表格行数为："+rows);
				Log.i(TAG, "getBssidFromExcel: "+"表格列数为："+cols);
				//本案例中，MAC地址按行存储在Excel表中，第一列为Mac地址
				HashMap<String,Integer>bssids=new HashMap<>();
				//读取Excel内容，只需第一列数据
				for (int j = 0; j <rows ; j++) {
					bssids.put(sheet.getCell(0,j).getContents(),j);
				}
				return bssids;
			} catch (BiffException e) {
				Toast.makeText(MainActivity.mainactivity, "JXL读取失败",
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		} catch (IOException e) {
			Toast.makeText(MainActivity.mainactivity, "文件读取失败",
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		return null;
	}

	/*
	从Excel文件中读取设备差异性表
	 */

	public HashMap<String,DeviceSignalDifference> getDeviceDiffFromExcel(String fileName){
		InputStream in;
		try {
			in = InitActivity.initActivity.getResources().getAssets().open(fileName);
			try {
				Workbook workbook=Workbook.getWorkbook(in);
				Sheet sheet=workbook.getSheet(0);
				//获取Excel的行列数
				int rows=sheet.getRows();
				int cols=sheet.getColumns();
				Log.i(TAG, "getDeviceDiffFromExcel: "+"表格行数为："+rows);
				Log.i(TAG, "getDeviceDiffFromExcel: "+"表格列数为："+cols);
				//本案例中，设备差异性按行列存储在Excel表中，共4行，指代4个不同型号的设备，共计三列，分别为设备型号、a、b;
				HashMap<String,DeviceSignalDifference>deviceDiff=new HashMap<>();
				//读取Excel内容
				for (int j = 0; j <rows ; j++) {
						float aWifi=Float.parseFloat(sheet.getCell(1,j).getContents());
					    float bWifi=Float.parseFloat(sheet.getCell(2,j).getContents());
					    float aBle=Float.parseFloat(sheet.getCell(3,j).getContents());
					    float bBle=Float.parseFloat(sheet.getCell(4,j).getContents());
						deviceDiff.put(sheet.getCell(0,j).getContents(),new DeviceSignalDifference(aWifi,bWifi,aBle,bBle));
				}
				return deviceDiff;
			} catch (BiffException e) {
				Toast.makeText(MainActivity.mainactivity, "JXL读取失败",
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		} catch (IOException e) {
			Toast.makeText(MainActivity.mainactivity, "文件读取失败",
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		return null;
	}






}
