package com.istroop.istrooprecognize.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.istroop.istrooprecognize.BaseActivity;
import com.istroop.istrooprecognize.IstroopConstants;
import com.istroop.istrooprecognize.R;
import com.istroop.istrooprecognize.utils.HttpTools;
import com.istroop.istrooprecognize.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class ICardLoginActivtiy extends BaseActivity implements OnClickListener {

    private static final   String TAG                         = "ICardLoginActivtiy";
    protected static final int    ERROR_LOGIN                 = 3;
    protected static final int    SUCCESS_LOGIN               = 5;
    protected static final int    ICARD_OTHER_FAIL            = 7;
    private static final   int    PROGRESS                    = 0;
    private static final   int    ICARD_LOGIN_REGISTER_RESULT = 100;
    protected static final int    NET_ERROR_LOGIN             = 10;
    private EditText     icard_login_mail_et;
    private EditText     icard_login_password_et;
    private LinearLayout icard_login_select_ll;
    private boolean      isMobile;
    private Context      mcontext;

    private LoginHandler handler = new LoginHandler();

    private int page_number;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.icard_login );
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if ( bundle != null ) {
            page_number = bundle.getInt( "page_number" );
        }
        mcontext = getApplicationContext();

        SharedPreferences preferences = getSharedPreferences( "config",
                                                              MODE_PRIVATE );

        IstroopConstants.isLogin = preferences.getBoolean( "isLogin", false );

        if ( IstroopConstants.isLogin ) {
            IstroopConstants.mobile = preferences.getString( "username", null );
            String password = preferences.getString( "password", null );
            Log.i( TAG, "isLogin" + IstroopConstants.isLogin );
            Log.i( TAG, "mobile" + IstroopConstants.mobile );
            isUserPwd( IstroopConstants.mobile, password );
        }

        icard_login_mail_et = ( EditText ) findViewById( R.id.icard_login_mail_et );
        icard_login_password_et = ( EditText ) findViewById( R.id.icard_login_password_et );
        icard_login_select_ll = ( LinearLayout ) findViewById( R.id.icard_login_select_ll );
        TextView icard_login_findpwd = ( TextView ) findViewById( R.id.icard_login_findpwd );
        icard_login_findpwd.setOnClickListener( this );
        Button icard_login_select_mobile = ( Button ) findViewById( R.id.icard_login_select_mobile );
        Button icard_login_select_cancel = ( Button ) findViewById( R.id.icard_login_select_cancel );
        Button icard_login_bt = ( Button ) findViewById( R.id.icard_login_bt );
        TextView icard_login_cannel = ( TextView ) findViewById( R.id.icard_login_cannel );
        icard_login_cannel.setOnClickListener( this );
        icard_login_bt.setOnClickListener( this );
        isMobile = false;
        icard_login_select_mobile.setOnClickListener( this );
        icard_login_select_cancel.setOnClickListener( this );
    }

    @Override
    public void onClick( View v ) {
        switch ( v.getId() ) {
            case R.id.icard_login_cannel:
                finish();
                break;
            case R.id.icard_login_findpwd:
                // 忘记密码 进入手机找回密码界面
                Intent intent2 = new Intent( ICardLoginActivtiy.this,
                                             ICardRegistreMobileActivity.class );
                Bundle bundle = new Bundle();
                bundle.putString( "mobile_findpwd", "找回密码" );
                bundle.putInt( "page_number", page_number );
                intent2.putExtras( bundle );
                startActivityForResult( intent2, ICARD_LOGIN_REGISTER_RESULT );
                break;
            case R.id.icard_login_select_mobile:
                isMobile = true;
                icard_login_mail_et.setHint( "手机号" );
                IstroopConstants.isMobile = isMobile;
                icard_login_select_ll.setVisibility( View.GONE );
                break;
            case R.id.icard_login_select_cancel:
                icard_login_select_ll.setVisibility( View.GONE );
                break;
            case R.id.icard_login_bt:
                String mail = icard_login_mail_et.getText().toString().trim();
                String password = icard_login_password_et.getText().toString()
                        .trim();
                if ( TextUtils.isEmpty( mail ) ) {
                    Toast.makeText( ICardLoginActivtiy.this, "账号不能为空",
                                    Toast.LENGTH_SHORT ).show();
                    IstroopConstants.isLogin = false;
                    return;
                } else if ( TextUtils.isEmpty( password ) ) {
                    Toast.makeText( ICardLoginActivtiy.this, "密码不能为空",
                                    Toast.LENGTH_SHORT ).show();
                    IstroopConstants.isLogin = false;
                    return;
                }
                isUserPwd( mail, password );
                break;
            default:
                break;
        }
    }

    /**
     * 用户登录方法
     *
     * @param mail 邮箱帐号
     * @param password 密码
     */
    public void isUserPwd( final String mail, final String password ) {
        // 1.4 手机号登陆 /Mobile/MobileLogin/
        new Thread() {
            public void run() {
                try {
                    String loginInfo;
                    IstroopConstants.cookieStore = null;
                    loginInfo = HttpTools.login( IstroopConstants.URL_PATH
                                                         + "/Mobile/login/?user=" + mail + "&password="
                                                         + password, IstroopConstants.cookieStore );
                    Log.i( TAG, "denglu:" + loginInfo );
                    if ( TextUtils.isEmpty( loginInfo ) ) {
                        Message message = Message.obtain();
                        message.what = NET_ERROR_LOGIN;
                        handler.sendMessage( message );
                        return;
                    }
                    JSONObject jsonObject = new JSONObject( loginInfo );
                    if ( jsonObject.getBoolean( "success" ) ) {
                        IstroopConstants.isLogin = true;
                        IstroopConstants.mobile = mail;

                        Log.i( TAG, "cookie信息:"
                                + IstroopConstants.cookieStore );

                        if ( jsonObject.getBoolean( "success" ) ) {
                            JSONObject dataObject = jsonObject
                                    .getJSONObject( "data" );
                            IstroopConstants.cookie = dataObject
                                    .getString( "token" );
                            JSONObject userinfo = dataObject
                                    .getJSONObject( "userinfo" );
                            String uid = userinfo.getString( "uid" );// 用户id
                            String uname = userinfo.getString( "uname" );// 用户名
                            String avater = userinfo.getString( "avater" );// 用户头像
                            IstroopConstants.user_id = uid;
                            IstroopConstants.user_name = uname;
                            IstroopConstants.user_image = avater;
                            if ( !Utils.isNotPhoneNumber( mail ) ) {
                                Log.i( TAG, "用手机号进行登陆" );
                                IstroopConstants.isMobile = true;
                                IstroopConstants.mobile = mail;
                            }
                        }
                        Message message = Message.obtain();
                        message.what = SUCCESS_LOGIN;
                        handler.sendMessage( message );
                        if ( mcontext != null ) {
                            saveLoginInfo( mcontext, password );
                        }

                    } else {
                        // {"success":false,"data":{"errorCode":102},"message":"\u624b\u673a\u53f7\u6216\u5bc6\u7801\u9519\u8bef"}
                        String msg = jsonObject.getString( "message" );
                        Log.i( TAG, "登陆错误返回的信息:" + msg );
                        Message message = Message.obtain();
                        message.obj = msg;
                        message.what = ERROR_LOGIN;
                        handler.sendMessage( message );
                    }
                } catch ( JSONException e ) {
                    Message message = Message.obtain();
                    message.what = NET_ERROR_LOGIN;
                    handler.sendMessage( message );
                }
            }
        }.start();
    }

    public void saveLoginInfo( Context ctx, String password ) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences( "config", MODE_PRIVATE );
        Editor edit = sharedPreferences.edit();
        edit.putBoolean( "isLogin", IstroopConstants.isLogin );
        edit.putString( "username", IstroopConstants.mobile );
        edit.putString( "password", password );
        edit.apply();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode,
                                     Intent data ) {
        super.onActivityResult( requestCode, resultCode, data );
        Log.i( TAG, "requestCode:" + requestCode );
        Log.i( TAG, "resultCode:" + resultCode );
        if ( requestCode == ICARD_LOGIN_REGISTER_RESULT ) {
            Log.i( TAG, "jinrurequestCode:" + requestCode );
            if ( data != null ) {
                Bundle extras = data.getExtras();
                if ( extras != null ) {
                    String login_return = extras.getString( "login_return" );
                    Log.i( TAG, "login_return:" + login_return );
                    if ( "success".equals( login_return ) ) {
                        finish();
                        MainActivity.tabHost.setCurrentTab( 0 );
                    }
                }
            }
        }
    }

    @Override
    protected Dialog onCreateDialog( int id ) {
        Dialog dialog = null;
        switch ( id ) {
            case PROGRESS:
                dialog = new ProgressDialog( this );
                ( ( ProgressDialog ) dialog ).setMessage( "请求中,请稍等..." );
                break;
        }
        return dialog;
    }

    class LoginHandler extends Handler {
        @Override
        public void handleMessage( Message msg ) {
            super.handleMessage( msg );
            switch ( msg.what ) {
                case ERROR_LOGIN:
                    String message = ( String ) msg.obj;
                    //TODO 登录失败打印有问题。。。
                    Toast.makeText( ICardLoginActivtiy.this, message,
                                    Toast.LENGTH_SHORT ).show();
                    IstroopConstants.isLogin = false;
                    break;
                case SUCCESS_LOGIN:
                    //TODO
                /* icard_login_error_ll.setVisibility(View.INVISIBLE); */
                    IstroopConstants.isLogin = true;
                    MainActivity.tabHost.setCurrentTab( 1 );
                    finish();
                    break;
                case ICARD_OTHER_FAIL:
                    Toast.makeText( ICardLoginActivtiy.this, "授权失败",
                                    Toast.LENGTH_SHORT ).show();
                    break;
                case NET_ERROR_LOGIN:
                    Toast.makeText( ICardLoginActivtiy.this,
                                    getResources().getString( R.string.icard_net_error ),
                                    Toast.LENGTH_SHORT ).show();
                    break;
                default:
                    break;
            }
        }
    }

}
