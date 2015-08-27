package com.istroop.istrooprecognize.ui.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.istroop.istrooprecognize.BaseActivity;
import com.istroop.istrooprecognize.IstroopConstants;
import com.istroop.istrooprecognize.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ActionSheet extends BaseActivity {

    private static final int    PHOTO_REQUEST_TAKEPHOTO = 1;// 拍照
    private static final int    PHOTO_REQUEST_GALLERY   = 2;// 从相册中选择
    private static final int    PHOTO_REQUEST_CUT       = 3;// 结果
    private static final String TAG                     = "ActionSheet";
    private              File   tempFile                = new File( IstroopConstants.PICTURE_PATH,
                                                                    "ichaotu_upload.jpg" );
    private String image_type;
    private boolean isCamera = false;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.actionsheet );
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        image_type = bundle.getString( "image_type" );
        IstroopConstants.absolutePath = tempFile.getAbsolutePath();
    }

    /*
     * @Override public boolean onTouchEvent(MotionEvent event){ finish();
     * return true; }
     */
    public void photo( View v ) {
        Log.i( TAG, "照相机照相" );
        Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
        intent.putExtra( MediaStore.EXTRA_OUTPUT, Uri.fromFile( tempFile ) );
        startActivityForResult( intent, PHOTO_REQUEST_TAKEPHOTO );
        Log.i( TAG, "照相机照相结束+返回结果" );
        // this.setResult(RESULT_OK);
        // this.finish();
    }

    public void album( View v ) {
        Intent intent = new Intent( Intent.ACTION_PICK, null );
        intent.setDataAndType( MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                               "image/*" );
        Log.i( TAG, "从相册选取图片" );
        startActivityForResult( intent, PHOTO_REQUEST_GALLERY );
        Log.i( TAG, "从相册选取图片结束+返回结果" );
        // this.setResult(RESULT_FIRST_USER);
        // this.finish();
    }

    public void quit( View v ) {
        this.setResult( RESULT_FIRST_USER );
        this.finish();
    }

    @Override
    protected void onSaveInstanceState( @NonNull Bundle outState ) {
        super.onSaveInstanceState( outState );
        Log.i( TAG, "outState:" + outState );
        SharedPreferences sp = getSharedPreferences( "tempfile", MODE_PRIVATE );
        Editor editor = sp.edit();
        editor.putString( "absolutePath", IstroopConstants.absolutePath );
        editor.apply();
    }

    @Override
    protected void onRestoreInstanceState( @NonNull Bundle savedInstanceState ) {
        super.onRestoreInstanceState( savedInstanceState );
        SharedPreferences sp = getSharedPreferences( "tempfile", MODE_PRIVATE );
        IstroopConstants.absolutePath = sp.getString( "absolutePath", "" );
        Log.i( TAG, "savedInstanceState:" + IstroopConstants.absolutePath );
    }

    @Override
    public void onConfigurationChanged( Configuration newConfig ) {
        super.onConfigurationChanged( newConfig );
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult( requestCode, resultCode, data );
        Log.i( TAG, "照相机返回的结果resultCode:" + resultCode );
        Log.i( TAG, "照相机返回的结果requestCode:" + requestCode );
        if ( 0 == resultCode ) {
            this.setResult( RESULT_FIRST_USER );
            this.finish();
        }
        IstroopConstants.data = data;
        switch ( requestCode ) {
            case PHOTO_REQUEST_TAKEPHOTO:
                if ( resultCode == 0 ) {
                    // 没有进行照相操作
                    this.finish();
                    return;
                }
                Log.i( TAG, "图片的路径" + Uri.fromFile( tempFile ) );
                Log.i( TAG, "进入裁剪图片+照相机" );
                if ( "更换头像".equals( image_type ) ) {
                    isCamera = true;
                    Log.i( TAG, "更换头像" );
                    startPhotoZoom( Uri.fromFile( tempFile ) );
                } else if ( "更换背景图".equals( image_type ) ) {
                    isCamera = true;
                    Log.i( TAG, "更换背景图" );
                    startPicZoom( Uri.fromFile( tempFile ) );
                    this.finish();
                } else if ( "首页背景图片".equals( image_type ) ) {
                    isCamera = true;
                    Log.i( TAG, "首页背景图片" );
                    homePhotoZoom( Uri.fromFile( tempFile ) );
                } else {
                    startPhotoZoom( Uri.fromFile( tempFile ) );
                }
                break;

            case PHOTO_REQUEST_GALLERY:
                if ( data != null ) {
                    if ( "更换头像".equals( image_type ) ) {
                        startPhotoZoom( data.getData() );
                    } else if ( "更换背景图".equals( image_type ) ) {
                        isCamera = false;
                        startPicZoom( data.getData() );
                        this.finish();
                    } else if ( "首页背景图片".equals( image_type ) ) {
                        Log.i( TAG, "首页背景图片" );
                        homePhotoZoom( data.getData() );
                    } else {
                        startPhotoZoom( data.getData() );
                    }
                }
                Log.i( TAG, "相册返回的数据:" + data );
                Log.i( TAG, "进入裁剪图片" );
                break;

            case PHOTO_REQUEST_CUT:
                Log.i( TAG, "裁剪后返回的数据--------------------------------:" + data );
                if ( data != null ) {
                    setPicToView( data );
                    Log.i( TAG, "向界面显示图片" );
                }
            /*
             * String imageUrl =
			 * IstroopConstants.PICTURE_PATH+"/ichaotu_upload.jpg"; Intent
			 * intent = new Intent(); Bundle bundleImage = new Bundle();
			 * bundleImage.putString("image", imageUrl);
			 * intent.putExtras(bundleImage); setResult(100, intent);
			 */
                this.finish();
                break;
        }

    }

    private void startPicZoom( Uri uri ) {
        Log.i( TAG, "选取图片返回的结果的路径" + uri );
        String imageUrl;
        if ( isCamera ) {
            imageUrl = IstroopConstants.PICTURE_PATH + "/ichaotu_upload.jpg";
        } else {
            imageUrl = getImageUrl( this, uri );
        }
        Log.i( TAG, "图片的路径:" + imageUrl );
        if ( imageUrl != null ) {
            isCamera = false;
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString( "dataImage", imageUrl );
            bundle.putString( "image", "image" );
            intent.putExtras( bundle );
            setResult( 100, intent );
        } else {
            Toast.makeText( this, "出错,请重新选择", Toast.LENGTH_SHORT ).show();
        }
    }

    private void startPhotoZoom( Uri uri ) {
        Log.i( TAG, "裁剪图片获取的uri" + uri );
        Log.i( TAG, "开始裁剪图片" );
        Intent intent = new Intent( "com.android.camera.action.CROP" );
        intent.setDataAndType( uri, "image/*" );
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra( "crop", "true" );

        // aspectX aspectY 是宽高的比例
        intent.putExtra( "aspectX", 1 );
        intent.putExtra( "aspectY", 1 );

        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra( "outputX", 250 );
        intent.putExtra( "outputY", 250 );
        intent.putExtra( "scale", true );
        intent.putExtra( MediaStore.EXTRA_OUTPUT, uri );
        intent.putExtra( "return-data", true );
        Log.i( TAG, "结束裁剪图片" );
        intent.putExtra( "outputFormat", Bitmap.CompressFormat.JPEG.toString() );// 图片格式
        intent.putExtra( "noFaceDetection", true );
        startActivityForResult( intent, PHOTO_REQUEST_CUT );
        Log.i( TAG, "将裁剪的图片在界面上显示" );
    }

    private void homePhotoZoom( Uri uri ) {
        Log.i( TAG, "裁剪图片获取的uri" + uri );
        Log.i( TAG, "开始裁剪图片" );
        Intent intent = new Intent( "com.android.camera.action.CROP" );
        intent.setDataAndType( uri, "image/*" );
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra( "crop", "true" );

        // aspectX aspectY 是宽高的比例
        intent.putExtra( "aspectX", 2 );
        intent.putExtra( "aspectY", 1.65 );

        intent.putExtra( "outputX", 410 );
        intent.putExtra( "outputY", 250 );

        intent.putExtra( "scale", true );
        intent.putExtra( MediaStore.EXTRA_OUTPUT, uri );
        intent.putExtra( "return-data", true );
        Log.i( TAG, "结束裁剪图片" );
        intent.putExtra( "outputFormat", Bitmap.CompressFormat.JPEG.toString() );// 图片格式
        intent.putExtra( "noFaceDetection", true );
        startActivityForResult( intent, PHOTO_REQUEST_CUT );
        Log.i( TAG, "将裁剪的图片在界面上显示" );
    }

    // 将进行剪裁后的图片显示到UI界面上
    private void setPicToView( Intent picdata ) {
        Log.i( TAG, "携带data数据向设计界面显示图片" + picdata );
        Bundle bundle = picdata.getExtras();
        if ( bundle != null ) {
            Bitmap photo = bundle.getParcelable( "data" );
            savePic( photo );
            byte[] bitmap2Bytes = Bitmap2Bytes( photo );
            Intent intent = new Intent();
            Bundle bundleImage = new Bundle();
            bundleImage.putByteArray( "dataImage", bitmap2Bytes );
            bundleImage.putString( "image", "image" );
            intent.putExtras( bundleImage );
            setResult( 100, intent );
        }
        Log.i( TAG, "数据传递结束" );

    }

    private void savePic( Bitmap photo ) {
        File file = new File( IstroopConstants.PICTURE_PATH,
                              "ichaotu_upload.jpg" );
        FileOutputStream fos;
        try {
            Bitmap bitmap = scaleDownBitmap( photo, 250, ActionSheet.this );
            fos = new FileOutputStream( file );
            bitmap.compress( Bitmap.CompressFormat.JPEG, 100, fos );
            fos.flush();
            fos.close();
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
            Log.i( TAG, "file not found" );
        } catch ( IOException e ) {
            e.printStackTrace();
            Log.i( TAG, "io_design error" );
        }
    }

    /**
     * 对位图进行缩放处理
     *
     * @param photo 原图片
     * @param newHeight 新高度
     * @param context 环境变量
     * @return 新图片
     */
    public static Bitmap scaleDownBitmap( Bitmap photo, int newHeight,
                                          Context context ) {
        final float densityMultiplier = context.getResources()
                .getDisplayMetrics().density;
        int h = ( int ) ( newHeight * densityMultiplier );
        int w = ( int ) ( h * photo.getWidth() / ( ( double ) photo.getHeight() ) );
        photo = Bitmap.createScaledBitmap( photo, w, h, true );
        return photo;
    }

    public static byte[] Bitmap2Bytes( Bitmap bm ) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress( Bitmap.CompressFormat.PNG, 100, baos );
        return baos.toByteArray();
    }

    public static String getImageUrl( Context context, Uri uri ) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query( uri, null, null, null, null );
        if ( cursor != null ) {
            int index = cursor.getColumnIndex( "_data" );
            cursor.moveToFirst();
            String url = cursor.getString( index );
            cursor.close();
            return url;
        }
        return null;
    }
}
