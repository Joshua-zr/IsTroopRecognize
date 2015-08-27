package com.istroop.istrooprecognize.utils;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview2 extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "Camer Preview";
    private SurfaceHolder mHolder;
    private Camera        mCamera;


    @SuppressWarnings( "deprecation" )
    public CameraPreview2( Context context, Camera camera ) {
        super( context );
        this.mCamera = camera;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback( this );
        mHolder.setKeepScreenOn( true );
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS );
/*        new Thread( new Runnable() {
            @Override
            public void run() {
                if ( Constant.coordinate == null ) {
                    Constant.coordinate = new Coordinate( 0.0, 0.0 );
                }
                //TODO 需要增加网络判读功能
                String start = Constant.URL_PATH + "stat.gif?plat=android&type=start&gps=" + Constant.coordinate.latitude + "," + Constant.coordinate.longitude + "&device_id=" + Constant.imei + "&device_type=" + Constant.model + "&appkey=" + Constant.appKey;
                try {
                    String s = HttpTools.toString( start );
                    if ( s != null ) {
                        Log.e( TAG, s );
                    }
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            }
        } ).start();*/

    }

    public void surfaceCreated( SurfaceHolder holder ) {
        try {
            mCamera.setPreviewDisplay( holder );
            mCamera.startPreview();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }


    public void surfaceDestroyed( SurfaceHolder holder ) {
        // empty. Take care of releasing the Camera preview in your activity.
        Log.i( "surfexp", "surfexp_surfaceDestroyed" );
        try {
            mCamera.setPreviewCallback( null );
            mCamera.stopPreview();

        } catch ( Exception e ) {
            // ignore: tried to stop a non-existent preview
        }
    }

    public void surfaceChanged( SurfaceHolder holder, int format, int w, int h ) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        //Log.i(TAG, "surface changed");
        Log.i( "surfexp", "surfexp_surfaceChanged" );
        if ( mHolder.getSurface() == null ) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {

            mCamera.stopPreview();
        } catch ( Exception e ) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {

            mCamera.setPreviewDisplay( mHolder );
//            mCamera.setOneShotPreviewCallback( ( PreviewCallback ) this.getContext() );
            mCamera.startPreview();

        } catch ( Exception e ) {
            Log.d( TAG, "Error starting camera preview: " + e.getMessage() );
        }
    }
}
