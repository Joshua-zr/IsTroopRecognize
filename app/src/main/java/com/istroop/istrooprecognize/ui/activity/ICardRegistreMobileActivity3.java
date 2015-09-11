package com.istroop.istrooprecognize.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.istroop.istrooprecognize.BaseActivity;
import com.istroop.istrooprecognize.IstroopConstants;
import com.istroop.istrooprecognize.R;
import com.istroop.istrooprecognize.utils.HttpTools;
import com.istroop.istrooprecognize.utils.Utils;
import com.istroop.istrooprecognize.utils.WidgetUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class ICardRegistreMobileActivity3 extends BaseActivity implements
        OnClickListener {
    protected static final String TAG = "ICardRegistreMobileActivity3";

    protected static final int MOBILE_REG_SUCCESS    = 1;
    protected static final int MOBILE_REG_FAIL       = 2;
    protected static final int MOBILE_REG_ERROR_JSON = 4;
    protected static final int MOBILE_RESET_SUCCESS  = 5;
    protected static final int MOBILE_RESET_FAIL     = 6;
    private EditText       icard_register_mobile_pwd;
    private String         mobile;
    private String         mobile_findpwd;
    private RelativeLayout icard_register_mobile3_pwd_hint;
    private String         mobile_code;
    private ThreeHander handler = new ThreeHander();

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.icard_register_mobile3 );
        init();
    }

    public void init() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if ( bundle != null ) {
            mobile = bundle.getString( "mobile", "12345678901" );
            mobile_findpwd = bundle.getString( "mobile_findpwd", "超级图片" );
            mobile_code = bundle.getString( "mobile_code" );
        }
        icard_register_mobile_pwd = ( EditText ) findViewById( R.id.icard_register_mobile_pwd );
        RelativeLayout icard_register_mobile_finish = ( RelativeLayout ) findViewById( R.id.icard_register_mobile_finish );
        TextView icard_register_mobile3_cannel = ( TextView ) findViewById( R.id.icard_register_mobile3_cannel );
        TextView icard_register_mobile3_tv = ( TextView ) findViewById( R.id.icard_register_mobile3_tv );
        TextView icard_register_mobile3_setpwd = ( TextView ) findViewById( R.id.icard_register_mobile3_setpwd );
        TextView icard_register_mobile_finish_tv = ( TextView ) findViewById( R.id.icard_register_mobile_finish_tv );
        icard_register_mobile3_pwd_hint = ( RelativeLayout ) findViewById( R.id.icard_register_mobile3_pwd_hint );
        WidgetUtil.invisible( icard_register_mobile_pwd,
                              icard_register_mobile3_pwd_hint );
        if ( "找回密码".equals( mobile_findpwd ) ) {
            icard_register_mobile3_setpwd
                    .setBackgroundResource( R.drawable.setpwd_bar );// 设置密码
            icard_register_mobile3_tv.setText( getResources().getString(
                    R.string.icard_findsms_title )
                                                       + mobile );
            icard_register_mobile_finish_tv.setText( getResources().getString(
                    R.string.icard_register_mobile_finish ) );
        } else {
            icard_register_mobile3_setpwd
                    .setBackgroundResource( R.drawable.bar_title );
            icard_register_mobile3_tv.setText( getResources().getString(
                    R.string.icard_register_mobile_pwd )
                                                       + mobile );
            icard_register_mobile_finish_tv.setText( getResources().getString(
                    R.string.icard_login_no ) );
        }
        icard_register_mobile_finish.setOnClickListener( this );
        icard_register_mobile3_cannel.setOnClickListener( this );
    }

    @Override
    public void onClick( View v ) {
        switch ( v.getId() ) {
            case R.id.icard_register_mobile_finish:
                String pwd = icard_register_mobile_pwd.getText().toString().trim();
                if ( TextUtils.isEmpty( pwd ) ) {
                    Toast.makeText(
                            ICardRegistreMobileActivity3.this,
                            getResources().getString(
                                    R.string.icard_login_password_error ), Toast.LENGTH_SHORT ).show();
                    return;
                }
                if ( !Utils.isPassword( pwd ) ) {
                    icard_register_mobile3_pwd_hint.setVisibility( View.VISIBLE );
                    return;
                }
                isCode( mobile, mobile_code, pwd );
                break;
            case R.id.icard_register_mobile3_cannel:
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString( "login_return", "finish" );
                intent.putExtras( bundle );
                setResult( RESULT_OK, intent );
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString( "login_return", "finish" );
        intent.putExtras( bundle );
        setResult( RESULT_OK, intent );
        finish();
    }

    /**
     * 检验验证码的正确性
     */
    private void isCode( final String mobile, final String code,
                         final String password ) {
        new Thread() {

            public void run() {
                try {
                    if ( "找回密码".equals( mobile_findpwd ) ) {
                        // /Mobile/ForgetPass/?mobile=%@&code=%@&password=%@
                        String regInfo = HttpTools.login(
                                IstroopConstants.URL_PATH
                                        + "/Mobile/ResetPass/?mobile=" + mobile
                                        + "&code=" + code + "&password="
                                        + password,
                                IstroopConstants.cookieStore );
                        Utils.log( TAG, IstroopConstants.URL_PATH
                                + "/Mobile/ResetPass/?mobile=" + mobile
                                + "&code=" + code + "&password=" + password, 4 );
                        Utils.log( TAG, "reset返回的信息:" + regInfo, 4 );
                        // {"success":false,"data":{"errorCode":208},"message":"\u6b64\u7528\u6237\u5df2\u5b58\u5728"}
                        // reset返回的信息:{"success":true,"data":"\u91cd\u7f6e\u6210\u529f"}

                        JSONObject object = new JSONObject( regInfo );
                        if ( object.getBoolean( "success" ) ) {
                            Message message = Message.obtain();
                            message.obj = object.getString( "data" );
                            message.what = MOBILE_RESET_SUCCESS;
                            handler.sendMessage( message );
                        } else {
                            String message = object.getString( "message" );
                            Message msg = Message.obtain();
                            msg.what = MOBILE_RESET_FAIL;
                            msg.obj = message;
                            handler.sendMessage( msg );
                        }
                    } else {
                        String regInfo = HttpTools.login(
                                IstroopConstants.URL_PATH
                                        + "/Mobile/MobileReg/?mobile=" + mobile
                                        + "&code=" + code + "&password="
                                        + password,
                                IstroopConstants.cookieStore );
                        Utils.log( TAG, IstroopConstants.URL_PATH
                                + "/Mobile/MobileReg/?mobile=" + mobile
                                + "&code=" + code + "&password=" + password, 4 );
                        Utils.log( TAG, regInfo, 4 );
                        JSONObject object = new JSONObject( regInfo );
                        if ( object.getBoolean( "success" ) ) {
                            JSONObject dataInfo = object.getJSONObject( "data" );
                            JSONObject userInfo = dataInfo
                                    .getJSONObject( "userinfo" );
                            IstroopConstants.user_id = userInfo
                                    .getString( "uid" );
                            IstroopConstants.user_name = userInfo
                                    .getString( "uname" );
                            IstroopConstants.user_image = userInfo
                                    .getString( "avater" );
                            Message message = Message.obtain();
                            message.what = MOBILE_REG_SUCCESS;
                            handler.sendMessage( message );
                        } else {
                            String message = object.getString( "message" );
                            Message msg = Message.obtain();
                            msg.what = MOBILE_REG_FAIL;
                            msg.obj = message;
                            handler.sendMessage( msg );
                        }
                    }
                } catch ( JSONException e ) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = MOBILE_REG_ERROR_JSON;
                    handler.sendMessage( message );
                }
            }
        }.start();
    }

    class ThreeHander extends Handler {
        @Override
        public void handleMessage( Message msg ) {
            super.handleMessage( msg );
            switch ( msg.what ) {
                case MOBILE_REG_SUCCESS:
                    Toast.makeText(
                            ICardRegistreMobileActivity3.this, "用手机号注册完成", Toast.LENGTH_SHORT ).show();
                    IstroopConstants.isLogin = true;
                    IstroopConstants.isMobile = true;
                    IstroopConstants.mobile = mobile;
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString( "login_return", "success" );
                    intent.putExtras( bundle );
                    setResult( RESULT_OK, intent );
                    finish();
                    break;
                case MOBILE_REG_FAIL:
                    String message = ( String ) msg.obj;
                    Toast.makeText( ICardRegistreMobileActivity3.this, message,
                                    Toast.LENGTH_SHORT ).show();
                    break;
                case MOBILE_REG_ERROR_JSON:
                    Toast.makeText( ICardRegistreMobileActivity3.this, "json error",
                                    Toast.LENGTH_SHORT ).show();
                    break;
                case MOBILE_RESET_SUCCESS:
                    String string = ( String ) msg.obj;
                    Toast.makeText( ICardRegistreMobileActivity3.this, string,
                                    Toast.LENGTH_SHORT ).show();
                    Intent intent_set = new Intent();
                    Bundle bundle_set = new Bundle();
                    bundle_set.putString( "login_return", "finish" );
                    intent_set.putExtras( bundle_set );
                    setResult( RESULT_OK, intent_set );
                    finish();
                    break;
                case MOBILE_RESET_FAIL:
                    String str = ( String ) msg.obj;
                    Toast.makeText( ICardRegistreMobileActivity3.this, str,
                                    Toast.LENGTH_SHORT ).show();
                    break;
                default:
                    break;
            }
        }
    }

}
