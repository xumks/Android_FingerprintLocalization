package com.linchisin.fingerprintlocalization.map;


import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.util.Log;

import com.linchisin.fingerprintlocalization.ui.InitActivity;
import com.linchisin.fingerprintlocalization.util.CoordinateSystem;

import java.io.IOException;
import java.io.InputStream;

/**
 *@author LinChiSin
 * @date 2018-5-1 下午 6:03:32
 * @description  楼层二维地图类
 */

public class FloorMap {
    private static final String TAG="FloorMap:";
    public String name; //楼层地图图片文件名（.png 格式，存放在Assets文件夹中）
    public float floorHeight; //楼层实际高度
    public float  trueWidth; //楼层实际宽度（东西向）
    public float  trueHeight; //楼层实际高度 （南北向）
    public int imageWidth; //楼层地图图片宽度像素大小
    public int imageHeight; //楼层地图图片高度像素大小
    public float scale;  //地图比例尺，实际距离/图片像素
    public PointF initPoint=new PointF(0,0); //地图实际初始点,默认为（0,0);
    public int coordinateSystem=CoordinateSystem.BOTTOM_LEFT;  //地图坐标系，默认为东北坐标系；

    //仅指定文件名和真实宽度，其余自动处理
    public FloorMap(String name,float trueWidth,float floorHeight){
        this.name=name;
        this.trueWidth=trueWidth;
        this.floorHeight=floorHeight;
        getImageSizeFromAssets(); //自动读取图片大小
        this.scale=this.trueWidth/(float)this.imageWidth;
    }

    //仅指定文件名和真实宽度，其余自动处理
    public FloorMap(String name,float trueWidth,float floorHeight,PointF initPoint,int coordinateSystem){
        this.name=name;
        this.trueWidth=trueWidth;
        getImageSizeFromAssets(); //自动读取图片大小
        this.scale=this.trueWidth/(float)this.imageWidth;
        this.initPoint=initPoint;
        this.coordinateSystem=CoordinateSystem.BOTTOM_LEFT;
    }

    //楼层地图默认构造方法，需要指定地图图片文件名称，实际宽高度，图片宽高度，并要求实际宽高度与图片宽高度等比例
    public FloorMap(String name, float trueWidth, float trueHeight, int imageWidth, int imageHeight){
      this(name,trueWidth,trueHeight,imageWidth,imageHeight,new PointF(0,0),CoordinateSystem.BOTTOM_LEFT);
    }

    //需要给定图片文件名、实际宽度、图片宽度确定一个楼层地图
    public FloorMap(String name, float trueWidth, int imageWidth){
     this(name,trueWidth,0,imageWidth,0,new PointF(0,0),CoordinateSystem.BOTTOM_LEFT);
    }

    //补充构造方法，指定初始点
    public FloorMap(String name, float trueWidth, int imageWidth, PointF initPoint){
       this(name,trueWidth,0,imageWidth,0,initPoint,CoordinateSystem.BOTTOM_LEFT);
    }

    //补充构造方法，指定坐标系
    public FloorMap(String name, float trueWidth, int imageWidth, int coordinateSystem){
        this(name,trueWidth,0,imageWidth,0,new PointF(0,0),coordinateSystem);
    }

    //补充构造方法，指定初始点及坐标系
    public FloorMap(String name, float trueWidth, int imageWidth, PointF initPoint,int coordinateSystem){
        this(name,trueWidth,0,imageWidth,0,initPoint,coordinateSystem);
    }

    //补充构造方法，指定坐标系
    public FloorMap(String name, float trueWidth, float trueHeight, int imageWidth, int imageHeight,PointF initPoint, int coordinateSystem){
        this.name = name;
        this.trueWidth=trueWidth;
        this.trueHeight=trueHeight;
        this.imageWidth=imageWidth;
        this.imageHeight=imageHeight;
        this.scale=this.trueWidth/(float)this.imageWidth;
        this.initPoint=initPoint;
        this.coordinateSystem=coordinateSystem;
    }

    /**
     *
     * @param name:图片名称
     * @param scale：比例尺
     * @param initPoint：初始点
     * @param coordinateSystem：坐标系
     */
    public FloorMap(String name,float scale,PointF initPoint,int coordinateSystem){
        this.name = name;
        this.scale = scale;
        this.initPoint = initPoint;
        this.coordinateSystem = coordinateSystem;
        getImageSizeFromAssets();
    }

    //自动获取图片的大小
    private void getImageSizeFromAssets() {
        //初始化时获取图片大小，要求必须在程序启动时执行
        AssetManager assetManager = InitActivity.initActivity.getApplicationContext().getAssets();
        try {
            InputStream inputStream = assetManager.open(name);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            this.imageHeight = bitmap.getHeight();
            this.imageWidth = bitmap.getWidth();
            Log.i(TAG, "getImageSizeFromAssets: +name "+name+ " imageHeight"+imageHeight+" imageWidth"+imageWidth);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //实际点坐标坐标转换函数，包括实际坐标系平移、转换为图片像素坐标、图片坐标系转换
    public PointF trueCoordToImageCoord(PointF trueCoord){
        //初始点平移
        trueCoord.x+=initPoint.x;
        trueCoord.y+=initPoint.y;
        //实际坐标与地图坐标比例尺转换，由于比例尺为真实距离除以图片像素距离，所以这里也应当是相除
        trueCoord.x/=scale;
        trueCoord.y/=scale;
        //图片像素坐标系转换,此处要求已知imageWidth,imageHeight
        trueCoord=convertPointByCorSys(trueCoord,coordinateSystem,imageWidth,imageHeight);
        return trueCoord;
    }

    //实际点坐标坐标转换函数，包括实际坐标系平移、转换为图片像素坐标、图片坐标系转换
    public PointF imageCoordToTrueCoord(PointF imageCoord){
        //图片像素坐标系转换,此处要求已知imageWidth,imageHeight
        imageCoord=convertPointByCorSys(imageCoord,coordinateSystem,imageWidth,imageHeight);
        //实际坐标与地图坐标比例尺转换，由于比例尺为真实距离除以图片像素距离，所以这里也应当是相除
        imageCoord.x*=scale;
        imageCoord.y*=scale;
        //初始点平移
        imageCoord.x-=initPoint.x;
        imageCoord.y-=initPoint.y;
        return imageCoord;
    }

    //按照指定的coordinateSystem与默认的TOP_LEFT坐标进行坐标转换,
    private PointF convertPointByCorSys(PointF point, int coordinateSystem, int imageWidth, int imageHeight) {
        PointF newPoint=point;
        switch (coordinateSystem) {
            case CoordinateSystem.TOP_LEFT:
                break;
            case CoordinateSystem.TOP_RIGHT:
                newPoint.set((float) imageWidth - point.x, point.y);
                break;
            case CoordinateSystem.BOTTOM_LEFT:
                Log.d(TAG, "imageHeight is : "+imageHeight);
                newPoint.set(point.x, (float) imageHeight - point.y);
                break;
            case CoordinateSystem.BOTTOM_RIGHT:
                newPoint.set((float) imageWidth - point.x, (float) imageHeight - point.y);
                break;
        }
        return newPoint;
    }


}
