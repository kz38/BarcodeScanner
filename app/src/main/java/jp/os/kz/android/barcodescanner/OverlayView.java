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
	  // �r���[�̃T�C�Y���擾  
	  width= w;  
	  height= h;
	  centerview = (int)width*7/10;
	  Log.d("OverlayView", "centerview = " + centerview);  
	 }
	 
	 /** 
	  * �`�揈�� 
	  */  
	 @Override  
	 protected void onDraw(Canvas canvas) {  
	  super.onDraw(canvas);
	  // �w�i�F��ݒ�  
	  canvas.drawColor(Color.TRANSPARENT);  
	  
	  // �`�悷�邽�߂̐��̐F��ݒ�  
	  Paint paint = new Paint();
	  
	  //paint.setStyle(Paint.Style.FILL);
	  
	  paint.setARGB(100, 0, 0, 0);  
	  
	  Log.d("OverlayView", "OverlayViewSize = " + "Width:" + width + 
			  " | Height:" + height);
	  // ��g�\��
	  canvas.drawRect(
			  width, //�E�ʒu
			  0, //��ʒu
			  0, //���ʒu
			  (height-centerview)/2, //���ʒu
			  paint);
	  // ���g�\��
	  canvas.drawRect(
			  width, //�E�ʒu
			  (centerview + height)/2, //��ʒu
			  0, //���ʒu
			  height, //���ʒu
			  paint);
	  // ���g�\��
	  canvas.drawRect(
			  (width-centerview)/2, //�E�ʒu
			  (height-centerview)/2, //��ʒu
			  0, //���ʒu
			  (centerview + height)/2, //���ʒu
			  paint);  
	  // �E�g�\��
	  canvas.drawRect(
			  width, //�E�ʒu
			  (height-centerview)/2, //��ʒu
			  (centerview + width)/2, //���ʒu
			  (centerview + height)/2, //���ʒu
			  paint);  
	  
	  //�������̕\��
	  paint.setARGB(255, 255, 0, 0);
	  canvas.drawLine((width-centerview)/2, height/2, (width+centerview)/2, height/2, paint);
	  
	  //�X�L�������[�h�̕\��
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
                      (int)(width-textWidth)/2, //�����ʒu
                      (height-centerview)/4, //�����ʒu
                      paint);
	  
	  // �����\���\��  
	  //int len = height / 16;  
	  //paint.setARGB(255, 255, 0, 0);  
	  //canvas.drawLine(width/2, height/2-len, width/2, height/2+len, paint);  
	  //canvas.drawLine(width/2-len, height/2, width/2+len, height/2, paint);  
	  // �~�\��  
	  //paint.setStyle(Paint.Style.STROKE);  
	  //canvas.drawCircle(width/2, height/2, len*5, paint);  
	 }

}
