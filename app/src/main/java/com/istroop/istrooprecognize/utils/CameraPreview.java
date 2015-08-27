package com.istroop.istrooprecognize.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.istroop.istrooprecognize.IstroopConstants;
import com.istroop.istrooprecognize.WMDetectorThread;
import com.istroop.istrooprecognize.ui.activity.RecoActivity;
import com.istroop.openapi.Constant;
import com.istroop.openapi.Coordinate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraPreview implements PreviewCallback {
    private static final String TAG = "Camer Preview";
    private final WMDetectorThread           mDetectorThd;
    private       Camera                     mCamera;
    private       CameraConfigurationManager configManager;
    private       Handler                    previewHandler;
    private       int                        previewMessage;
    private       int                        mPreviewWidth;
    private       int                        mPreviewHeight;

    public CameraPreview( Activity activity, CameraConfigurationManager configManager,
                          Handler handler, int previewWidth, int previewHeight ) {
        this.configManager = configManager;
        mPreviewWidth = previewWidth;
        mPreviewHeight = previewHeight;
        new Thread( new Runnable() {
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
        } ).start();

        mDetectorThd = new WMDetectorThread( "wmdetector", handler, activity );
        mDetectorThd.start();
    }

    private DrawingView drawingView;

    private boolean drawingViewSet;
    // some code here

    public boolean onTouchEvent( @NonNull MotionEvent event ) {

        if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
            float x = event.getX();
            float y = event.getY();

            Rect touchRect = new Rect(
                    ( int ) ( x - 100 ),
                    ( int ) ( y - 100 ),
                    ( int ) ( x + 100 ),
                    ( int ) ( y + 100 ) );

            /*final Rect targetFocusRect = new Rect(
                    touchRect.left * 2000 / this.getWidth() - 1000,
                    touchRect.top * 2000 / this.getHeight() - 1000,
                    touchRect.right * 2000 / this.getWidth() - 1000,
                    touchRect.bottom * 2000 / this.getHeight() - 1000 );*/

            //doTouchFocus( targetFocusRect );

            if ( drawingViewSet ) {
                drawingView.setHaveTouch( true, touchRect );
                drawingView.invalidate();

                // Remove the square indicator after 1000 msec
                Handler handler = new Handler();
                handler.postDelayed( () -> {
                    drawingView.setHaveTouch( false, new Rect( 0, 0, 0, 0 ) );
                    drawingView.invalidate();
                }, 1000 );
            }
        }

        return false;
    }

    /**
     * set DrawingView instance for touch focus indication.
     */
    public void setDrawingView( DrawingView dView ) {
        drawingView = dView;
        drawingViewSet = true;
    }

    public void doTouchFocus( final Rect tfocusRect ) {
        try {
            List<Camera.Area> focusList = new ArrayList<>();
            Camera.Area focusArea = new Camera.Area( tfocusRect, 1000 );
            focusList.add( focusArea );

            Camera.Parameters param = mCamera.getParameters();
            param.setFocusAreas( focusList );
            param.setMeteringAreas( focusList );
            mCamera.setParameters( param );

//            mCamera.autoFocus( myAutoFocusCallback );
        } catch ( Exception e ) {
            e.printStackTrace();
            Log.i( TAG, "Unable to autofocus" );
        }
    }

    void setHandler( Handler previewHandler, int previewMessage ) {
        this.previewHandler = previewHandler;
        this.previewMessage = previewMessage;
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

    }

    @Override
    public void onPreviewFrame( byte[] data, Camera camera ) {
/*        Point cameraResolution = configManager.getCameraResolution();
        Handler thePreviewHandler = previewHandler;
        if ( cameraResolution != null && thePreviewHandler != null ) {
            Message message = thePreviewHandler.obtainMessage( previewMessage, cameraResolution.x,
                                                               cameraResolution.y, data );
            message.sendToTarget();
            previewHandler = null;
        } else {
            Log.d( TAG, "Got preview callback, but no handler or resolution available" );
        }*/
        Handler handler = mDetectorThd.getHandler();
        if ( handler != null ) {
            Message msg = Message.obtain( handler,
                                          IstroopConstants.IAMessages_MAIN_WM_DETECT_REQ,
                                          mPreviewWidth, mPreviewHeight, data );
            handler.sendMessage( msg );
        }
    }
}
