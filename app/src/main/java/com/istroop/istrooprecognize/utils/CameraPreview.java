package com.istroop.istrooprecognize.utils;

import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Handler;
import android.os.Message;

import com.istroop.istrooprecognize.IstroopConstants;
import com.istroop.istrooprecognize.WMDetectorThread;
import com.istroop.openapi.Constant;
import com.istroop.openapi.Coordinate;

import java.io.IOException;

public class CameraPreview implements PreviewCallback {
    private static final String TAG = "Camer Preview";
    private WMDetectorThread mDetectorThd;
    private Handler          previewHandler;
    private int              previewMessage;
    private int              mPreviewWidth;
    private int              mPreviewHeight;

    public CameraPreview( int previewWidth, int previewHeight, WMDetectorThread detectorThread ) {
        mDetectorThd = detectorThread;
        mPreviewWidth = previewWidth;
        mPreviewHeight = previewHeight;
        new Thread( new Runnable() {
            @Override
            public void run() {
                if ( Constant.coordinate == null ) {
                    Constant.coordinate = new Coordinate( 0.0, 0.0 );
                }
                //TODO 需要增加网络判读功能
                String url = Constant.URL_PATH + "stat.gif?plat=android&type=start&gps=" + Constant.coordinate.latitude + "," + Constant.coordinate.longitude + "&device_id=" + Constant.imei + "&device_type=" + Constant.model + "&appkey=" + Constant.appKey;
                try {
                    Okhttps instance = Okhttps.getInstance();
                    String result = instance.get( url );
                    if ( result != null ) {
                        Utils.log( TAG, result, 5 );
                    }
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            }
        } ).start();
    }

/*    private DrawingView drawingView;

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

            final Rect targetFocusRect = new Rect(
                    touchRect.left * 2000 / this.getWidth() - 1000,
                    touchRect.top * 2000 / this.getHeight() - 1000,
                    touchRect.right * 2000 / this.getWidth() - 1000,
                    touchRect.bottom * 2000 / this.getHeight() - 1000 );

            doTouchFocus( targetFocusRect );

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
    }*/

/*    */

    /**
     * set DrawingView instance for touch focus indication.
     *//*
    public void setDrawingView( DrawingView dView ) {
        drawingView = dView;
        drawingViewSet = true;
    }*/

/*    public void doTouchFocus( final Rect tfocusRect ) {
        try {
            List<Camera.Area> focusList = new ArrayList<>();
            Camera.Area focusArea = new Camera.Area( tfocusRect, 1000 );
            focusList.add( focusArea );

            Camera.Parameters param = mCamera.getParameters();
            param.setFocusAreas( focusList );
            param.setMeteringAreas( focusList );
            mCamera.setParameters( param );

            mCamera.autoFocus( myAutoFocusCallback );
        } catch ( Exception e ) {
            e.printStackTrace();
            Log.i( TAG, "Unable to autofocus" );
        }
    }*/

    void setHandler( Handler previewHandler, int previewMessage ) {
        this.previewHandler = previewHandler;
        this.previewMessage = previewMessage;
    }

    @Override
    public void onPreviewFrame( byte[] data, Camera camera ) {

        Handler handler = mDetectorThd.getHandler();
        if ( handler != null ) {
            Message msg = Message.obtain( handler,
                                          IstroopConstants.IAMessages_MAIN_WM_DETECT_REQ,
                                          mPreviewWidth, mPreviewHeight, data );
            Utils.log( TAG, "data.length:" + data.length + "   width:" + mPreviewWidth + "   height:" + mPreviewHeight, 5 );
            handler.sendMessage( msg );
        }
    }
}
