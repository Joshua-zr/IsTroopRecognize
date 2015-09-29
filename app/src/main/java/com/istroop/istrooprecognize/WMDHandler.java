package com.istroop.istrooprecognize;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.istroop.istrooprecognize.ui.activity.RecoActivity;
import com.istroop.istrooprecognize.utils.Utils;
import com.istroop.watermark.AndroidWMDetector;

/**
 * 识别摄像头捕捉到的数据
 * Created by joshua-zr on 8/28/15.
 */
public class WMDHandler extends Handler {

    private static final String TAG = WMDHandler.class.getSimpleName();

    private Handler mMainHandler = null;
    private Activity mFragment;
    private boolean running = true;

    WMDHandler( Activity fragment, Handler handler ) {
        mFragment = fragment;
        mMainHandler = handler;
    }

    @Override
    public void handleMessage( Message msg ) {
        if ( !running ) {
            return;
        }
        switch ( msg.what ) {
            case IstroopConstants.IAMessages_MAIN_WM_DETECT_REQ:
                byte[] data = ( byte[] ) msg.obj;
                int width = msg.arg1;
                int height = msg.arg2;
                int wm_id = AndroidWMDetector.detect( data, width, height );
//                int wm_id = -1;
                Utils.log( TAG, "data.length:" + data.length + "   水印id:" + wm_id + "   width:" + width + "   height:" + height, 5 );
                if ( wm_id < 0 ) {
                    Message response = Message.obtain( mMainHandler,
                                                       IstroopConstants.IAMessages_SUB_FLAG_NO_WATERMARK );
                    mMainHandler.sendMessage( response );
                } else if ( mFragment instanceof RecoActivity ) {
                    ( ( RecoActivity ) mFragment ).loadPicInfo( wm_id );
                }
                break;
            case IstroopConstants.IAMessages_MAIN_QUIT:
                running = false;
                Looper.myLooper().quit();
                break;
            default:
        }
    }
}
