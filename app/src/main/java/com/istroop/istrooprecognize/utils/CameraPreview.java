package com.istroop.istrooprecognize.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.ImageView;

import com.facebook.stetho.common.Util;
import com.istroop.istrooprecognize.IstroopConstants;
import com.istroop.istrooprecognize.WMDetectorThread;
import com.istroop.istrooprecognize.ui.activity.RecoActivity;
import com.istroop.openapi.Constant;
import com.istroop.openapi.Coordinate;
import com.istroop.watermark.AndroidWMDetector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Random;

public class CameraPreview implements PreviewCallback {
    private static final String TAG = "Camer Preview";
    private WMDetectorThread mDetectorThd;
    private int              mPreviewWidth;
    private int              mPreviewHeight;
    private Context          mContext;

    public CameraPreview( int previewWidth, int previewHeight, WMDetectorThread detectorThread, Context context ) {

        mDetectorThd = detectorThread;
        mPreviewWidth = previewWidth;
        mPreviewHeight = previewHeight;
        mContext = context;
        new Thread( new Runnable() {
            @Override
            public void run() {
                if ( Constant.coordinate == null ) {
                    Constant.coordinate = new Coordinate( 0.0, 0.0 );
                }
                Utils.currentTime();
                //TODO 需要增加网络判读功能
                String url = Constant.URL_PATH + "stat.gif?plat=android&type=start&gps=" +
                        Constant.coordinate.latitude + "," + Constant.coordinate.longitude +
                        "&device_id=" + Constant.imei + "&device_type=" + Constant.model +
                        "&appkey=" + Constant.appKey + "&time_created=" + Utils.currentTime();
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
    @Override
    public void onPreviewFrame( byte[] data, Camera camera ) {

        Handler handler = mDetectorThd.getHandler();
        if ( handler != null ) {
//            Utils.log( TAG, "width : " + mPreviewWidth + "   height : " + mPreviewHeight, 6 );
//            final int[] rgb = decodeYUV420SP( data, mPreviewWidth, mPreviewHeight );
//            int[] matrix = getMatrix( rgb );
//            Bitmap bmp = Bitmap.createBitmap( matrix, zoomWidth, zoomHeight, Bitmap.Config.ARGB_8888 );
//            ( ( RecoActivity ) mContext ).recognize( bmp );

//            String root = Environment.getExternalStorageDirectory().toString();
//            File myDir = new File( root + "/Pictures/save_picture" );
//            myDir.mkdirs();
//            Random generator = new Random();
//            int n = 10000;
//            n = generator.nextInt( n );
//            String fname = "Image-" + n + ".jpg";
//            File file = new File( myDir, fname );
//            if ( file.exists() ) file.delete();
//            try {
//                FileOutputStream out = new FileOutputStream( file );
//                bmp.compress( Bitmap.CompressFormat.JPEG, 90, out );
//                out.flush();
//                out.close();
//            } catch ( Exception e ) {
//                e.printStackTrace();
//            }
            Message msg = Message.obtain( handler,
                                          IstroopConstants.IAMessages_MAIN_WM_DETECT_REQ,
                                          mPreviewWidth, mPreviewHeight, data );
            handler.sendMessage( msg );
        }
    }

    public int[] decodeYUV420SP( byte[] yuv420sp, int width, int height ) {

//        final int frameSize = width * height;
//        int rgb[] = new int[width * height];
//        for ( int j = 0, yp = 0; j < height; j++ ) {
//            int uvp = frameSize + ( j >> 1 ) * width, u = 0, v = 0;
//            for ( int i = 0; i < width; i++, yp++ ) {
//                int y = ( 0xff & ( ( int ) yuv420sp[yp] ) ) - 16;
//                if ( y < 0 ) y = 0;
//                if ( ( i & 1 ) == 0 ) {
//                    v = ( 0xff & yuv420sp[uvp++] ) - 128;
//                    u = ( 0xff & yuv420sp[uvp++] ) - 128;
//                }
//
//                int y1192 = 1192 * y;
//                int r = ( y1192 + 1634 * v );
//                int g = ( y1192 - 833 * v - 400 * u );
//                int b = ( y1192 + 2066 * u );
//
//                if ( r < 0 ) r = 0;
//                else if ( r > 262143 ) r = 262143;
//                if ( g < 0 ) g = 0;
//                else if ( g > 262143 ) g = 262143;
//                if ( b < 0 ) b = 0;
//                else if ( b > 262143 ) b = 262143;
//
//                rgb[yp] = 0xff000000 | ( ( r << 6 ) & 0xff0000 ) | ( ( g >> 2 ) &
//                        0xff00 ) | ( ( b >> 10 ) & 0xff );
//            }
//        }
//        return rgb;

        final int frameSize = width * height;
        int[] argb = new int[frameSize];
        final int ii = 0;
        final int ij = 0;
        final int di = +1;
        final int dj = +1;

        int a = 0;
        for ( int i = 0, ci = ii; i < height; ++i, ci += di ) {
            for ( int j = 0, cj = ij; j < width; ++j, cj += dj ) {
                int y = ( 0xff & ( ( int ) yuv420sp[ci * width + cj] ) );
                int v = ( 0xff & ( ( int ) yuv420sp[( frameSize + ( ci >> 1 ) * width + ( cj & ~1 ) )] ) );
                int u = ( 0xff & ( ( int ) yuv420sp[frameSize + ( ci >> 1 ) * width + ( cj & ~1 ) + 1] ) );
                y = y < 16 ? 16 : y;

                int r = ( int ) ( 1.164f * ( y - 16 ) + 1.596f * ( v - 128 ) );
                int g = ( int ) ( 1.164f * ( y - 16 ) - 0.813f * ( v - 128 ) - 0.391f * ( u - 128 ) );
                int b = ( int ) ( 1.164f * ( y - 16 ) + 2.018f * ( u - 128 ) );

                r = r < 0 ? 0 : ( r > 255 ? 255 : r );
                g = g < 0 ? 0 : ( g > 255 ? 255 : g );
                b = b < 0 ? 0 : ( b > 255 ? 255 : b );

                argb[a++] = 0xff000000 | ( r << 16 ) | ( g << 8 ) | b;
            }
        }
        return argb;
    }

//    private int zoomWidth;
//    private int zoomHeight;
//
//    public int[] getMatrix( int[] rgb ) {
//
//        zoomWidth = Utils.dip2px( mDetectorThd.getActivity(), 100 );
//        zoomHeight = mPreviewHeight - Utils.dip2px( mDetectorThd.getActivity(), 50 );
//
//        int area = zoomWidth * zoomHeight;
//        int[] matrix = new int[area];
//        int inputOffset = Utils.dip2px( mDetectorThd.getActivity(), 25 ) * mPreviewWidth
//                + Utils.dip2px( mDetectorThd.getActivity(), 120 );
//        Utils.log( TAG, "rgb.length : " + rgb.length + "   inputOffset : " + inputOffset +
//                "   matrix.length : " + matrix.length, 5 );
//        // Otherwise copy one cropped row at a time.
//        for ( int y = 0; y < zoomHeight; y++ ) {
//            int outputOffset = y * zoomWidth;
//            System.arraycopy( rgb, inputOffset, matrix, outputOffset, zoomWidth );
//            inputOffset += mPreviewWidth;
//        }
//        return matrix;
//    }
}
