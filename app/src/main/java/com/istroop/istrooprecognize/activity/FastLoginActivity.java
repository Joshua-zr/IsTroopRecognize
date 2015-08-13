package com.istroop.istrooprecognize.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.istroop.istrooprecognize.BaseActivity;
import com.istroop.istrooprecognize.IstroopConstants;
import com.istroop.istrooprecognize.R;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class FastLoginActivity extends BaseActivity implements View.OnClickListener {
    private static final int ICARD_LOGIN_REGISTER_RESULT = 100;

    //微信注册
    private void regToWx() {
        IstroopConstants.api = WXAPIFactory.createWXAPI( this, IstroopConstants.APP_ID, true );
        IstroopConstants.api.registerApp( IstroopConstants.APP_ID );
    }

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_fast_login );
        ImageView iv_login_phone = ( ImageView ) findViewById( R.id.iv_login_phone );
        ImageView iv_login_wx = ( ImageView ) findViewById( R.id.iv_login_wx );
        iv_login_phone.setOnClickListener( this );
        iv_login_wx.setOnClickListener( this );
        Button bt_regist = ( Button ) findViewById( R.id.bt_regist );
        bt_regist.setOnClickListener( this );
        regToWx();  //微信注册
    }

    @Override
    public void onClick( View v ) {
        switch ( v.getId() ) {
            case R.id.iv_login_phone:
                Intent login = new Intent( this, ICardLoginActivtiy.class );
                startActivity( login );
                finish();
                break;
            //微信登录
            case R.id.iv_login_wx:
                SendAuth.Req req = new SendAuth.Req();
                req.scope = "snsapi_userinfo";
                req.state = "Istroop2014";
                IstroopConstants.api.sendReq( req );
                break;

            case R.id.bt_regist:
                Intent intent = new Intent( this,
                                            ICardRegisterActivity.class );
                startActivityForResult( intent, ICARD_LOGIN_REGISTER_RESULT );
                break;
        }
    }

}
