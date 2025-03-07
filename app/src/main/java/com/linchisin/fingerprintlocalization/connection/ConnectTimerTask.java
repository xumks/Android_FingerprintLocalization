package com.linchisin.fingerprintlocalization.connection;

import android.util.Log;

import com.linchisin.fingerprintlocalization.algorithms.Position;
import com.linchisin.fingerprintlocalization.ui.MainActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.TimerTask;

/**
 * Created by Sin on 2018/4/23.
 *
 * @author LinChiSin
 * @date 2018-4-23,下午22:43:42
 * @description 固定周期将定位结果发送至服务器
 */

public class ConnectTimerTask extends TimerTask {
	private static final String TAG ="ConnectTimerTask" ;
	private static boolean socketStatus = false; //通信状态
	private OutputStream outputStream = null;
	private  String ip;
	private int port;

	public ConnectTimerTask(String ip,int port){
		Log.i(TAG, "ConnectTimerTask: 新建TimerTask任务成功");
		this.ip=ip;
		this.port=port;
	}

	@Override
	public void run() {
//		if (!socketStatus) {   //与服务器通信标志，确保断开重连
//			try {
//				Log.i(TAG, "run: ip="+ip);
//				Log.i(TAG, "run: port="+port);
//				Socket socket = new Socket(ip, 8000);
//				MainActivity.mainactivity.handler.sendEmptyMessage(1);
//				Log.i(TAG, "run: Socket建立成功");
//				socketStatus = true;
//				outputStream = socket.getOutputStream();
//			} catch (IOException e) {
//				Log.i(TAG, "run: Socket建立失败");
//				MainActivity.mainactivity.handler.sendEmptyMessage(0);
//				e.printStackTrace();
//			}
//		}
//		if(socketStatus) {
//			try {
//				MainActivity.mainactivity.handler.sendEmptyMessage(2);
//				Position currentPosition=MainActivity.mainactivity.currentPdrPosition;
////				String s = MainActivity.mainactivity.currentPosition.x + "," + MainActivity.mainactivity.result_y0;
//				String s = currentPosition.x + "," + currentPosition.y+","+currentPosition.z;
//				outputStream.write(s.getBytes());
//				Log.i(TAG, "Socket: 传输正常："+s);
//			} catch (IOException e) {
//				socketStatus = false;  //断开重连
//				Log.i(TAG, "Socket: 传输断开：");
//				MainActivity.mainactivity.handler.sendEmptyMessage(3);
//				e.printStackTrace();
//			}
//		}
	}

}

