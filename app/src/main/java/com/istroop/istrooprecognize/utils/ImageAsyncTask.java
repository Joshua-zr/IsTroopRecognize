package com.istroop.istrooprecognize.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.istroop.istrooprecognize.IstroopConstants;
import com.istroop.istrooprecognize.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageAsyncTask extends AsyncTask<String, ProgressBar, Bitmap> {

    private static final String TAG = "ImageAsyncTask";
    private ImageView                imageView;//加载图片的控件
    private LruCache<String, Bitmap> mLruCache;
    private ProgressBar              bar;
    //private Bitmap bitmap_fail;
    private Bitmap                   bitmap_init;
    private String                   map;
    private ImageView                model;
    private ImageView                mode;
    private View                     view;


    public ImageAsyncTask( Context context, View view, LruCache<String, Bitmap> mLruCache ) {
        this.mLruCache = mLruCache;
        bitmap_init = BitmapFactory.decodeResource( context.getResources(), R.drawable.pic_big );
        map = "ll";
        this.view = view;
    }

    public ImageAsyncTask( Context context, LruCache<String, Bitmap> mLruCache ) {
        this.mLruCache = mLruCache;
        bitmap_init = BitmapFactory.decodeResource( context.getResources(), R.drawable.pic_big );
        map = "map";
    }

    public ImageAsyncTask( Context context, ImageView imageView, LruCache<String, Bitmap> mLruCache, ProgressBar bar ) {
        this.imageView = imageView;
        this.mLruCache = mLruCache;
        this.bar = bar;
        bitmap_init = BitmapFactory.decodeResource( context.getResources(), R.drawable.pic_big );
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if ( TextUtils.isEmpty( map ) ) {
            bar.setVisibility( View.VISIBLE );
        }
    }

    @Override
    protected Bitmap doInBackground( String... params ) {
        String url = params[0];
        Log.i( TAG, "图片的url:" + url );
        Bitmap bitmap;
        InputStream inputStream;
        try {
            URL url_image = new URL( url );
            HttpURLConnection conn = ( HttpURLConnection ) url_image.openConnection();
            conn.setReadTimeout( 10000 );
            conn.setConnectTimeout( 10000 );
            conn.setRequestMethod( "GET" );
            conn.setDoInput( true );
            conn.setRequestProperty( "accept", "*/*" );
            conn.connect();
            int code = conn.getResponseCode();
            if ( 200 == code ) {
                inputStream = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream( inputStream );
            } else {
                Log.i( TAG, "网络返回异常,返回图片不正确" );
                return bitmap_init;
            }
            inputStream.close();
        } catch ( MalformedURLException e ) {
            e.printStackTrace();
            Log.i( TAG, "url格式不正确" + e.toString() );
            return bitmap_init;
        } catch ( IOException e ) {
            e.printStackTrace();
            Log.i( TAG, "输入输出异常" + e.toString() );
            return bitmap_init;
        }
        if ( bitmap != null ) {
            addBitmapToMemoryCache( url, bitmap );
            addBitmapFileCache( url, bitmap );
            return bitmap;
        } else {
            return bitmap_init;
        }
    }

    @SuppressWarnings( "deprecation" )
    @Override
    protected void onPostExecute( Bitmap result ) {
        super.onPostExecute( result );
        if ( TextUtils.isEmpty( map ) ) {
            bar.setVisibility( View.INVISIBLE );
            imageView.setImageBitmap( result );
        } else if ( "mode".equals( map ) ) {
            mode.setImageBitmap( result );
            model.setImageResource( R.drawable.icard_two_tag );
        } else if ( "ll".equals( map ) ) {
            BitmapDrawable drawable = new BitmapDrawable( result );
            view.setBackgroundDrawable( drawable );
        }
    }

    //调用LruCache的put 方法将图片加入内存缓存中，要给这个图片一个key 方便下次从缓存中取出来
    private void addBitmapToMemoryCache( String key, Bitmap bitmap ) {
        if ( getBitmapFromMemoryCache( key ) == null ) {
            mLruCache.put( key, bitmap );
        }
    }

    //调用Lrucache的get 方法从内存缓存中去图片
    public Bitmap getBitmapFromMemoryCache( String key ) {
        return mLruCache.get( key );
    }

    public void addBitmapFileCache( String url, Bitmap bitmap ) {

        if ( getBitmapFileCache( url ) == null ) {
            File filePath = new File( IstroopConstants.PICTURE_PATH );
            if ( !filePath.exists() ) {
                filePath.mkdir();
                Log.i( TAG, "执行路径创建操作" );
            }
            Log.i( TAG, "创建路径成功" );
            File file = new File( IstroopConstants.PICTURE_PATH, MD5Util.md5( url ) + ".jpg" );
            if ( file.exists() ) {
                file.delete();
            }
            try {
                //file.createNewFile();
                FileOutputStream fos = new FileOutputStream( file );
                bitmap.compress( Bitmap.CompressFormat.JPEG, 100, fos );
                fos.flush();
                fos.close();
            } catch ( FileNotFoundException e ) {
                e.printStackTrace();
                Log.i( TAG, "文件没有找到异常" );
            } catch ( IOException e ) {
                e.printStackTrace();
                Log.i( TAG, "输入输出异常" );
            }
        }
    }

    public Bitmap getBitmapFileCache( String url ) {
        return BitmapFactory.decodeFile( IstroopConstants.PICTURE_PATH + "/" + MD5Util.md5( url ) + ".jpg" );
    }
}
