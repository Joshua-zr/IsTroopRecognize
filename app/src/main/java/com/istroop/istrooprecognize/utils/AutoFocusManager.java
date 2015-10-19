package com.istroop.istrooprecognize.utils;

import android.content.Context;
import android.hardware.Camera;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by joshua-zr on 8/24/15.
 */
public class AutoFocusManager implements Camera.AutoFocusCallback {

    private static final String TAG = AutoFocusManager.class.getSimpleName();

    private static final long AUTO_FOCUS_INTERVAL_MS = 2000L;
    private static Collection<String> FOCUS_MODES_CALLING_AF;

    private AsyncTask outstandingTask;

    static {
        FOCUS_MODES_CALLING_AF = new ArrayList<>( 2 );
        FOCUS_MODES_CALLING_AF.add( Camera.Parameters.FOCUS_MODE_AUTO );
        FOCUS_MODES_CALLING_AF.add( Camera.Parameters.FOCUS_MODE_MACRO );
    }

    private boolean stoped;
    private boolean focusing;
    private Camera  mCamera;
    private boolean useAutoFocus;

    AutoFocusManager( Context context, Camera camera ) {
        mCamera = camera;
        /*SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( context );*/
        String currentFocusMode = camera.getParameters().getFocusMode();
        useAutoFocus = FOCUS_MODES_CALLING_AF.contains( currentFocusMode );
        Utils.log( TAG, "!!" + useAutoFocus + "!!", 5 );
    }

    @Override
    public void onAutoFocus( boolean success, Camera camera ) {
        focusing = false;
        autoFocusAgainLater();
    }

    private synchronized void autoFocusAgainLater() {
        if ( !stoped && outstandingTask == null ) {
            AutoFocusTask autoFocusTask = new AutoFocusTask();
            try {
                autoFocusTask.executeOnExecutor( AsyncTask.THREAD_POOL_EXECUTOR );
                outstandingTask = autoFocusTask;
            } catch ( RejectedExecutionException ree ) {
                Utils.log( TAG, "Could not request auto foucs", 5, ree );
            }
        }
    }

    void start() {
        if ( useAutoFocus ) {
            outstandingTask = null;
            if ( !stoped && !focusing ) {
                try {
                    mCamera.autoFocus( this );
                    focusing = true;
                } catch ( RuntimeException re ) {
                    Utils.log( TAG, "Unexcepted exception while focusing", 5, re );
                    autoFocusAgainLater();
                }
            }
        }
    }

    private synchronized void cancelOutstandingTask() {
        if ( outstandingTask != null ) {
            if ( outstandingTask.getStatus() != AsyncTask.Status.FINISHED ) {
                outstandingTask.cancel( true );
            }
            outstandingTask = null;
        }
    }

    synchronized void stop() {
        stoped = true;
        if ( useAutoFocus ) {
            cancelOutstandingTask();
            try {
                mCamera.cancelAutoFocus();
            } catch ( RuntimeException re ) {
                Utils.log( TAG, "Unexception exception while cancelling focusing", 5, re );
            }
        }
    }

    private final class AutoFocusTask extends AsyncTask<Object, Objects, Object> {

        @Override
        protected Object doInBackground( Object... params ) {
            try {
                Thread.sleep( AUTO_FOCUS_INTERVAL_MS );
            } catch ( InterruptedException e ) {
                Thread.interrupted();
            }
            start();
            return null;
        }
    }

}
