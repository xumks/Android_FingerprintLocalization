package com.linchisin.fingerprintlocalization.file;

/**
 * Created by Sin on 2018/4/27.
 */

import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.linchisin.fingerprintlocalization.algorithms.Position;
import com.linchisin.fingerprintlocalization.ui.MainActivity;
import com.linchisin.fingerprintlocalization.wifiAndBle.WifiAndBleDataManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author LinChiSin
 * @date 2018-4-27,下午16:46:22
 * @description  将程序数据写入本地文件夹中,单例模式
 */

public class FileWriterManager {


	 private static FileWriterManager  instance=null;
	 private static final String TAG="FileWriterManager:";
	 private static String APPLICATION_FOLDER_PATH = Environment.getExternalStorageDirectory().getPath()+"/"+"FingerprintLocalization"+ "/";
	 private static String APPLICATION_FOLDER_PATH_WIFI =APPLICATION_FOLDER_PATH+"WIFI"+"/" ;
	 private static String APPLICATION_FOLDER_PATH_PDR =APPLICATION_FOLDER_PATH+"PDR"+"/" ;


	//单例构造模式
	private  FileWriterManager(){}
    //获取唯一样例
	public static FileWriterManager getInstance(){
		synchronized (FileWriterManager.class) {
			if(instance==null){
				instance=new FileWriterManager();
			}
			return instance;
		}
	}

	//将Wifi信息写入本地文档
	public void saveWifiAndBleDataToSDCards(List<Long> wifiTimeStampRecords, List<Position> wifiPositionRecords, List<List<Float>>wifiSignalLevelRecords){
		long currentTime=System.currentTimeMillis();
		SimpleDateFormat format= new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
		Date date=new Date(currentTime);
		//创建相应文件路径+路径名的文件
		try {
			//可直接写成.xls文件
			File file=new File(APPLICATION_FOLDER_PATH_WIFI +"WiFi_Info_"+ Build.MODEL+"_"+format.format(date)+".xls");
			FileOutputStream fileOutputStream=new FileOutputStream(file);
			OutputStreamWriter outputStreamWriter=new OutputStreamWriter(fileOutputStream);
			Writer out=new BufferedWriter(outputStreamWriter);
			// TODO: 2018/4/28 此处有线程安全问题
			//添加表头
			out.write("Timestamp"+"\t");
			out.write("Model"+"\t");
			out.write("x"+"\t");
			out.write("y"+"\t");
			out.write("z"+"\t");
			out.write("Orient"+"\t");
			for (int i = 0; i < 17; i++) {
				char c=(char)('c'+i);
				out.write("TP"+c+"_2G"+"\t");
				out.write("TP"+c+"_5G"+"\t");
			}
			out.write("TPt_2G"+"\t");
			out.write("TPt_5G"+"\n");
			int size=wifiTimeStampRecords.size();
			for (int i = 0; i < size; i++) {
				//写入时间戳
				out.write(String.valueOf(wifiTimeStampRecords.get(i))+"\t");
				//写入设备型号
				out.write(WifiAndBleDataManager.getInstance().deviceModel+"\t");
				//写入估算位置
				out.write(String.valueOf(wifiPositionRecords.get(i).x)+"\t");
				out.write(String.valueOf(wifiPositionRecords.get(i).y)+"\t");
				out.write(String.valueOf(wifiPositionRecords.get(i).z)+"\t");
				//写入测试方向
				out.write(MainActivity.mainactivity.mOrient+"\t");
				List<Float>singleGroupSignalLevel=wifiSignalLevelRecords.get(i);
				int length=singleGroupSignalLevel.size();
				//按照MAC地址写入信号量大小
				for (int j = 0; j < length-1; j++) {
					out.write(String.valueOf(singleGroupSignalLevel.get(j))+"\t");
				}
				//最后一个换行
				out.write(String.valueOf(singleGroupSignalLevel.get(length-1))+"\n");
			}
			//关闭输出流
			out.close();
			outputStreamWriter.close();
			fileOutputStream.close();
			Log.i(TAG, "saveWifiAndBleDataToSDCards: 总体大小为："+wifiTimeStampRecords.size());
			Toast.makeText(MainActivity.mainactivity,"文件写入成功！",Toast.LENGTH_SHORT).show();
		} catch (FileNotFoundException e) {
			Log.i(TAG, "saveWifiAndBleDataToSDCards: "+"文件创建错误");
			Toast.makeText(MainActivity.mainactivity,"文件创建错误！",Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (IOException e) {
			Log.i(TAG, "saveWifiAndBleDataToSDCards: "+"文件写入错误");
			Toast.makeText(MainActivity.mainactivity,"文件写入错误！",Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

	}

	//将Wifi信息写入本地文档
	public void savePdrDataToSDCards(List<Long> pdrTimeStampRecords, List<Position> pdrPositionRecords, List<Double>pdrStepLengthRecords, List<Double>pdrWaistHeightRecords){
		synchronized (MainActivity.PDR_INFO_LOCK) {
			long currentTime=System.currentTimeMillis();
			SimpleDateFormat format= new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
			Date date=new Date(currentTime);
			//创建相应文件路径+路径名的文件
			try {
                //可直接写成.xls文件
                File file=new File(APPLICATION_FOLDER_PATH_PDR +"PDR_Info_"+ Build.MODEL+"_"+format.format(date)+".xls");
                FileOutputStream fileOutputStream=new FileOutputStream(file);
                OutputStreamWriter outputStreamWriter=new OutputStreamWriter(fileOutputStream);
                Writer out=new BufferedWriter(outputStreamWriter);
                // TODO: 2018/4/28 此处有线程安全问题
                int size=pdrTimeStampRecords.size();
                for (int i = 0; i < size; i++) {
                    //写入时间戳
                    out.write(String.valueOf(pdrTimeStampRecords.get(i))+"\t");
                    //写入估算位置
                    out.write(String.valueOf(pdrPositionRecords.get(i).x)+"\t");
                    out.write(String.valueOf(pdrPositionRecords.get(i).y)+"\t");
                    out.write(String.valueOf(pdrPositionRecords.get(i).z)+"\t");
                    //写入步长
                    out.write(String.valueOf(pdrStepLengthRecords.get(i))+"\t");
                    //写入高度
                    out.write(String.valueOf(pdrWaistHeightRecords.get(i)+"\n"));

                }
                //关闭输出流
                out.close();
                outputStreamWriter.close();
                fileOutputStream.close();
                Log.i(TAG, "saveWifiAndBleDataToSDCards: 总体大小为："+pdrTimeStampRecords.size());
                Toast.makeText(MainActivity.mainactivity,"文件写入成功！",Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                Log.i(TAG, "saveWifiAndBleDataToSDCards: "+"文件创建错误");
                Toast.makeText(MainActivity.mainactivity,"文件创建错误！",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (IOException e) {
                Log.i(TAG, "saveWifiAndBleDataToSDCards: "+"文件写入错误");
                Toast.makeText(MainActivity.mainactivity,"文件写入错误！",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
		}

	}

	//创建文件夹
	// TODO: 2018/4/28  此处有权限管理问题
	public void createFolderDir(){
		File folderMain=new File(APPLICATION_FOLDER_PATH);
		if (!folderMain.exists()) {
			folderMain.mkdirs();
		}
		File folderWifi=new File(APPLICATION_FOLDER_PATH_WIFI);
		if (!folderWifi.exists()) {
			folderWifi.mkdirs();
		}
		File folderPDR=new File(APPLICATION_FOLDER_PATH_PDR);
		if (!folderPDR.exists()) {
			folderPDR.mkdirs();
		}
	}


}
