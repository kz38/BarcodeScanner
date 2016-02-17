package jp.os.kz.android.barcodescanner;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.Toast;

class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	
	private Activity parentActivity; //�e�A�N�e�B�r�e�B
	private Camera camera;
	
	boolean DecodeResult;
	String DecodeData;
	
	//���z�I�ȃv���r���[�T�C�Y��ݒ肷�鎞�̉��Əc�̔䗦�iw/h�j���̋��e�͈�
    private final float ASPECT_TOLERANCE = 0.05f;
	
    /**
     * �R���X�g���N�^
     */
	public CameraPreview(Context context) {
        super(context);

        // �T�[�t�F�X�z���_�[�̎擾�ƃR�[���o�b�N�ʒm��̐ݒ�
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        this.setParentActivity((Activity) context);
        
        DecodeResult = false;
        DecodeData = "";
    }

    /**
     * �A�N�e�B�r�e�B���Z�b�g����
     * @param activity
     */
    public void setParentActivity(Activity activity) {
        this.parentActivity = activity;
    }
	
    /**
     * SurfaceView ����
     */
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            // �J�������J��
        	this.camera = Camera.open();
            // �J�����C���X�^���X�ɁA�摜�\�����ݒ�
        	this.camera.setPreviewDisplay(holder);
            // �v���r���[�J�n
        	this.camera.startPreview();
        } catch (IOException e) {
            //
        }
    }

    /**
     * SurfaceView �j��
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d("CameraPreview", "surfaceDestroyed");
		if (this.camera != null) {
			this.camera.stopPreview();
			this.camera.release();
			this.camera = null;
		}
    }

    /**
     * SurfaceHolder ���ω������Ƃ��̃C�x���g
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // ��ʉ�]�ɑΉ�����ꍇ�́A�����Ńv���r���[���~���A
        // ��]�ɂ�鏈�������{�A�ēx�v���r���[���J�n����B

    	Log.d("CameraPreview", "CameraViewSize = " + "Width:" + width + 
				  " | Height:" + height);  
    	
    	// ��������O�Ɉ�x�v���r���[���~����
        this.camera.stopPreview();
    	
    	// �J�����p�����[�^�����o��
        Camera.Parameters params = this.camera.getParameters();
        
        //���g�̃p�����[�^�����o��
    	ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
 
        // �x�X�g�ȃv���r���[�T�C�Y��T��
        // 
        // �[���̃T�|�[�g���Ă���v���r���[�T�C�Y�����o��
        List<Size> listSize = params.getSupportedPreviewSizes();
         
        // ��Ԃ򉻂����Ȃ��摜�̃T�C�Y
        Size bestPrevSize = this.getBestPreviewSize(listSize, width, height);
    	Log.d("CameraPreview", "bestPrevSize = " + "Width:" + bestPrevSize.width + 
				  " | Height:" + bestPrevSize.height);   
        
        // �v���r���[�T�C�Y��ݒ肷��
        params.setPreviewSize(bestPrevSize.width, bestPrevSize.height);
        
        //���g�̃T�C�Y���J�����v���r���[���킹��
        layoutParams.height=bestPrevSize.width;
        layoutParams.width=bestPrevSize.height;
        this.setLayoutParams(layoutParams);
        
    	// �J�����̉�]�p�x���Z�b�g����
    	setCameraDisplayOrientation(this.parentActivity,this.camera);
    	
    	// �I�[�g�t�H�[�J�X�̐ݒ�
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
         
        // �p�����[�^�[�̍X�V
        this.camera.setParameters(params);
         
        // �v���r���[���ĊJ
        this.camera.startPreview();
    }
    
    /**
     * ���z�ɋ߂��T�C�Y���v���r���[�T�C�Y�̒�����T���o��
     * @param listPreviewSize �T�|�[�g����Ă���v���r���[�̃T�C�Y
     * @param w ��ʕ�
     * @param h ��ʂ̍���
     * @return �K�����v���r���[�T�C�Y
     */
    private Size getBestPreviewSize(List<Size> listPreviewSize, int w, int h) {
         
        // �v���r���[�T�C�Y���X�g���Ȃ������Ƃ��͉������Ȃ�
        if (listPreviewSize == null) {
            return null;
        }
         
        // �[������������Ԃ̏ꍇ��W��H�����ւ���
        if (w < h) {
            int tmp = w;
            w = h;
            h = tmp;
        }
         
        float bestRatio = (float)w / h; // ���̔䗦�ɋ߂����̂����X�g����T��
        float minHeightDiff = Float.MAX_VALUE;  // ��ԍ����ɍ����Ȃ�����
        int bestHeight = h; // �v���r���[��ʂɃx�X�g�ȍ���
        float currRatio = 0;    // �����Ă�����̂̃A�X�y�N�g��
        Size bestSize = null;
         
        // �߂��T�C�Y�̂��̂�T��
        for (Size curr : listPreviewSize) {
             
            // �����Ă�����̂̃A�X�y�N�g��
            currRatio = (float)curr.width / curr.height;
             
            // ���e�͈͂𒴂�������Ă��͖���
            if (ASPECT_TOLERANCE < Math.abs(currRatio - bestRatio)) {
                continue;
            }
             
            // �O�Ɍ������荂���̍������Ȃ�
            if (Math.abs(curr.height - bestHeight) < minHeightDiff) {
                 
                // ��Ԃ����T�C�Y�̍X�V
                bestSize = curr;
                 
                // ���̂Ƃ��낱�ꂪ��ԍ������Ȃ�
                minHeightDiff = Math.abs(curr.height - bestHeight);
            }
        }
         
        // ���z�I�Ȃ��̂�������Ȃ������ꍇ�A���傤���Ȃ��̂ŉ�ʂɓ���悤�Ȃ��T���Ȃ���
        if (bestSize == null) {
             
            // �ł������l������Ƃ��i���g�p�ł��j
            minHeightDiff = Float.MAX_VALUE;
 
            // ���x�͉�ʂɓ��肻���Ȃ��̂�T��
            for (Size curr : listPreviewSize) {
                 
                // �����Ă�����̂̃A�X�y�N�g��
                currRatio = (float)curr.width / curr.height;
                 
                // �O�Ɍ������荂���̍������Ȃ�
                if (Math.abs(curr.height - bestHeight) < minHeightDiff) {
                     
                    // ��Ԃ����T�C�Y�̍X�V
                    bestSize = curr;
                     
                    // ���̂Ƃ��낱�ꂪ��ԍ������Ȃ�
                    minHeightDiff = Math.abs(curr.height - bestHeight);
                }
            }
        }
         
        return bestSize;
    }
    
   /**
    * ��ʂ̉�]�p�x��ݒ肷��
    * @param activity �A�N�e�B�r�e�B
    * @param camera �J����
    */
   public static void setCameraDisplayOrientation(
           Activity activity, Camera camera) {
        
       // ������ݒ�
       camera.setDisplayOrientation(getCameraDisplayOrientation(activity));
   }
   
   /**
    * ��ʂ̉�]�p�x�����o��
    * @param activity �A�N�e�B�r�e�B
    * @return ��ʂ̉�]�p�x
    */
   public static int getCameraDisplayOrientation(Activity activity) {
        
       // �f�B�X�v���C�̉�]�p�����o��
       int rot = activity.getWindowManager().getDefaultDisplay().getRotation();
        
       // ��]�̃f�O���[�p
       int degree = 0;
        
       // ���o�����p�x������ۂ̊p�x�ւ̕ϊ�
       switch (rot) {
       case Surface.ROTATION_0:    degree = 0;     break;
       case Surface.ROTATION_90:   degree = 90;    break;
       case Surface.ROTATION_180:  degree = 180;   break;
       case Surface.ROTATION_270:  degree = 270;   break;
       }
        
       // �w�ʃJ���������̏����ɂȂ邯�ǁA�摜����]�����ďc�����ɑΉ�
       return (90 + 360 - degree) % 360;
   }
   /**
   * �V���b�^�[������
   */
   public void shutter() {
	   DecodeResult = false;
	   DecodeData = "";
	   this.camera.autoFocus(this.autofocusListener);
	   }
   
   // �I�[�g�t�H�[�J�X���X�i�[
   private Camera.AutoFocusCallback autofocusListener = 
		   new Camera.AutoFocusCallback() {
	   @Override
	   public void onAutoFocus(boolean success, Camera camera) {
		   //camera.autoFocus(null);
		    if (success) 
		        camera.setOneShotPreviewCallback(previewCallback); 
		   }
	   };
	    
   private PreviewCallback previewCallback = new PreviewCallback() {
	   public void onPreviewFrame(byte[] data, Camera camera) {
		   		   
		   //�J�����p�����[�^�����o���@�J�����͉���������{�ł��邽�߁A�c�����A�v���ł͏c�����t�ɂ���
		   Camera.Parameters params = camera.getParameters();
		   Size pvSize = params.getPreviewSize();
		   int pvWidth = pvSize.width;// �v���r���[�̕�
		   int pvHeight = pvSize.height;// �v���r���[�̍���
		   Log.d("CameraPreview", "PreviewSize = " + "Width:" + pvSize.width + 
					  " | Height:" + pvSize.height);
		   
		   int centerViewWH = (int)pvSize.height * 8 / 10;
		   int left = (int)(pvWidth-centerViewWH)/2; 
		   int top = (int)(pvHeight-centerViewWH)/2; 
		   int width = centerViewWH; 
		   int height = centerViewWH;
		   
		   Resources res = getResources();
		   
		   ImageDecoder Decoder = new ImageDecoder(data, pvWidth, pvHeight, 
					top, left, width, height);
		   //String text = Decoder.decodeImg();
           //String text = Decoder.decodeBWRevImg(params);
           //String text = Decoder.decodeImg2(params);
		   boolean invertColors=false;
		   if ((MainActivity.readMode == MainActivity.MODE_REVERSAL) || 
				   (MainActivity.readMode == MainActivity.MODE_REVERSAL_BYTEDATA)){
			   invertColors=true;
		   }
		   
           //String text = Decoder.getString(params, invertColors);
		   String text = null;
		   if ((MainActivity.readMode == MainActivity.MODE_NORMAL)||
					(MainActivity.readMode == MainActivity.MODE_REVERSAL)) {
			   
			   MainActivity.readText = Decoder.getString(params, invertColors);
			   
			   if (MainActivity.readText != null){
				   text=MainActivity.readText;
				   ClipboardManager clipboard = 
					   (ClipboardManager)parentActivity.getSystemService(Context.CLIPBOARD_SERVICE);
				   //�N���b�v�{�[�h�Ɋi�[����ClipData�I�u�W�F�N�g�̍쐬
				   ClipData clipdata = ClipData.newPlainText("Result text",text);
				   clipboard.setPrimaryClip(clipdata);
			   }else{
				   text=res.getString(R.string.Not_Found);
			   }
		   
		   }else if ((MainActivity.readMode == MainActivity.MODE_BYTEDATA)||
				   (MainActivity.readMode == MainActivity.MODE_REVERSAL_BYTEDATA)){
			   
			   String datPath = MainActivity.dirPath + "/" + MainActivity.datFile;
			   byte[] readbyte = Decoder.getBytes(params, invertColors);
			   
			   if (readbyte.length == 0) {
				   text=res.getString(R.string.Not_Found);
			   }else{
				   //�t�@�C���I�u�W�F�N�g�쐬
				   FileOutputStream fileOutStm = null;
				   try {
					   fileOutStm = new FileOutputStream(datPath);
					   text = datPath + res.getString(R.string.SaveTo);
				   } catch (FileNotFoundException e2) {
					   text=res.getString(R.string.FileNotFound);
				   }
				   
				   try {
					   fileOutStm.write(readbyte);
				   } catch (IOException e3) {
						text=res.getString(R.string.IO_Error);
				   }   
			   }
		   }
		   Toast.makeText(parentActivity, text, Toast.LENGTH_SHORT).show();
	   	}
	   
	   };
}
