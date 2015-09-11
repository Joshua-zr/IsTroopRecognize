package com.istroop.istrooprecognize.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.istroop.istrooprecognize.BaseActivity;
import com.istroop.istrooprecognize.IstroopConstants;
import com.istroop.istrooprecognize.R;
import com.istroop.istrooprecognize.utils.HackyViewPager;
import com.istroop.istrooprecognize.utils.HttpTools;
import com.istroop.istrooprecognize.utils.ImageAsyncTask;
import com.istroop.istrooprecognize.utils.PhotoView;
import com.istroop.istrooprecognize.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImagePagerActivity extends BaseActivity implements OnClickListener {

    private static final   String TAG                 = ImagePagerActivity.class.getSimpleName();
    protected static final int    IMAGE_PAGER_SUCCESS = 1;
    protected static final int    IMAGE_PAGER_FAIL    = 2;
    private LinearLayout image_pager_ll;
    HackyViewPager pager;
    private ImagePagerHandler handler = new ImagePagerHandler();
    private String[] imageUrls;

    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.image_pager );
        String picUrl = getIntent().getExtras().getString( "picUrl" );
        String number = getIntent().getExtras().getString( "number" );
        Utils.log( TAG, "picUrl:" + picUrl + "   number:" + number, 5 );
        String[] split = new String[0];
        if ( picUrl != null ) {
            split = picUrl.split( ".jpg" );
        }
        picUrl = split[0] + "_wm.jpg";
        imageUrls = new String[] { "http://tstatics.tujoin.com/print.php?url=" + picUrl };

        TextView image_pager_return_tv = ( TextView ) findViewById( R.id.image_pager_return_tv );
        ImageView image_pager_more = ( ImageView ) findViewById( R.id.image_pager_more );
        image_pager_ll = ( LinearLayout ) findViewById( R.id.image_pager_ll );
        Button image_pager_save = ( Button ) findViewById( R.id.image_pager_save );
        Button image_pager_cancel = ( Button ) findViewById( R.id.image_pager_cancel );
        image_pager_return_tv.setText( getResources().getString( R.string.his_title ) + "(" + number + ")" );
        //TODO  历史记录的个数
        image_pager_cancel.setOnClickListener( this );
        image_pager_save.setOnClickListener( this );
        image_pager_more.setOnClickListener( this );
        image_pager_return_tv.setOnClickListener( this );
        pager = ( HackyViewPager ) findViewById( R.id.pager );
        pager.setAdapter( new ImagePagerAdapter( imageUrls ) );
        pager.setCurrentItem( 0 );
    }

    private class ImagePagerAdapter extends PagerAdapter {

        private static final String TAG = "ImagePagerAdapter";
        private String[]       images;
        private LayoutInflater inflater;

        ImagePagerAdapter( String[] images ) {
            this.images = images;
            inflater = getLayoutInflater();
        }

        @Override
        public void destroyItem( ViewGroup container, int position, Object object ) {
            container.removeView( ( View ) object );
        }

        @Override
        public void finishUpdate( View container ) {
        }

        @Override
        public int getCount() {
            return images.length;
        }

        @Override
        public Object instantiateItem( ViewGroup view, int position ) {
            View imageLayout = inflater.inflate( R.layout.item_pager_image, view, false );

            PhotoView imageView = ( PhotoView ) imageLayout.findViewById( R.id.image );
            final ProgressBar spinner = ( ProgressBar ) imageLayout.findViewById( R.id.loading );

            ImageAsyncTask task = new ImageAsyncTask( ImagePagerActivity.this, imageView, IstroopConstants.mLruCache, spinner );
            Bitmap bitmap = task.getBitmapFromMemoryCache( images[position] );
            Bitmap cache = task.getBitmapFileCache( images[position] );
            Log.i( TAG, "url:" + images[position] );
            Log.i( TAG, bitmap + "" );
            if ( bitmap != null ) {
                spinner.setVisibility( View.INVISIBLE );
                imageView.setImageBitmap( bitmap );
            } else if ( cache != null ) {
                spinner.setVisibility( View.INVISIBLE );
                imageView.setImageBitmap( cache );
            } else {
                imageView.setImageResource( R.drawable.pic_big );
                task.execute( images[position] );
            }
            view.addView( imageLayout, 0 );
            return imageLayout;
        }

        @Override
        public boolean isViewFromObject( View view, Object object ) {
            return view.equals( object );
        }

    }

    @Override
    public void onClick( View v ) {
        switch ( v.getId() ) {
            case R.id.image_pager_return_tv:
                finish();
                break;
            case R.id.image_pager_more:
                image_pager_ll.setVisibility( View.VISIBLE );
                break;
            case R.id.image_pager_cancel:
                image_pager_ll.setVisibility( View.INVISIBLE );
                break;
            case R.id.image_pager_save:
                createPic();
                image_pager_ll.setVisibility( View.INVISIBLE );
                break;
            default:
                break;
        }
    }


    /**
     * create picture with tag
     */
    protected void createPic() {
        new Thread() {
            public void run() {
                Bitmap imageload = HttpTools.imageload( ImagePagerActivity.this, imageUrls[0] );
                if ( imageload != null ) {
                    String path = Environment.getExternalStorageDirectory().getPath() + "/istroop_image";
                    File filePath = new File( path );
                    if ( !filePath.exists() ) {
                        filePath.mkdir();
                    }

                    File file = new File( path, getPhotoFileName() );
                    FileOutputStream fos;
                    try {
                        file.createNewFile();
                        fos = new FileOutputStream( file );
                        imageload.compress( Bitmap.CompressFormat.JPEG, 100, fos );
                        sdScan();
                        fos.flush();
                        fos.close();
                        Message message = Message.obtain();
                        message.what = IMAGE_PAGER_SUCCESS;
                        handler.sendMessage( message );
                        return;
                    } catch ( IOException e ) {
                        e.printStackTrace();
                    }
                }
                Message message = Message.obtain();
                message.what = IMAGE_PAGER_FAIL;
                handler.sendMessage( message );
            }
        }.start();
    }

    public String getPhotoFileName() {
        Date date = new Date( System.currentTimeMillis() );
        SimpleDateFormat dateFormat = new SimpleDateFormat( "'ichaotu'_yyyyMMdd_HHmmss", Locale.CHINA );
        return dateFormat.format( date ) + ".jpg";
    }

    public void sdScan() {
        sendBroadcast( new Intent( Intent.ACTION_MEDIA_MOUNTED, Uri.parse( "file://"
                                                                                   + Environment.getExternalStorageDirectory() ) ) );
    }

    class ImagePagerHandler extends Handler {
        @Override
        public void handleMessage( Message msg ) {
            super.handleMessage( msg );
            switch ( msg.what ) {
                case IMAGE_PAGER_SUCCESS:
                    Toast.makeText( ImagePagerActivity.this, "保存成功", Toast.LENGTH_SHORT ).show();
                    break;
                case IMAGE_PAGER_FAIL:
                    Toast.makeText( ImagePagerActivity.this, "保存失败", Toast.LENGTH_SHORT ).show();
                    break;
                default:
                    break;
            }
        }
    }
}
