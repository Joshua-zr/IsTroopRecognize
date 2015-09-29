package com.istroop.istrooprecognize;

import android.app.Activity;
import android.app.Fragment;
import android.os.Handler;
import android.os.Looper;

public class WMDetectorThread extends Thread {
    private Handler mHandler     = null;
    private Handler mMainHandler = null;
    private Activity fragment;

    public WMDetectorThread( String name, Handler main_handler, Activity fragment ) {
        super( name );
        mMainHandler = main_handler;
        this.fragment = fragment;
    }

    public Handler getHandler() {
        return mHandler;
    }

    //TODO 临时方法
    public Activity getActivity() {
        return fragment;
    }

    @Override
    public void run() {
        Looper.prepare();
        mHandler = new WMDHandler( fragment, mMainHandler );
        Looper.loop();
    }

}
