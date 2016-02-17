package jp.os.kz.android.barcodescanner;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

class OverlayView extends View {
	
	private int width, height; 
	//private int camerawidth, cameraheight;
	private int centerview;
	
	public OverlayView(Context context,SurfaceView surfaceview) {
		super(context);
		// TODO Auto-generated constructor stub
		setFocusable(true);
		//camerawidth=surfaceview.getWidth();
		//cameraheight=surfaceview.getHeight();
		Log.d("OverlayView", "OverlayViewCameraSize = " + "Width:" + width + 
				  " | Height:" + height);  
	}
	 
	 @Override  
	 protected void onSizeChanged(int w, int h, int oldw, int oldh){  
	  // ビューのサイズを取得  
	  width= w;  
	  height= h;
	  centerview = (int)width*7/10;
	  Log.d("OverlayView", "centerview = " + centerview);  
	 }
	 
	 /** 
	  * 描画処理 
	  */  
	 @Override  
	 protected void onDraw(Canvas canvas) {  
	  super.onDraw(canvas);
	  // 背景色を設定  
	  canvas.drawColor(Color.TRANSPARENT);  
	  
	  // 描画するための線の色を設定  
	  Paint paint = new Paint();
	  
	  //paint.setStyle(Paint.Style.FILL);
	  
	  paint.setARGB(100, 0, 0, 0);  
	  
	  Log.d("OverlayView", "OverlayViewSize = " + "Width:" + width + 
			  " | Height:" + height);
	  // 上枠表示
	  canvas.drawRect(
			  width, //右位置
			  0, //上位置
			  0, //左位置
			  (height-centerview)/2, //下位置
			  paint);
	  // 下枠表示
	  canvas.drawRect(
			  width, //右位置
			  (centerview + height)/2, //上位置
			  0, //左位置
			  height, //下位置
			  paint);
	  // 左枠表示
	  canvas.drawRect(
			  (width-centerview)/2, //右位置
			  (height-centerview)/2, //上位置
			  0, //左位置
			  (centerview + height)/2, //下位置
			  paint);  
	  // 右枠表示
	  canvas.drawRect(
			  width, //右位置
			  (height-centerview)/2, //上位置
			  (centerview + width)/2, //左位置
			  (centerview + height)/2, //下位置
			  paint);  
	  
	  //中央線の表示
	  paint.setARGB(255, 255, 0, 0);
	  canvas.drawLine((width-centerview)/2, height/2, (width+centerview)/2, height/2, paint);
	  
	  //スキャンモードの表示
      paint.setColor(Color.WHITE);
      paint.setTextSize(32);
      paint.setAntiAlias(true);
      int readMode= MainActivity.readMode;
      String modeTitle = null;
      Resources res = getResources();
      if (readMode == MainActivity.MODE_NORMAL){
    	  modeTitle=res.getString(R.string.mode_normal_title);
	  }else if (readMode == MainActivity.MODE_REVERSAL){
		  modeTitle = res.getString(R.string.mode_reversal_title);
	  }else if (readMode == MainActivity.MODE_BYTEDATA){
		  modeTitle = res.getString(R.string.mode_bytedata_title);
	  }else if (readMode == MainActivity.MODE_REVERSAL_BYTEDATA){
		  modeTitle = res.getString(R.string.mode_reversal_bytedata_title);
	  }
      float textWidth = paint.measureText(modeTitle);
      canvas.drawText(
    		  modeTitle,
                      (int)(width-textWidth)/2, //水平位置
                      (height-centerview)/4, //垂直位置
                      paint);
	  
	  // 中央十字表示  
	  //int len = height / 16;  
	  //paint.setARGB(255, 255, 0, 0);  
	  //canvas.drawLine(width/2, height/2-len, width/2, height/2+len, paint);  
	  //canvas.drawLine(width/2-len, height/2, width/2+len, height/2, paint);  
	  // 円表示  
	  //paint.setStyle(Paint.Style.STROKE);  
	  //canvas.drawCircle(width/2, height/2, len*5, paint);  
	 }

}
