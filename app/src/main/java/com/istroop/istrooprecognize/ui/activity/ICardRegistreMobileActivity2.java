package com.istroop.istrooprecognize.ui.activity;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.istroop.istrooprecognize.utils.Okhttps;
import com.istroop.istrooprecognize.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ICardRegistreMobileActivity2 extends BaseActivity implements
        OnClickListener {

    protected static final String TAG                    = "ICardRegistreMobileActivity2";
    protected static final int    ICARD_MOBILE_SUCCESS   = 1;
    protected static final int    ICARD_MOBILE_FAIL      = 2;
    protected static final int    REGISTER1_SEND_SUCCESS = 3;
    protected static final int    ICARD_REGISTER_MOBILE2 = 4;
    private EditText icard_register_mobile_sms_et;
    private String   mobile;
    private TextView icard_register_mobile_sms_time;
    private String   mobile_findpwd;
    private String   mobile_code;
    private MobileHandler handler = new MobileHandler();
    private int     page_number;
    private Okhttps okhttps;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.icard_register_mobile2 );
        init();
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult( requestCode, resultCode, data );
        if ( data == null ) {
            return;
        }
        switch ( requestCode ) {
            case ICARD_REGISTER_MOBILE2:
                setResult( RESULT_OK, data );
                finish();
                break;
            default:
                break;
        }
    }

    public void init() {
        okhttps = Okhttps.getInstance();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if ( bundle != null ) {
            mobile_code = bundle.getString( "mobile_code" );
            Utils.log( TAG, "mobile_code" + mobile_code, 4 );
            mobile = bundle.getString( "mobile" );
            Utils.log( TAG, "mobile" + mobile, 4 );
            mobile_findpwd = bundle.getString( "mobile_findpwd" );
            Utils.log( TAG, "mobile_findpwd" + mobile_findpwd, 4 );
            page_number = bundle.getInt( "page_number" );
        }
        TextView icard_register_mobile_notice = ( TextView ) findViewById( R.id.icard_register_mobile_notice );
        icard_register_mobile_sms_et = ( EditText ) findViewById( R.id.icard_register_mobile_sms_et );
        RelativeLayout icard_register_mobile_next = ( RelativeLayout ) findViewById( R.id.icard_register_mobile_next );
        icard_register_mobile_sms_time = ( TextView ) findViewById( R.id.icard_register_mobile_sms_time );
        TextView icard_register_mobile2_cannel = ( TextView ) findViewById( R.id.icard_register_mobile2_cannel );
        TextView icard_register_mobile2_findpwd = ( TextView ) findViewById( R.id.icard_register_mobile2_findpwd );
        icard_register_mobile_sms_et.setSelection( 0 );
        if ( "找回密码".equals( mobile_findpwd ) ) {
            icard_register_mobile2_findpwd
                    .setBackgroundResource( R.drawable.findpwd_bar );
        } else {
            icard_register_mobile2_findpwd
                    .setBackgroundResource( R.drawable.bar_title );
        }
        icard_register_mobile_notice.setText( getResources().getString(
                R.string.icard_register_mobile_notice )
                                                      + mobile );
        icard_register_mobile_next.setOnClickListener( this );
        icard_register_mobile2_cannel.setOnClickListener( this );
        icard_register_mobile_sms_time.setOnClickListener( this );

        CountDownTimer timer = new CountDownTimer( 60000, 1000 ) {
            @Override
            public void onTick( long millisUntilFinished ) {
                icard_register_mobile_sms_time.setText( getResources()
                                                                .getString( R.string.icard_register_mobile_time1 )
                                                                + millisUntilFinished
                        / 1000
                                                                + getResources().getString(
                        R.string.icard_register_mobile_time2 ) );
                icard_register_mobile_sms_time.setTextColor( getResources()
                                                                     .getColor( R.color.black ) );
            }

            @Override
            public void onFinish() {
                icard_register_mobile_sms_time.setText( getResources()
                                                                .getString( R.string.icard_register_mobile_resend ) );
                icard_register_mobile_sms_time.setTextColor( getResources()
                                                                     .getColor( R.color.bule ) );
            }
        };
        timer.start();

    }

    @Override
    public void onClick( View v ) {
        switch ( v.getId() ) {
            case R.id.icard_register_mobile2_cannel:
                final Dialog serviceDialog2 = new Builder(
                        ICardRegistreMobileActivity2.this )
                        .setMessage(
                                getResources().getString(
                                        R.string.icard_register_mobile_yanchi ) )
                        .setPositiveButton(
                                getResources().getString(
                                        R.string.icard_register_mobile_wait ),
                                ( dialog, which ) -> {
                                    dialog.dismiss();
                                } )
                        .setNegativeButton(
                                getResources().getString( R.string.cannel ),
                                ( dialog, which ) -> {
                                    Intent intent = new Intent();
                                    Bundle bundle = new Bundle();
                                    bundle.putString( "login_return", "finish" );
                                    intent.putExtras( bundle );
                                    setResult( RESULT_OK, intent );
                                    finish();
                                } ).create();
                serviceDialog2.show();
                break;
            case R.id.icard_register_mobile_next:
                Utils.log( TAG, mobile + mobile_code, 4 );
                // 验证短信验证码的正确性,并进行下一步操作
                icard_register_mobile_sms_time.setVisibility( View.GONE );
                String sms = icard_register_mobile_sms_et.getText().toString()
                        .trim();
                Utils.log( TAG, sms + ":" + mobile_code, 4 );
                if ( TextUtils.isEmpty( sms ) ) {
                    Toast.makeText(
                            ICardRegistreMobileActivity2.this,
                            getResources().getString(
                                    R.string.icard_register_mobile_sms ), Toast.LENGTH_SHORT ).show();
                    return;
                }
                isCode( sms );
            /*
             * if (!sms.equals(mobile_code)) { }
			 */
                break;
            case R.id.icard_register_mobile_sms_time:
                if ( getResources().getString( R.string.icard_register_mobile_resend )
                        .equals( icard_register_mobile_sms_time.getText() ) ) {
                    new Thread() {
                        public void run() {
                            try {
                                String string = okhttps.get(
                                        IstroopConstants.URL_PATH
                                                + "/Mobile/ForgetPass/?mobile="
                                                + mobile );
//                                        HttpTools
//                                        .toString( IstroopConstants.URL_PATH
//                                                           + "/Mobile/ForgetPass/?mobile="
//                                                           + mobile );
                                JSONObject object = new JSONObject( string );
                                if ( object.getBoolean( "success" ) ) {
                                    mobile_code = object.getString( "data" );
                                    Message message = Message.obtain();
                                    message.what = REGISTER1_SEND_SUCCESS;
                                    handler.sendMessage( message );
                                }
                            } catch ( Exception e ) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } else {
                    return;
                }
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

    private void isCode( final String sms ) {
        new Thread() {
            public void run() {
                // Mobile/Validatecode?mobile=***&code=**
                try {
                    String string = okhttps.get( IstroopConstants.URL_PATH
                                                         + "/Mobile/Validatecode?mobile=" + mobile
                                                         + "&code=" + sms );
//                            HttpTools
//                            .toString( IstroopConstants.URL_PATH
//                                               + "/Mobile/Validatecode?mobile=" + mobile
//                                               + "&code=" + sms );
                    if ( !TextUtils.isEmpty( string ) ) {
                        JSONObject jsonObject = new JSONObject( string );
                        if ( jsonObject.getBoolean( "success" ) ) {
                            Message message = Message.obtain();
                            message.what = ICARD_MOBILE_SUCCESS;
                            message.obj = sms;
                            handler.sendMessage( message );
                        } else {
                            String msg = jsonObject.getString( "message" );
                            Message message = Message.obtain();
                            message.obj = msg;
                            message.what = ICARD_MOBILE_FAIL;
                            handler.sendMessage( message );
                        }
                    }
                } catch ( JSONException | IOException e ) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    class MobileHandler extends Handler {
        @Override
        public void handleMessage( Message msg ) {
            super.handleMessage( msg );
            switch ( msg.what ) {
                case ICARD_MOBILE_SUCCESS:
                    mobile_code = ( String ) msg.obj;
                    Utils.log( TAG, "验证码:" + mobile_code, 4 );
                    Intent intent = new Intent( ICardRegistreMobileActivity2.this,
                                                ICardRegistreMobileActivity3.class );
                    Bundle bundle = new Bundle();
                    bundle.putString( "mobile", mobile );
                    bundle.putString( "mobile_findpwd", mobile_findpwd );
                    bundle.putString( "mobile_code", mobile_code );
                    bundle.putInt( "page_number", page_number );
                    intent.putExtras( bundle );
                    startActivityForResult( intent, ICARD_REGISTER_MOBILE2 );
                    break;
                case ICARD_MOBILE_FAIL:
                    Builder builder = new Builder(
                            ICardRegistreMobileActivity2.this );
                    builder.setMessage( getResources().getString(
                            R.string.icard_register_mobile_reinput ) );
                    // builder.setMessage(message);
                    builder.setPositiveButton(
                            getResources().getString(
                                    R.string.icard_register_mobile_sure ),
                            ( dialog, which ) -> {
                                dialog.dismiss();
                            } );
                    builder.create().show();
                    break;
                case REGISTER1_SEND_SUCCESS:
                    CountDownTimer timer = new CountDownTimer( 60000, 1000 ) {
                        @Override
                        public void onTick( long millisUntilFinished ) {
                            icard_register_mobile_sms_time
                                    .setText( getResources().getString(
                                            R.string.icard_register_mobile_time1 )
                                                      + millisUntilFinished
                                            / 1000
                                                      + getResources()
                                            .getString(
                                                    R.string.icard_register_mobile_time2 ) );
                            icard_register_mobile_sms_time
                                    .setTextColor( getResources().getColor(
                                            R.color.black ) );
                        }

                        @Override
                        public void onFinish() {
                            icard_register_mobile_sms_time.setText( getResources()
                                                                            .getString(
                                                                                    R.string.icard_register_mobile_resend ) );
                            icard_register_mobile_sms_time
                                    .setTextColor( getResources().getColor(
                                            R.color.bule ) );
                        }
                    };
                    timer.start();

                    break;
                default:
                    break;
            }
        }
    }
}
