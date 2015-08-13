package com.istroop.istrooprecognize.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.istroop.istrooprecognize.BaseActivity;
import com.istroop.istrooprecognize.IstroopConstants;
import com.istroop.istrooprecognize.R;
import com.istroop.istrooprecognize.utils.ImageAsyncTask;
import com.istroop.istrooprecognize.utils.PhotoView;

public class RecoDetailPicActivity extends BaseActivity {
    GestureDetector gestureDetector;

    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.detail_pic );
        String picUrl = getIntent().getExtras().getString( "DB_tag_url" );
        String titleStr = getIntent().getExtras().getString( "DB_tag_title" );
        PhotoView imageView = ( PhotoView ) findViewById( R.id.imageView );
        TextView leftBtn_pic = ( TextView ) findViewById( R.id.leftBtn_pic );
        leftBtn_pic.setOnClickListener( v -> finish() );
        final ProgressBar progressBar = ( ProgressBar ) findViewById( R.id.loadPro );
        final ImageView picBackView = ( ImageView ) findViewById( R.id.picBack );

        final TextView titleView = ( TextView ) findViewById( R.id.pic_title );
        titleView.setText( titleStr );

        gestureDetector = new GestureDetector( RecoDetailPicActivity.this,
                                               new SimpleOnGestureListener() {
                                                   @Override
                                                   public void onShowPress( MotionEvent e ) {
                                                       System.out.println( "=====GestureDetector-DPWN" );
                                                   }

                                                   @Override
                                                   public boolean onSingleTapUp( MotionEvent e ) {
                                                       System.out.println( "=====GestureDetector-TAP" );
                                                       return true;
                                                   }

                                                   @Override
                                                   public boolean onSingleTapConfirmed( MotionEvent e ) {
                                                       System.out.println( "=====GestureDetector-CON" );
                                                       if ( picBackView.isShown() ) {
                                                           picBackView.setVisibility( View.INVISIBLE );
                                                           titleView.setVisibility( View.INVISIBLE );
                                                       } else {
                                                           picBackView.setVisibility( View.VISIBLE );
                                                           titleView.setVisibility( View.VISIBLE );
                                                       }
                                                       return true;
                                                   }
                                               } );
        // 异步加载图片
        ImageAsyncTask task = new ImageAsyncTask( RecoDetailPicActivity.this,
                                                  imageView, IstroopConstants.mLruCache, progressBar );
        Bitmap bitmap = task.getBitmapFromMemoryCache( picUrl );
        if ( bitmap != null ) {
            progressBar.setVisibility( View.INVISIBLE );
            imageView.setImageBitmap( bitmap );
        } else {
            imageView.setImageResource( R.drawable.pic_big );
            task.execute( picUrl );
        }
    }

    @Override
    public boolean onTouchEvent( MotionEvent event ) {
        System.out.println( "=====Activity" );
        return gestureDetector.onTouchEvent( event );

    }
}