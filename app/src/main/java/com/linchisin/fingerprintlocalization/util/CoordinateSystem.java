package com.linchisin.fingerprintlocalization.util;

/**
 * Created by Sin on 2018/4/19.
 * @author LinChiSin
 * @date 2018-4-20 下午 5:00:32
 * @description 坐标系
 */

public class CoordinateSystem {
	//左上坐标系，规定坐标原点在左上角，X轴正方向为正东，Y轴正方向为正南
	public static final int TOP_LEFT=0;
	//右上坐标系，规定坐标原点在右上角，X轴正方向为正西，Y轴正方向为正南
	public static final int TOP_RIGHT=1;
	//左下坐标系，规定坐标原点在左下角，X轴正方向为正东，Y轴正方向为正北
	public static final int BOTTOM_LEFT =2;
	//右下坐标系，规定坐标原点在右下角，X轴正方向为正西，Y轴正方向为正北
	public static final int BOTTOM_RIGHT =3;

}
