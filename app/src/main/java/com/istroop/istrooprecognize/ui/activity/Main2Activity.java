package com.istroop.istrooprecognize.ui.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.istroop.istrooprecognize.IstroopConstants;
import com.istroop.istrooprecognize.MyApplication;
import com.istroop.istrooprecognize.R;
import com.istroop.istrooprecognize.ui.fragment.HisFragment;
import com.istroop.istrooprecognize.ui.fragment.RecoFragment;
import com.istroop.istrooprecognize.utils.ViewServer;

import butterknife.Bind;
import butterknife.ButterKnife;

public class Main2Activity extends Activity {

    private static final int MSG_MAIN_DELAY = 0;
    @Bind( R.id.fl_main_frame )
    FrameLayout flMainFrame;
    @Bind( R.id.iv_reco )
    ImageView   ivReco;
    @Bind( R.id.iv_his )
    ImageView   ivHis;

    private RecoFragment recoFragment;
    private HisFragment  hisFragment;
    private Handler      mHandler;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main2 );
        ButterKnife.bind( this );
        mHandler = new MainHandler();
        recoFragment = new RecoFragment();
        hisFragment = new HisFragment();
        ViewServer.get( this ).addWindow( this );   //可以真机查看布局渲染

        setRecoFragment();

        ivReco.setOnClickListener( v -> {
            if ( !recoFragment.isVisible() )
                setRecoFragment();
        } );
        ivHis.setOnClickListener( v -> {
            if ( !hisFragment.isVisible() )
                setHisFragment();
        } );
    }

    private void setRecoFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace( R.id.fl_main_frame, recoFragment );
        fragmentTransaction.commit();
    }

    private void setHisFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace( R.id.fl_main_frame, hisFragment );
        fragmentTransaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ViewServer.get( this ).setFocusedWindow( this );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ViewServer.get( this ).removeWindow( this );
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        exit();
    }

    static class MainHandler extends Handler {
        @Override
        public void handleMessage( Message msg ) {
            super.handleMessage( msg );
            if ( msg.what == MSG_MAIN_DELAY )
                IstroopConstants.isExit = false;
        }
    }

    private void exit() {
        if ( !IstroopConstants.isExit ) {
            IstroopConstants.isExit = true;
            Toast.makeText( this, "再按一次退出程序", Toast.LENGTH_SHORT ).show();
            // 利用handler延迟发送更改状态信息
            mHandler.sendEmptyMessageDelayed( MSG_MAIN_DELAY, 2000 );
        } else {
//            if ( IstroopConstants.mCamera != null ) {
//                IstroopConstants.mCamera.stopPreview();
//                IstroopConstants.mCamera.release();
//                IstroopConstants.mCamera = null;
//            }
            MyApplication.getInstance().exit();
        }
    }
}
