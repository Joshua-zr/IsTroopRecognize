package com.istroop.istrooprecognize.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.istroop.istrooprecognize.IstroopConstants;
import com.istroop.istrooprecognize.bean.User;
import com.istroop.istrooprecognize.ui.activity.MainActivity;
import com.istroop.istrooprecognize.utils.Okhttps;
import com.istroop.istrooprecognize.utils.Utils;
import com.lidroid.xutils.HttpUtils;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import rx.Observable;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final String TAG = WXEntryActivity.class.getSimpleName();

    private static final int MESSAGE_WEIXIN_INFO_IS_OK = 0;
    private static final int MESSAGE_WEIXIN_LOGIN      = 1;
    private static final int MESSAGE_LOGIN_IS_OK       = 2;
    private HttpUtils httpUtils;
    private WXHandler handler = new WXHandler();
    private Okhttps okhttps;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        okhttps = Okhttps.getInstance();
        httpUtils = new HttpUtils();
        if ( IstroopConstants.admin == null ) {
            IstroopConstants.admin = new User();
        }
        if ( IstroopConstants.api == null ) {
            IstroopConstants.api = WXAPIFactory.createWXAPI( this, IstroopConstants.APP_ID, true );
            IstroopConstants.api.registerApp( IstroopConstants.APP_ID );
        }
        IstroopConstants.api.handleIntent( getIntent(), this );
    }

    @Override
    public void onReq( BaseReq baseReq ) {

    }

    @Override
    public void onResp( BaseResp baseResp ) {

        if ( baseResp.errCode == BaseResp.ErrCode.ERR_OK ) {
            if ( baseResp instanceof SendAuth.Resp ) {
                String code = ( ( SendAuth.Resp ) baseResp ).code;
                Utils.log( TAG, "code :" + code, 6 );
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            String result = okhttps.get( "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" +
                                                                 "wxb272fda9c4623c5f" + "&secret=" + "ed0406de923df94924bbde284fa7baf4" +
                                                                 "&code=" + code + "&grant_type=authorization_code" );
                            Utils.log( TAG, result, 6 );
                            JSONObject jsonObject = new JSONObject( result );
                            IstroopConstants.access_token = jsonObject
                                    .getString( "access_token" );
                            IstroopConstants.openid = jsonObject.getString( "openid" );
                            Message msg = new Message();
                            msg.what = MESSAGE_WEIXIN_INFO_IS_OK;
                            handler.sendMessage( msg );
                        } catch ( JSONException | IOException e ) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            } else {
                Toast.makeText( WXEntryActivity.this, "平台分享成功", Toast.LENGTH_SHORT ).show();
                finish();
            }
        } else {
            if ( baseResp instanceof SendAuth.Resp ) {
                Toast.makeText( WXEntryActivity.this, "登陆失败，请重新登陆", Toast.LENGTH_SHORT ).show();
            } else {
                Toast.makeText( WXEntryActivity.this, "平台分享失败", Toast.LENGTH_SHORT ).show();
            }
        }
    }

    class WXHandler extends Handler {
        @Override
        public void handleMessage( Message msg ) {
            super.handleMessage( msg );
            switch ( msg.what ) {
                case MESSAGE_WEIXIN_INFO_IS_OK:
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                String result = okhttps.get(
                                        "https://api.weixin.qq.com/sns/userinfo?access_token=" +
                                                IstroopConstants.access_token + "&openid=" +
                                                IstroopConstants.openid );
                                Utils.log( "weixin", result, 6 );
                                JSONObject jsonObject = new JSONObject( result );
                                IstroopConstants.admin.setOauthId(
                                        ( String ) jsonObject.get( "unionid" ) );
                                IstroopConstants.admin.setUsername(
                                        ( String ) jsonObject.get( "nickname" ) );
                                IstroopConstants.admin.setAvater(
                                        ( String ) jsonObject.get( "headimgurl" ) );
                                IstroopConstants.admin.setSex(
                                        jsonObject.get( "sex" ) + "" );

                                Message message = new Message();
                                message.what = MESSAGE_WEIXIN_LOGIN;
                                handler.sendMessage( message );
                            } catch ( IOException | JSONException e ) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                    break;
                case MESSAGE_WEIXIN_LOGIN:
                    /**
                     * 第三方登陆
                     * plat= qq, weixin, weibo &oauth[id]= &oauth[name]= &oauth[avater]= &oauth[gender]= &oauth[token]=
                     */
                    new Thread( () -> {
                        String url = "";
                        if ( IstroopConstants.admin.getSex().equals( "1" ) ) {
                            try {
                                url = URLEncoder.encode( "男", "UTF-8" );
                            } catch ( UnsupportedEncodingException e ) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            String login = okhttps.get( IstroopConstants.ICHAOTU_URL +
                                                                IstroopConstants.URL_OTHER_LOGIN + "/?plat=weixin" + "&oauth[id]=" +
                                                                IstroopConstants.admin
                                                                        .getOauthId() + "&oauth[name]=" + IstroopConstants.admin
                                    .getUsername() + "&oauth[avater]=" + IstroopConstants.admin
                                    .getAvater() + "&oauth[gender]=" + url + "&oauth[token]=" + IstroopConstants.access_token );
                            Utils.log( TAG, login, 6 );
//                      HttpTools
//                                .login( IstroopConstants.ICHAOTU_URL + IstroopConstants.URL_OTHER_LOGIN + "/?plat=weixin" + "&oauth[id]=" + IstroopConstants.admin
//                                                .getOauthId() + "&oauth[name]=" + IstroopConstants.admin
//                                                .getUsername() + "&oauth[avater]=" + IstroopConstants.admin
//                                                .getAvater() + "&oauth[gender]=" + url + "&oauth[token]=" + IstroopConstants.access_token,
//                                        IstroopConstants.cookiestore );
                            JSONObject jsonObject = new JSONObject(
                                    login );
                            if ( jsonObject.getBoolean( "success" ) ) {

                                JSONObject dataObject = ( JSONObject ) jsonObject
                                        .get( "data" );

                                JSONObject addressObject = ( JSONObject ) jsonObject
                                        .get( "address" );
                                if ( addressObject.get( "address" ) instanceof String ) {
                                    IstroopConstants.admin
                                            .setAddress( ( String ) addressObject.get( "address" ) );
                                } else {
                                    IstroopConstants.admin
                                            .setAddress( "" );
                                }
                                IstroopConstants.admin = new User();

                                IstroopConstants.admin.setToken(
                                        ( String ) dataObject.get( "token" ) );

                                JSONObject objectUserInfo = ( JSONObject ) dataObject
                                        .get( "userinfo" );
                                IstroopConstants.admin.setUserInfoUid(
                                        ( String ) objectUserInfo.get( "uid" ) );
                                IstroopConstants.admin.setSex( ( String ) objectUserInfo
                                        .get( "sex" ) );
                                IstroopConstants.admin
                                        .setUsername( ( String ) objectUserInfo
                                                .get( "uname" ) );
                                IstroopConstants.admin
                                        .setAvater( ( String ) objectUserInfo
                                                .get( "avater" ) );

                                IstroopConstants.admin.setUserInfoUid(
                                        ( String ) objectUserInfo.get( "uid" ) );

                                Message loginMsg = new Message();
                                loginMsg.what = MESSAGE_LOGIN_IS_OK;
                                handler.sendMessage( loginMsg );
                            }

                        } catch ( JSONException | IOException e ) {
                            e.printStackTrace();
                        }
                    } ).start();
                case MESSAGE_LOGIN_IS_OK:
                    IstroopConstants.isLogin = true;
                    Intent intent = new Intent( WXEntryActivity.this, MainActivity.class );
                    startActivity( intent );
                    finish();
                    break;
            }
        }
    }

    @Override
    protected void onNewIntent( Intent intent ) {
        super.onNewIntent( intent );
        IstroopConstants.api.handleIntent( intent, this );
    }
}