package com.istroop.istrooprecognize.ui.activity;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ICardRegistreMobileActivity extends BaseActivity implements
        OnClickListener {

    protected static final String TAG = "ICardRegistreMobileActivity";

    protected static final int REGISTER_SEND_SUCCESS  = 1;
    protected static final int ICARD_REGISTER_MOBILE1 = 2;
    protected static final int REGISTER_SEND_FAIL     = 3;
    private EditText icard_register_mobile;
    private String   mobile_findpwd;
    private String   code;
    private RegistreHandler handler = new RegistreHandler();
    private String mobile;
    private int    page_number;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.icard_register_mobile );
        init();
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult( requestCode, resultCode, data );
        if ( data == null ) {
            return;
        }
        switch ( requestCode ) {
            case ICARD_REGISTER_MOBILE1:
                setResult( RESULT_OK, data );
                finish();
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

    public void init() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if ( bundle != null ) {
            mobile_findpwd = bundle.getString( "mobile_findpwd", "超级图片" );
            page_number = bundle.getInt( "page_number" );
        }
        icard_register_mobile = ( EditText ) findViewById( R.id.icard_register_mobile );
        RelativeLayout icard_register_mobile1_next = ( RelativeLayout ) findViewById( R.id.icard_register_mobile1_next );
        TextView icard_register_mobile_cannel = ( TextView ) findViewById( R.id.icard_register_mobile_cannel );
        TextView icard_register_mobile_findpwd = ( TextView ) findViewById( R.id.icard_register_mobile_findpwd );
        TextView icard_register_mobile_number = ( TextView ) findViewById( R.id.icard_register_mobile_number );
        RelativeLayout icard_register_mobile_protocol = ( RelativeLayout ) findViewById( R.id.icard_register_mobile_protocol );
        icard_register_mobile.setCursorVisible( true );
        if ( "找回密码".equals( mobile_findpwd ) ) {
            icard_register_mobile_number.setText( getResources().getString(
                    R.string.icard_register_reset ) );
            icard_register_mobile_protocol.setVisibility( View.INVISIBLE );
            icard_register_mobile_findpwd
                    .setBackgroundResource( R.drawable.findpwd_bar );
        } else {
            icard_register_mobile_number.setText( getResources().getString(
                    R.string.icard_register_mobile_input ) );
            icard_register_mobile_protocol.setVisibility( View.VISIBLE );
            icard_register_mobile_findpwd
                    .setBackgroundResource( R.drawable.bar_title );
        }
        icard_register_mobile1_next.setOnClickListener( this );
        icard_register_mobile_cannel.setOnClickListener( this );
    }

    @Override
    public void onClick( View v ) {
        switch ( v.getId() ) {
            case R.id.icard_register_mobile1_next:
                mobile = icard_register_mobile.getText().toString().trim();
                if ( TextUtils.isEmpty( mobile ) ) {
                    Toast.makeText(
                            ICardRegistreMobileActivity.this,
                            getResources().getString(
                                    R.string.icard_register_mobile_shuru ), Toast.LENGTH_SHORT )
                            .show();
                    return;
                }
                if ( mobile.length() != 11 ) {
                    Builder builder = new Builder( this );
                    builder.setTitle( getResources().getString(
                            R.string.icard_register_mobile_error ) );
                    builder.setMessage( getResources().getString(
                            R.string.icard_register_mobile_error_wuxiao ) );
                    builder.setPositiveButton(
                            getResources().getString(
                                    R.string.icard_register_mobile_sure ),
                            ( dialog, which ) -> {
                                dialog.dismiss();
                            } );
                    builder.create().show();
                    return;
                }
                Dialog serviceDialog2 = new Builder(
                        ICardRegistreMobileActivity.this )
                        .setTitle(
                                getResources().getString(
                                        R.string.icard_register_sure_mobile ) )
                        .setMessage(
                                getResources().getString(
                                        R.string.icard_register_sure_send )
                                        + mobile )
                        .setPositiveButton(
                                getResources().getString(
                                        R.string.design_headimage_quit ),
                                ( dialog, which ) -> {
                                    dialog.dismiss();
                                } )
                        .setNegativeButton(
                                getResources().getString(
                                        R.string.icard_register_sure_OK ),
                                ( dialog, which ) -> {
                                    // http://tapi.tujoin.com/Mobile/MobileCodeSend/?mobile=*****
                                    new Thread() {
                                        public void run() {
                                            try {
                                                String string;
                                                if ( "找回密码"
                                                        .equals( mobile_findpwd ) ) {
                                                    string = HttpTools
                                                            .toString( IstroopConstants.URL_PATH
                                                                               + "/Mobile/ForgetPass?mobile="
                                                                               + mobile );
                                                    Log.i( TAG, "找回密码返回的信息:"
                                                            + string );
                                                } else {
                                                    string = HttpTools
                                                            .toString( IstroopConstants.URL_PATH
                                                                               + "/Mobile/MobileCodeSend/?mobile="
                                                                               + mobile );
                                                }
                                                if ( TextUtils.isEmpty( string ) ) {
                                                    Message message = Message
                                                            .obtain();
                                                    message.what = REGISTER_SEND_FAIL;
                                                    handler.sendMessage( message );
                                                    return;
                                                }
                                                JSONObject object = new JSONObject(
                                                        string );
                                                if ( object
                                                        .getBoolean( "success" ) ) {
                                                    code = object
                                                            .getString( "data" );
                                                    Message message = Message
                                                            .obtain();
                                                    message.what = REGISTER_SEND_SUCCESS;
                                                    handler.sendMessage( message );
                                                } else {
                                                    String data = object
                                                            .getString( "message" );
                                                    Message message = Message
                                                            .obtain();
                                                    message.what = REGISTER_SEND_FAIL;
                                                    message.obj = data;
                                                    handler.sendMessage( message );
                                                }
                                            } catch ( IOException | JSONException e ) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                } ).create();
                serviceDialog2.show();
                break;
            case R.id.icard_register_mobile_cannel:
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString( "login_return", "finish" );
                intent.putExtras( bundle );
                setResult( RESULT_OK, intent );
                finish();
            default:
                break;
        }
    }

    class RegistreHandler extends Handler {
        @Override
        public void handleMessage( Message msg ) {
            super.handleMessage( msg );
            switch ( msg.what ) {
                case REGISTER_SEND_SUCCESS:
                    Intent intent2 = new Intent( ICardRegistreMobileActivity.this,
                                                 ICardRegistreMobileActivity2.class );
                    Bundle bundle = new Bundle();
                    bundle.putString( "mobile", mobile );
                    bundle.putString( "mobile_code", code );
                    bundle.putString( "mobile_findpwd", mobile_findpwd );
                    bundle.putInt( "page_number", page_number );
                    intent2.putExtras( bundle );
                    startActivityForResult( intent2, ICARD_REGISTER_MOBILE1 );
                    break;
                case REGISTER_SEND_FAIL:
                    Toast.makeText(
                            ICardRegistreMobileActivity.this,
                            getResources().getString(
                                    R.string.icard_register_mobile_already ),
                            Toast.LENGTH_SHORT ).show();
                    break;
                default:
                    break;
            }
        }
    }

}
