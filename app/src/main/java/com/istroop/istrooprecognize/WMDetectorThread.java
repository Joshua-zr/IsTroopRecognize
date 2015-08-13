package com.istroop.istrooprecognize;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.istroop.istrooprecognize.activity.RecoActivity;
import com.istroop.watermark.AndroidWMDetector;


public class WMDetectorThread extends Thread {
    private Handler mHandler = null;
    private Handler mMainHandler = null;
    private RecoActivity mActivity;

    public WMDetectorThread(String name, Handler main_handler, RecoActivity activity) {
        super(name);
        mMainHandler = main_handler;
        mActivity = activity;
    }

    public Handler getHandler() {
        return this.mHandler;
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void run() {
        Looper.prepare();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case IstroopConstants.IAMessages_MAIN_WM_DETECT_REQ:
                        byte[] data = (byte[]) msg.obj;
                        int width = msg.arg1;
                        int height = msg.arg2;
                        int wm_id = AndroidWMDetector.detect(data, width, height);
                        if (wm_id < 0) {
                            Message response = Message.obtain(mMainHandler,
                                    IstroopConstants.IAMessages_SUB_FLAG_NO_WATERMARK);
                            mMainHandler.sendMessage(response);
                        } else {
                            mActivity.loadPicInfo(wm_id);
                        }
                        break;
                    case IstroopConstants.IAMessages_MAIN_QUIT:
                        Looper.myLooper().quit();
                        break;
                    default:
                        ;
                }
            }
        };

        Looper.loop();
    }
}
