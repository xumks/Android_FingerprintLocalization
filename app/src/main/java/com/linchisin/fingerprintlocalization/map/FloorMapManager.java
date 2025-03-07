package com.linchisin.fingerprintlocalization.map;

import android.graphics.PointF;

import com.linchisin.fingerprintlocalization.util.CoordinateSystem;

/**
 * Created by Sin on 2018/4/19.
 *
 *@author LinChiSin
 * @date 2018-4-20 下午 5:00:32
 * @description 包含多个楼层地图的地图管理器，单例设计模式，程序启动时进行加载，并初始化
 */

public class FloorMapManager {
    public static FloorMapManager instance=null;
    //地图管理器，单例设计模式
   //地图数组
    public FloorMap[]floorMaps;

    private static float scale_D10 = 0.03f;

    private FloorMapManager(){};

    public static FloorMapManager getInstance() {
        synchronized (FloorMapManager.class) {
            if(instance==null){
                instance=new FloorMapManager();
            }
        }
        return instance;
    }

    public void init(){
        floorMaps= new FloorMap[]{
                 new FloorMap("JinhuaF1.png",79.5f,0f,new PointF(24.7f,37.2f), CoordinateSystem.BOTTOM_LEFT),
                 new FloorMap("JinhuaF2.png",79.5f,4f,new PointF(24.7f,37.2f), CoordinateSystem.BOTTOM_LEFT),
                 new FloorMap("JinhuaF3.png",79.5f,8f,new PointF(24.7f,37.2f), CoordinateSystem.BOTTOM_LEFT),
                 new FloorMap("JinhuaF4.png",79.5f,12f,new PointF(24.7f,37.2f), CoordinateSystem.BOTTOM_LEFT),
                 new FloorMap("NMBF6.png",43,25f,new PointF(7f,2f), CoordinateSystem.BOTTOM_LEFT),
                 new FloorMap("NMBF7.jpg",43,30f,new PointF(0,0),CoordinateSystem.BOTTOM_LEFT),
                 new FloorMap("nmb_f6.png",22,25f,new PointF(1.2f,0), CoordinateSystem.BOTTOM_LEFT),
                 new FloorMap("nmb_f5.png",22,20f,new PointF(1.2f,0),CoordinateSystem.BOTTOM_LEFT),
                 new FloorMap("NMBD10.png",scale_D10,new PointF(5.1f,6.9f),CoordinateSystem.BOTTOM_LEFT)
         };
    }

    /*
    //需要给定图片文件名、实际宽度、图片宽度确定一个楼层地图
	public static final FloorMap JHF1 =new FloorMap("JinhuaF1.png",200); //此处随便给的两个数，需要用到时注意检查
	public static final FloorMap JHF2 =new FloorMap("JinhuaF2.png",200); //此处随便给的两个数，需要用到时注意检查
	public static final FloorMap JHF3 =new FloorMap("JinhuaF3.png",200); //此处随便给的两个数，需要用到时注意检查
	public static final FloorMap JHF4 =new FloorMap("JinhuaF4.png",200); //此处随便给的两个数，需要用到时注意检查

    //新主楼实际测试环境
	public static final FloorMap NMBF6 =new FloorMap("NMBF6.png",43);
	public static final FloorMap NMBF7 =new FloorMap("NMBF7.jpg",43);

	//此案例下指定坐标初始点
	public static final FloorMap TESTF6 =new FloorMap("nmb_f6.png",22,new PointF(1.2f,0), CoordinateSystem.BOTTOM_LEFT);
	public static final FloorMap TESTF5=new FloorMap("nmb_f5.png",22,new PointF(1.2f,0),CoordinateSystem.BOTTOM_LEFT);

    */


}
