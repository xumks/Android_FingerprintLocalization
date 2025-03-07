package com.linchisin.fingerprintlocalization.ui;

/**
 * Created by Sin on 2018/4/19.
 *
 * @author LinChiSin
 * @date 2018-4-13,上午10:12:02
 * @description 地图展示及定位结果展示View
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.ArrayList;
import java.util.List;

public class MapView extends SubsamplingScaleImageView {

	private static final String TAG ="MapView" ;
	private Paint mPaint;
	private Paint mStrokePaint;
	private Path mArrowPath; // 箭头路径
	private final PointF vPoint=new PointF();
	private int circleRadius = 10; //圆点半径
	private int arrowRadius = 20; // 箭头半径

	private float mCurX = 100;
	private float mCurY = 2480;
	private int mOrient;

	private List<PointF> mPointList = new ArrayList<>();

	public MapView(Context context) {
		this(context, null);
	}
	public MapView(Context context, AttributeSet attrs) {

		super(context, attrs);
		Log.i(TAG, "initialise: 进入初始化");
		// 初始化画笔
		mPaint = new Paint();
		mPaint.setColor(Color.BLUE);
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.FILL);
		mStrokePaint = new Paint(mPaint);
		mStrokePaint.setStyle(Paint.Style.STROKE);
		mStrokePaint.setStrokeWidth(5);

		// 初始化箭头路径
//		mArrowPath = new Path();
//		mArrowPath.arcTo(new RectF(-arrowRadius, -arrowRadius, arrowRadius, arrowRadius), 0, -180);
//		mArrowPath.lineTo(0, -3 * arrowRadius);
//		mArrowPath.close();

	}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Don't draw before image is ready so it doesn't move around during setup.
		if (!isReady()) {
			return;
		}

		if(mPointList!=null&&mPointList.size()>=1) {
			Log.d(TAG, "mPointList size: "+mPointList.size());
			for (PointF p : mPointList) {
				Log.d(TAG, "currentPoint: ("+p.x+","+p.y+")");
				sourceToViewCoord(p.x,p.y,vPoint);
				canvas.drawCircle(vPoint.x, vPoint.y, circleRadius, mPaint);
			}
			canvas.save(); // 保存画布
			sourceToViewCoord(mCurX,mCurY,vPoint);
			canvas.translate(vPoint.x, vPoint.y); // 平移画布
//			canvas.rotate(mOrient); // 转动画布
//			canvas.drawPath(mArrowPath, mPaint);
			canvas.drawArc(new RectF(-arrowRadius * 0.8f, -arrowRadius * 0.8f, arrowRadius * 0.8f, arrowRadius * 0.8f),
					0, 360, false, mStrokePaint);
			canvas.restore(); // 恢复画布
		}
	}

	public void reset(){
		this.mPointList=null;
		invalidate();
	}

	/**
	 * 自动增加点
	 */
	public void autoAddPoint(float stepLen,float scale) {
		if(mPointList==null)
			mPointList=new ArrayList<>();

		float deltaX=(float) (stepLen * Math.sin(Math.toRadians(mOrient)));
		float deltaY=(float) (-stepLen * Math.cos(Math.toRadians(mOrient)));

//		mCurX += 8*scaleX*deltaX;  //比例尺转换与坐标系转换
//		mCurY += 3*scaleY*deltaY;

		mCurX += scale*deltaX;  //比例尺转换与坐标系转换
		mCurY += scale*deltaY;
		String pdrResult="△x="+deltaX+" △y="+deltaY;
		MainActivity.mainactivity.pdrResult=pdrResult;
		mPointList.add(new PointF(mCurX, mCurY));
		invalidate();
	}

	public void autoDrawArrow(int orient) {
		mOrient = orient;
		invalidate();
	}

	//此处传入是已经经过比例尺换算、坐标系换算、和初始点平移后的数据
	public void autoDrawPoint(PointF imageCoord) {
		if(mPointList==null)
			mPointList=new ArrayList<>();
		mCurX =imageCoord.x;
		mCurY = imageCoord.y;
		mPointList.add(imageCoord);
		invalidate();
	}

}


