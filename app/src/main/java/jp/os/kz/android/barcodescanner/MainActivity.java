package jp.os.kz.android.barcodescanner;

import java.io.File;
import java.io.IOException;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

public class MainActivity extends Activity {
	//�ǂݎ�胂�[�h
	static final int MODE_NORMAL = 1; 
	static final int MODE_REVERSAL = 2; //�������]
	static final int MODE_BYTEDATA = 3; //�o�C�g�f�[�^�ǎ�
	static final int MODE_REVERSAL_BYTEDATA = 4; //�o�C�g�f�[�^�ǎ�
	static int readMode = MODE_NORMAL; 
	static String readText = "";
	static byte[] readByteData = null;
	static String dirPath = "";
	static String datFile = "read.hex";
	
    // �J�����v���r���[�N���X
    private CameraPreview camerapreview = null;
    
    OverlayView overlayView; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        // FrameLayout �� CameraPreview �N���X��ݒ�
        FrameLayout preview = (FrameLayout)findViewById(R.id.CameraPreview);
        camerapreview = new CameraPreview(this);
        preview.addView(camerapreview);
        
		/*addContentView(new OverlayView(this,camerapreview), 
				new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));*/
        overlayView= new OverlayView(this,camerapreview);
        addContentView(overlayView,new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

    	dirPath = Environment.getExternalStorageDirectory() + "/BarcodeScanner";
    	File dir = new File(dirPath);
    	if (!dir.exists()){
        	dir.mkdir();
    	}
    	File dat = new File(dirPath + "/" + datFile);
    	if (!dat.exists()){
    		try {
				dat.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
    	}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// XML�Œ�`����menu���w�肷��
		/*		inflater.inflate(R.menu.main, menu);
				return true; */
		
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	/**
	 * �I�v�V�������j���[�̑I��
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		//return super.onOptionsItemSelected(item);
		
		int itemId = item.getItemId();
		//String modeTitle="";
		switch (itemId){
			case R.id.menu_mode_rotate:
				if (readMode == MODE_NORMAL){
					readMode=MODE_REVERSAL;
					//modeTitle = getString(R.string.mode_reversal_title);
				}else if (readMode == MODE_REVERSAL){
					readMode=MODE_BYTEDATA;
					//modeTitle = getString(R.string.mode_bytedata_title);
				}else if (readMode == MODE_BYTEDATA){
					readMode=MODE_REVERSAL_BYTEDATA;
					//modeTitle = getString(R.string.mode_reversal_bytedata_title);
				}else if (readMode == MODE_REVERSAL_BYTEDATA){
					readMode=MODE_NORMAL;
					//modeTitle = getString(R.string.mode_normal_title);
				}
				/*Toast toast = Toast.makeText(this, modeTitle, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();*/
				overlayView.invalidate();
		}
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
        //�C�x���g��ނ�getAction���\�b�h�Ŏ擾
		switch ( event.getAction() ) {
		
		 	//��ʂ��^�b�`���ꂽ�Ƃ��̓���
			case MotionEvent.ACTION_DOWN:
				camerapreview.shutter();
				break;
				
				//�^�b�`�����܂܈ړ������Ƃ��̓���
            case MotionEvent.ACTION_MOVE:
            	Log.d("Motion", "ACTION_MOVE");
            	break;
            
            //�^�b�`�������ꂽ�Ƃ��̓���	
            case MotionEvent.ACTION_UP:
            	Log.d("Motion", "ACTION_UP");
            	break;
            	
            //�^�b�`���L�����Z�����ꂽ�Ƃ��̓���
            case MotionEvent.ACTION_CANCEL:
            	Log.d("Motion", "ACTION_CANCEL");
            	break;
        }
        return super.onTouchEvent(event); 
    } 
}
