package jp.os.kz.android.barcodescanner;

import java.io.ByteArrayOutputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

class ImageDecoder {
	//static final String NOT_FOUND = "Not Found";
	
	private final byte[] srcData;
	private final int srcWidth;
	private final int srcHeight;
	private final int scopePositionTop;
	private final int scopePositionLeft;
	private final int scopeWidth;
	private final int scopeHeight;
	private int[] rgb;
	
	public ImageDecoder(byte[] srcData, int srcWidth, int srcHeight, 
			int scopePositionTop, int scopePositionLeft, int scopeWidth, int scopeHeight) {
		
		this.srcData = srcData;
		this.srcWidth = srcWidth;
		this.srcHeight = srcHeight;
		this.scopePositionTop = scopePositionTop;
		this.scopePositionLeft = scopePositionLeft;
		this.scopeWidth = scopeWidth;
		this.scopeHeight = scopeHeight;
		this.rgb = new int[(srcWidth * srcHeight)]; // ARGB8888の画素の配列
		
		Log.d("QRCodeDecder", "ImageInfo : " + 
	     	   ", srcWidth = " + this.srcWidth +
     		   ", srcHeight = " +this.srcHeight +
     		   ", scopePositionTop = " + this.scopePositionTop +
     		   ", scopePositionLeft = " + this.scopePositionLeft +
     		   ", scopeWidth = " + this.scopeWidth +
     		   ", scopeHeight = " + this.scopeHeight);
	}

	int[] getRGB(){
		return this.rgb;
	}
	
	/*カメラの画像データが横向きである為、縦向きの状態では読み込まない*/
	String decodeImg(){
		String retStr = "";

		// プレビューデータから BinaryBitmap を生成
		PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
				this.srcData, this.srcWidth, this.srcHeight, 
				this.scopePositionLeft, this.scopePositionTop, 
				this.scopeWidth, this.scopeHeight, false);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		
		// バーコードを読み込む
		Reader reader = new MultiFormatReader();
		Result result = null;
		try {
			result = reader.decode(bitmap);
			retStr = result.getText();
		}catch (Exception e) {
			retStr = null;
			}
		return retStr;
	}
	
	String getString(Camera.Parameters params,boolean invert){
		String ret = "";
		Bitmap bmp = getBitmap(params);
		int bmpWidth = bmp.getWidth(), bmpHeight = bmp.getHeight();
		int[] pixels = new int[bmpWidth * bmpHeight];
		bmp.getPixels(pixels, 0, bmpWidth, 0, 0, bmpWidth, bmpHeight);
		
		//色反転
		if (invert == true){
			invertColors(pixels,bmpWidth,bmpHeight);
		}
		
		bmp.recycle();
		bmp = null;
		RGBLuminanceSource source = new RGBLuminanceSource(bmpWidth, bmpHeight, pixels);
		BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
		MultiFormatReader reader = new MultiFormatReader();
           
		Result result = null;
		try {
			result = reader.decode(bBitmap);
			ret = result.getText();
			//バイトデータとして取得するgetRawBytes()メソッド
		}catch (Exception e) {
			ret = null;
			}
		return ret;
	}

	byte[] getBytes(Camera.Parameters params,boolean invert){
		byte[] ret ;
		Bitmap bmp = getBitmap(params);
		int bmpWidth = bmp.getWidth(), bmpHeight = bmp.getHeight();
		int[] pixels = new int[bmpWidth * bmpHeight];
		bmp.getPixels(pixels, 0, bmpWidth, 0, 0, bmpWidth, bmpHeight);
		
		//色反転
		if (invert == true){
			invertColors(pixels,bmpWidth,bmpHeight);
		}
		
		bmp.recycle();
		bmp = null;
		RGBLuminanceSource source = new RGBLuminanceSource(bmpWidth, bmpHeight, pixels);
		BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
		MultiFormatReader reader = new MultiFormatReader();
           
		Result result = null;
		try {
			result = reader.decode(bBitmap);
			ret = result.getRawBytes();
			//バイトデータとして取得するgetRawBytes()メソッド
		}catch (Exception e) {
			ret = new byte[]{};
			}
		return ret;
	}
	
	private Bitmap getBitmap(Camera.Parameters params){
		YuvImage yuvImage = new YuvImage(this.srcData, params.getPreviewFormat(),
				   this.srcWidth, this.srcHeight, null);
		Log.d("CameraPreview", "PreviewFormat = " + params.getPreviewFormat());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		yuvImage.compressToJpeg(new Rect(this.scopePositionLeft, this.scopePositionTop, 
				this.scopePositionLeft + this.scopeWidth, 
				this.scopePositionTop + this.scopeHeight),
				50, out);
				
		byte[] imageBytes = out.toByteArray();
		Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

		return bmp;
	}
	
	private int[] invertColors(int[] src, int width, int height){
		int[] Ret = src;
		//色反転
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				Ret[x + y * width] ^= 0x00ffffff;
			}
		}
		return Ret;
	}

}
