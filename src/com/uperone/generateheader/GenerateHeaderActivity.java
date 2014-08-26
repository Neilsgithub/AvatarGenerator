package com.uperone.generateheader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class GenerateHeaderActivity extends BaseActivity {
	
	@Override
	public void setContentView() {
		setContentView(R.layout.activity_generate_header_layout);
	}

	@Override
	public void findViews() {
		mGenerateLayout = ( RelativeLayout )findViewById(R.id.generateLayoutId);
		mHeaderLayout = ( RelativeLayout )findViewById(R.id.headerLayoutId);
		mHeaderImg = ( ImageView )findViewById(R.id.headerImgId);
		mSelectBtn = ( Button )findViewById(R.id.selectBtnId);
		mOkBtn = ( Button )findViewById(R.id.okBtnId);
		mInputEditTxt = ( EditText )findViewById(R.id.inputEditTxtId);
		
		mInputEditTxt.addTextChangedListener( mTextWatcher );
	}

	@Override
	public void getData() {
		
	}

	@Override
	public void showContent() {
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			showHeaderImg( data );
        }  
        super.onActivityResult(requestCode, resultCode, data);  
	}

	public void onClick(View v) {
		switch( v.getId( ) ){
		case R.id.certainBtnId:{
			updateBitmap( );
		}
		break;
		case R.id.selectBtnId:{
			selectImage( );
		}
		break;
		case R.id.okBtnId:{
			saveBitmap( );
		}
		break;
		default:{
			
		}
		break;
		}
	}
	
	private Bitmap getScaleBitmap(Bitmap bitmap, float scale) {
		if( null == bitmap ){
			return null;
		}
		
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale); // 长和宽放大缩小的比例
		Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return resizeBmp;
	}
	
	private void updateBitmap( ){
		if( null != mBitmap ){
			if( null == mBadgeView ){
	        	mBadgeView = new BadgeView(this, mHeaderImg);
	        }
	        mBadgeView.setText( getShowIndex( ) );
	        mBadgeView.show();
		}else{
			Toast.makeText( this, getResources( ).getString( R.string.select_toast ), Toast.LENGTH_LONG ).show( );
		}
	}
	
	private void saveBitmap( ){
		if( null != mBitmap ){
			Bitmap bitmap = getViewBitmap( mHeaderLayout );
			if( null != bitmap ){
				String filePath = savePhotoToSDCard( bitmap, Environment.getExternalStorageDirectory() + "/DCIM/Camera", String.valueOf(System.currentTimeMillis()) );
				if( !TextUtils.isEmpty( filePath ) ){
					scanPhotos( filePath, this );
					Toast.makeText( this, getResources( ).getString( R.string.save_toast ) + filePath, Toast.LENGTH_LONG ).show( );
				}
			}
		}else{
			Toast.makeText( this, getResources( ).getString( R.string.select_toast ), Toast.LENGTH_LONG ).show( );
		}
	}
	
	private Bitmap getViewBitmap( View view ){
    	view.setDrawingCacheEnabled( true );
        Bitmap bitmap = null;
        try{
            if( null != view.getDrawingCache( ) ){
            	bitmap = Bitmap.createBitmap( view.getDrawingCache( ) );
            }
        }catch( OutOfMemoryError e ){
            e.printStackTrace( );
        }finally{
        	view.setDrawingCacheEnabled( false );
        	view.destroyDrawingCache( );
        }

        return bitmap;
    }
	
	private void showHeaderImg( Intent data ){
		Uri uri = data.getData();  
        Log.e("uri", uri.toString());  
        ContentResolver cr = this.getContentResolver();  
        try {  
        	mBitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
        	float height = getResources( ).getDimension( R.dimen.icon_width );
        	float scale = (mBitmap.getHeight( )*1.0f)/height;
        	
        	mBitmap = getScaleBitmap( mBitmap, scale );
            mHeaderImg.setImageBitmap( mBitmap );
            
            if( null == mBadgeView ){
            	mBadgeView = new BadgeView(this, mHeaderImg);
            }
            mBadgeView.setText( getShowIndex( ) );
            mBadgeView.show();
        } catch (FileNotFoundException e) {  
            Log.e("Exception", e.getMessage(),e);  
        }  
	}
	
	private String getShowIndex( ){
		String indexStr = "1";
		
		String inputString = getInputTxt( );
		System.out.println( "inputString ==22 " + inputString );
		if( !TextUtils.isEmpty( inputString ) ){
			int index = Integer.parseInt( inputString );
			if( index > 99 ){
				indexStr = "99+";
			}else{
				indexStr = index + "";
			}
		}
		
		return indexStr;
	}
	
	private void selectImage( ){
		Intent intent = new Intent();  
        intent.setType("image/*");  
        intent.setAction(Intent.ACTION_GET_CONTENT);   
        /* 取得相片后返回本画面 */  
        startActivityForResult(intent, 1); 
	}
	
	/**
	 * Save image to the SD card 本地相册：Environment.getExternalStorageDirectory()
	 * + "/DCIM/Camera"
	 */
	private String savePhotoToSDCard(Bitmap photoBitmap, String path, String photoName) {
		String filePath = "";
		//if (checkSDCardAvailable())
		{
			File dir = new File(path);
			if (!dir.exists()) {
				dir.mkdirs();
			}

			File photoFile = new File(path, photoName + ".png");
			FileOutputStream fileOutputStream = null;
			try {
				fileOutputStream = new FileOutputStream(photoFile);
				if (photoBitmap != null) {
					if (photoBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)) {
						fileOutputStream.flush();
						filePath = photoFile.toString();
						System.out.println(filePath);
					}
				}
			} catch (FileNotFoundException e) {
				photoFile.delete();
				e.printStackTrace();
			} catch (IOException e) {
				photoFile.delete();
				e.printStackTrace();
			} finally {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return filePath;
	}

	/**
	 * 扫描、刷新相册
	 */
	private void scanPhotos(String filePath, Context context) {
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		Uri uri = Uri.fromFile(new File(filePath));
		intent.setData(uri);
		context.sendBroadcast(intent);
	}
	
	private TextWatcher mTextWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
			
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			String inputString = getInputTxt( );
			System.out.println( "inputString ==11 " + inputString );
			if( !TextUtils.isEmpty( inputString ) ){
				mSelectBtn.setVisibility( Button.VISIBLE );
				mOkBtn.setVisibility( Button.VISIBLE );
				mGenerateLayout.setVisibility( RelativeLayout.VISIBLE );
			}else{
				mSelectBtn.setVisibility( Button.INVISIBLE );
				mOkBtn.setVisibility( Button.INVISIBLE );
				mGenerateLayout.setVisibility( RelativeLayout.INVISIBLE );
			}
		}
	};
	
	private String getInputTxt( ){
		String inputString = mInputEditTxt.getText().toString();
		
		return inputString;
	}
	
	private RelativeLayout mGenerateLayout = null;
	private RelativeLayout mHeaderLayout = null;
	private Bitmap mBitmap = null;
	private BadgeView mBadgeView = null;
	private Button mSelectBtn = null;
	private Button mOkBtn = null;
	private EditText mInputEditTxt = null;
	private ImageView mHeaderImg = null;
}
