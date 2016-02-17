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
	
	private Activity parentActivity; //親アクティビティ
	private Camera camera;
	
	boolean DecodeResult;
	String DecodeData;
	
	//理想的なプレビューサイズを設定する時の横と縦の比率（w/h）差の許容範囲
    private final float ASPECT_TOLERANCE = 0.05f;
	
    /**
     * コンストラクタ
     */
	public CameraPreview(Context context) {
        super(context);

        // サーフェスホルダーの取得とコールバック通知先の設定
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        this.setParentActivity((Activity) context);
        
        DecodeResult = false;
        DecodeData = "";
    }

    /**
     * アクティビティをセットする
     * @param activity
     */
    public void setParentActivity(Activity activity) {
        this.parentActivity = activity;
    }
	
    /**
     * SurfaceView 生成
     */
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            // カメラを開く
        	this.camera = Camera.open();
            // カメラインスタンスに、画像表示先を設定
        	this.camera.setPreviewDisplay(holder);
            // プレビュー開始
        	this.camera.startPreview();
        } catch (IOException e) {
            //
        }
    }

    /**
     * SurfaceView 破棄
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
     * SurfaceHolder が変化したときのイベント
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // 画面回転に対応する場合は、ここでプレビューを停止し、
        // 回転による処理を実施、再度プレビューを開始する。

    	Log.d("CameraPreview", "CameraViewSize = " + "Width:" + width + 
				  " | Height:" + height);  
    	
    	// 何かする前に一度プレビューを停止する
        this.camera.stopPreview();
    	
    	// カメラパラメータを取り出す
        Camera.Parameters params = this.camera.getParameters();
        
        //自身のパラメータを取り出す
    	ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
 
        // ベストなプレビューサイズを探す
        // 
        // 端末のサポートしているプレビューサイズを取り出す
        List<Size> listSize = params.getSupportedPreviewSizes();
         
        // 一番つ劣化が少ない画像のサイズ
        Size bestPrevSize = this.getBestPreviewSize(listSize, width, height);
    	Log.d("CameraPreview", "bestPrevSize = " + "Width:" + bestPrevSize.width + 
				  " | Height:" + bestPrevSize.height);   
        
        // プレビューサイズを設定する
        params.setPreviewSize(bestPrevSize.width, bestPrevSize.height);
        
        //自身のサイズをカメラプレビュー合わせる
        layoutParams.height=bestPrevSize.width;
        layoutParams.width=bestPrevSize.height;
        this.setLayoutParams(layoutParams);
        
    	// カメラの回転角度をセットする
    	setCameraDisplayOrientation(this.parentActivity,this.camera);
    	
    	// オートフォーカスの設定
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
         
        // パラメーターの更新
        this.camera.setParameters(params);
         
        // プレビューを再開
        this.camera.startPreview();
    }
    
    /**
     * 理想に近いサイズをプレビューサイズの中から探し出す
     * @param listPreviewSize サポートされているプレビューのサイズ
     * @param w 画面幅
     * @param h 画面の高さ
     * @return 適したプレビューサイズ
     */
    private Size getBestPreviewSize(List<Size> listPreviewSize, int w, int h) {
         
        // プレビューサイズリストがなかったときは何もしない
        if (listPreviewSize == null) {
            return null;
        }
         
        // 端末が立った状態の場合はWとHを入れ替える
        if (w < h) {
            int tmp = w;
            w = h;
            h = tmp;
        }
         
        float bestRatio = (float)w / h; // この比率に近いものをリストから探す
        float minHeightDiff = Float.MAX_VALUE;  // 一番高さに差がないもの
        int bestHeight = h; // プレビュー画面にベストな高さ
        float currRatio = 0;    // 今見ているもののアスペクト比
        Size bestSize = null;
         
        // 近いサイズのものを探す
        for (Size curr : listPreviewSize) {
             
            // 今見ているもののアスペクト比
            currRatio = (float)curr.width / curr.height;
             
            // 許容範囲を超えちゃってるやつは無視
            if (ASPECT_TOLERANCE < Math.abs(currRatio - bestRatio)) {
                continue;
            }
             
            // 前に見たやつより高さの差が少ない
            if (Math.abs(curr.height - bestHeight) < minHeightDiff) {
                 
                // 一番いいサイズの更新
                bestSize = curr;
                 
                // 今のところこれが一番差が少ない
                minHeightDiff = Math.abs(curr.height - bestHeight);
            }
        }
         
        // 理想的なものが見つからなかった場合、しょうがないので画面に入るようなやつを探しなおす
        if (bestSize == null) {
             
            // でっかい値をいれとく（未使用です）
            minHeightDiff = Float.MAX_VALUE;
 
            // 今度は画面に入りそうなものを探す
            for (Size curr : listPreviewSize) {
                 
                // 今見ているもののアスペクト比
                currRatio = (float)curr.width / curr.height;
                 
                // 前に見たやつより高さの差が少ない
                if (Math.abs(curr.height - bestHeight) < minHeightDiff) {
                     
                    // 一番いいサイズの更新
                    bestSize = curr;
                     
                    // 今のところこれが一番差が少ない
                    minHeightDiff = Math.abs(curr.height - bestHeight);
                }
            }
        }
         
        return bestSize;
    }
    
   /**
    * 画面の回転角度を設定する
    * @param activity アクティビティ
    * @param camera カメラ
    */
   public static void setCameraDisplayOrientation(
           Activity activity, Camera camera) {
        
       // 向きを設定
       camera.setDisplayOrientation(getCameraDisplayOrientation(activity));
   }
   
   /**
    * 画面の回転角度を取り出す
    * @param activity アクティビティ
    * @return 画面の回転角度
    */
   public static int getCameraDisplayOrientation(Activity activity) {
        
       // ディスプレイの回転角を取り出す
       int rot = activity.getWindowManager().getDefaultDisplay().getRotation();
        
       // 回転のデグリー角
       int degree = 0;
        
       // 取り出した角度から実際の角度への変換
       switch (rot) {
       case Surface.ROTATION_0:    degree = 0;     break;
       case Surface.ROTATION_90:   degree = 90;    break;
       case Surface.ROTATION_180:  degree = 180;   break;
       case Surface.ROTATION_270:  degree = 270;   break;
       }
        
       // 背面カメラだけの処理になるけど、画像を回転させて縦持ちに対応
       return (90 + 360 - degree) % 360;
   }
   /**
   * シャッターを押す
   */
   public void shutter() {
	   DecodeResult = false;
	   DecodeData = "";
	   this.camera.autoFocus(this.autofocusListener);
	   }
   
   // オートフォーカスリスナー
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
		   		   
		   //カメラパラメータを取り出す　カメラは横向きが基本であるため、縦向きアプリでは縦横を逆にする
		   Camera.Parameters params = camera.getParameters();
		   Size pvSize = params.getPreviewSize();
		   int pvWidth = pvSize.width;// プレビューの幅
		   int pvHeight = pvSize.height;// プレビューの高さ
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
				   //クリップボードに格納するClipDataオブジェクトの作成
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
				   //ファイルオブジェクト作成
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
