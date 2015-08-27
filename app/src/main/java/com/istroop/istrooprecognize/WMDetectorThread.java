package com.istroop.istrooprecognize;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.istroop.istrooprecognize.ui.activity.RecoActivity;
import com.istroop.istrooprecognize.utils.Utils;
import com.istroop.watermark.AndroidWMDetector;

public class WMDetectorThread extends Thread {
    private Handler mHandler     = null;
    private Handler mMainHandler = null;
    private Activity mActivity;
    private static final String TAG = WMDetectorThread.class.getSimpleName();

    public WMDetectorThread( String name, Handler main_handler, Activity activity ) {
        super( name );
        mMainHandler = main_handler;
        mActivity = activity;
    }

    public Handler getHandler() {
        return this.mHandler;
    }

    @Override
    public void run() {
        Looper.prepare();
        mHandler = new WMDHandler();
        Looper.loop();
    }

    class WMDHandler extends Handler {
        @Override
        public void handleMessage( Message msg ) {
            switch ( msg.what ) {
                case IstroopConstants.IAMessages_MAIN_WM_DETECT_REQ:
                    byte[] data = ( byte[] ) msg.obj;
                    int width = msg.arg1;
                    int height = msg.arg2;
                    int wm_id = AndroidWMDetector.detect( data, width, height );
                    Utils.log( TAG, "data.length:" + data.length + "   水印id:" + wm_id + "   width:" + width + "   height:" + height, 5 );
                    if ( wm_id < 0 ) {
                        Message response = Message.obtain( mMainHandler,
                                                           IstroopConstants.IAMessages_SUB_FLAG_NO_WATERMARK );
                        mMainHandler.sendMessage( response );
                    } else if ( mActivity instanceof RecoActivity ) {
                        ( ( RecoActivity ) mActivity ).loadPicInfo( wm_id );
                    }
                    break;
                case IstroopConstants.IAMessages_MAIN_QUIT:
                    Looper.myLooper().quit();
                    break;
                default:
            }
        }
    }
}
