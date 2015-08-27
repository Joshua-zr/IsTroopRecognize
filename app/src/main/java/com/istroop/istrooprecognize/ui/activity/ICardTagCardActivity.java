package com.istroop.istrooprecognize.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.istroop.istrooprecognize.BaseActivity;
import com.istroop.istrooprecognize.IstroopConstants;
import com.istroop.istrooprecognize.R;
import com.istroop.istrooprecognize.utils.BroadcastHelper;
import com.istroop.istrooprecognize.utils.HttpTools;
import com.istroop.istrooprecognize.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ICardTagCardActivity extends BaseActivity implements OnClickListener {

    private static final   String TAG                        = "ICardTagCardActivity";
    protected static final int    ICARD_TAG_CARD_ADD_SUCCESS = 1;
    protected static final int    ICARD_TAG_CARD_ADD_FAIL    = 2;
    protected static final int    ICARD_TAG_CARD_SUCCESS     = 3;
    protected static final int    ICARD_TAG_CARD_FAIL        = 4;
    protected static final int    ICARD_TAG_CARD_SET_SUCCESS = 5;
    protected static final int    ICARD_TAG_CARD_SET_FAIL    = 6;
    protected static final int    ICARD_TAG_CARDS_SUCCESS    = 7;
    protected static final int    ICARD_TAG_CARDS_FAIL       = 8;
    private EditText icard_tag_card_name;
    private EditText icard_tag_card_mobile;
    private EditText icard_tag_card__mail;
    private EditText icard_tag_card_conpany;
    private EditText icard_tag_card_part;
    private EditText icard_tag_card_job;
    private EditText icard_tag_card_index;
    private EditText icard_tag_card_address;
    private EditText icard_tag_card_signature;
    private EditText icard_tag_card_weixin;

    private String                  card_id;
    private HashMap<String, String> map;
    private String[]                cardInfos;
    private String                  cards_name;
    private String                  cards_mobile;
    private String                  cards_mail;
    private String                  cards_company;
    private String                  cards_part;
    private String                  cards_job;
    private String                  cards_index;
    private String                  cards_address;
    private String                  cards_signature;
    private String                  cards_weixin;
    // private ImageView icard_card_menu;
    // private static final String SHAREDPREFERENCES_NAME = "first_pref";
    private Handler handler = new Handler();
    private String[] copyrights;
    private String   cards_type;
    private boolean isFirstIn = false;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.icard_tag_card );
        init();
    }

    private void init() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if ( extras != null ) {
            card_id = extras.getString( "card_id" );
            copyrights = extras.getStringArray( "copyrights" );
            cards_type = extras.getString( "cards_type" );
            isFirstIn = extras.getBoolean( "isFirstIn" );
            Log.i( TAG, "获取到的card_id:" + card_id );
        }
        TextView icard_tag_card_cannel = ( TextView ) findViewById( R.id.icard_tag_card_cannel );
        RelativeLayout icard_tag_card_save = ( RelativeLayout ) findViewById( R.id.icard_tag_card_save );
        icard_tag_card_name = ( EditText ) findViewById( R.id.icard_tag_card_name );
        icard_tag_card_mobile = ( EditText ) findViewById( R.id.icard_tag_card_mobile );
        icard_tag_card__mail = ( EditText ) findViewById( R.id.icard_tag_card__mail );
        icard_tag_card_conpany = ( EditText ) findViewById( R.id.icard_tag_card_conpany );
        icard_tag_card_part = ( EditText ) findViewById( R.id.icard_tag_card_part );
        icard_tag_card_job = ( EditText ) findViewById( R.id.icard_tag_card_job );
        icard_tag_card_index = ( EditText ) findViewById( R.id.icard_tag_card_index );
        icard_tag_card_address = ( EditText ) findViewById( R.id.icard_tag_card_address );
        icard_tag_card_signature = ( EditText ) findViewById( R.id.icard_tag_card_signature );
        icard_tag_card_weixin = ( EditText ) findViewById( R.id.icard_tag_card_weixin );
        Button icard_tag_card_preview = ( Button ) findViewById( R.id.icard_tag_card_preview );
        RelativeLayout icard_card_cancel_rl = ( RelativeLayout ) findViewById( R.id.icard_card_cancel_rl );
        TextView icard_tag_card_title = ( TextView ) findViewById( R.id.icard_tag_card_title );
        RelativeLayout icard_card_rl = ( RelativeLayout ) findViewById( R.id.icard_card_rl );
        TextView icard_tag_card_save_tv = ( TextView ) findViewById( R.id.icard_tag_card_save_tv );
        if ( isFirstIn ) {
            icard_tag_card_save_tv.setText( getResources().getString(
                    R.string.icard_register_mobile_next ) );
            icard_card_rl.setVisibility( View.GONE );
        } else {
            icard_card_rl.setVisibility( View.VISIBLE );
            icard_tag_card_save_tv.setText( getResources().getString(
                    R.string.save ) );
        }

        if ( card_id != null ) {
            icard_tag_card_title.setText( getResources().getString(
                    R.string.version_title ) );
            getCard();
        } else {
            icard_tag_card_title.setText( getResources().getString(
                    R.string.version_title ) );
        }
        if ( copyrights != null && copyrights.length != 0 ) {
            icard_tag_card_name.setText( copyrights[0] );
            icard_tag_card_mobile.setText( copyrights[1] );
            icard_tag_card__mail.setText( copyrights[2] );
            icard_tag_card_conpany.setText( copyrights[3] );
            icard_tag_card_part.setText( copyrights[4] );
            icard_tag_card_job.setText( copyrights[5] );
            icard_tag_card_index.setText( copyrights[6] );
            icard_tag_card_address.setText( copyrights[7] );
            icard_tag_card_signature.setText( copyrights[8] );
            icard_tag_card_weixin.setText( copyrights[9] );
        }

        icard_tag_card_cannel.setOnClickListener( this );

        icard_tag_card_save.setOnClickListener( this );
        icard_tag_card_preview.setOnClickListener( this );
        icard_card_cancel_rl.setOnClickListener( this );
    }

    private void initCards() {
        new Thread() {
            public void run() {
                Log.i( TAG, "进入子线程" );
                /* cardsList = helper.queryALL(); */
                String cards = HttpTools.userInfo( IstroopConstants.URL_PATH
                                                           + "/ICard/MyCard/", IstroopConstants.cookieStore );
                Log.i( TAG, "获取用户的名片信息:" + cards );
                if ( cards != null ) {
                    try {
                        ICardTagCardsActivity.cardsList = new ArrayList<>();
                        JSONObject object = new JSONObject( cards );
                        if ( object.getBoolean( "success" ) ) {
                            JSONArray jsonArray = object.getJSONArray( "data" );
                            HashMap<String, String> map;
                            for ( int i = 0; i < jsonArray.length(); i++ ) {
                                map = new HashMap<>();
                                String cards_id = jsonArray.getJSONObject( i )
                                        .getString( "cid" );
                                String is_default = jsonArray.getJSONObject( i )
                                        .getString( "is_default" );
                                JSONObject params = jsonArray.getJSONObject( i )
                                        .getJSONObject( "params" );
                                String cards_name = params.getString( "Name" );
                                String cards_mobile = params.getString( "Phone" );
                                String cards_mail = params.getString( "Mail" );
                                String cards_company;
                                if ( params.isNull( "Company" ) ) {
                                    cards_company = "";
                                } else {
                                    cards_company = params.getString( "Company" );
                                }
                                String cards_part = params
                                        .getString( "Department" );
                                String cards_job = params.getString( "Position" );
                                String cards_index = params
                                        .getString( "CompanyWeb" );
                                String cards_address = params
                                        .getString( "Address" );
                                String cards_signature = params
                                        .getString( "Sign" );
                                String cards_weixin = params
                                        .getString( "Weixin" );
                                map.put( "cards_id", cards_id );
                                map.put( "cards_name", cards_name );
                                map.put( "cards_mobile", cards_mobile );
                                map.put( "cards_mail", cards_mail );
                                map.put( "cards_company", cards_company );
                                map.put( "cards_part", cards_part );
                                map.put( "cards_job", cards_job );
                                map.put( "cards_index", cards_index );
                                map.put( "cards_address", cards_address );
                                map.put( "cards_signature ", cards_signature );
                                map.put( "cards_weixin", cards_weixin );
                                map.put( "is_default", is_default );
                                ICardTagCardsActivity.cardsList.add( map );
                            }
                            Message message = Message.obtain();
                            message.what = ICARD_TAG_CARDS_SUCCESS;
                            handler.sendMessage( message );
                            Log.i( TAG, "发送信息更新cards" );
                        } else {
                            Message message = Message.obtain();
                            message.what = ICARD_TAG_CARDS_FAIL;
                            handler.sendMessage( message );
                        }
                    } catch ( JSONException e ) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void getCard() {
        new Thread() {
            public void run() {
                String cards = HttpTools.userInfo( IstroopConstants.URL_PATH
                                                           + "/ICard/getCard/?cid=" + card_id,
                                                   IstroopConstants.cookieStore );
                if ( cards != null ) {
                    try {
                        JSONObject object = new JSONObject( cards );
                        if ( object.getBoolean( "success" ) ) {
                            JSONObject jsonObject = object
                                    .getJSONObject( "data" );
                            map = new HashMap<>();
                            JSONObject params = jsonObject
                                    .getJSONObject( "params" );
                            String cards_name = params.getString( "Name" );
                            String cards_mobile = params.getString( "Phone" );
                            String cards_mail = params.getString( "Mail" );
                            String cards_company = params.getString( "Company" );
                            String cards_part = params.getString( "Department" );
                            String cards_job = params.getString( "Position" );
                            String cards_index = params.getString( "CompanyWeb" );
                            String cards_address = params.getString( "Address" );
                            String cards_signature = null;
                            if ( !params.isNull( "Sign" ) ) {
                                cards_signature = params.getString( "Sign" );
                            }
                            String cards_weixin = null;
                            if ( !params.isNull( "Weixin" ) ) {
                                cards_weixin = params.getString( "Weixin" );
                            }
                            map.put( "cards_name", cards_name );
                            map.put( "cards_mobile", cards_mobile );
                            map.put( "cards_mail", cards_mail );
                            map.put( "cards_company", cards_company );
                            map.put( "cards_part", cards_part );
                            map.put( "cards_job", cards_job );
                            map.put( "cards_index", cards_index );
                            map.put( "cards_address", cards_address );
                            map.put( "cards_signature", cards_signature );
                            map.put( "cards_weixin", cards_weixin );
                            Message message = Message.obtain();
                            message.what = ICARD_TAG_CARD_SUCCESS;
                            handler.sendMessage( message );
                        } else {
                            Message message = Message.obtain();
                            message.what = ICARD_TAG_CARD_FAIL;
                            handler.sendMessage( message );
                        }
                    } catch ( JSONException e ) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void getText() {
        cards_name = icard_tag_card_name.getText().toString().trim();
        cards_mobile = icard_tag_card_mobile.getText().toString().trim();
        cards_mail = icard_tag_card__mail.getText().toString().trim();
        cards_company = icard_tag_card_conpany.getText().toString().trim();
        cards_part = icard_tag_card_part.getText().toString().trim();
        cards_job = icard_tag_card_job.getText().toString().trim();
        cards_index = icard_tag_card_index.getText().toString().trim();
        cards_address = icard_tag_card_address.getText().toString().trim();
        cards_signature = icard_tag_card_signature.getText().toString().trim();
        cards_weixin = icard_tag_card_weixin.getText().toString().trim();
        cardInfos = new String[] { cards_name, cards_mobile, cards_mail,
                cards_company, cards_part, cards_job, cards_index,
                cards_address, cards_signature, cards_weixin };
    }

    @Override
    public void onClick( View v ) {
        switch ( v.getId() ) {
            case R.id.icard_tag_card_cannel:
                Log.i( TAG, "点击cancel" );
                finish();
                break;
            case R.id.icard_tag_card_save:
                Log.i( TAG, "点击save" );
                getText();
                if ( TextUtils.isEmpty( cards_name ) ) {
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.icard_version_name_null ),
                            Toast.LENGTH_SHORT ).show();
                    return;
                }
                if ( TextUtils.isEmpty( cards_mobile ) ) {
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.icard_version_mobile_null ),
                            Toast.LENGTH_SHORT ).show();
                    return;
                }
                if ( Utils.isNotPhoneNumber( cards_mobile ) ) {
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.icard_version_mobile_error ),
                            Toast.LENGTH_SHORT ).show();
                    return;
                }
                if ( !TextUtils.isEmpty( cards_mail ) ) {
                    if ( Utils.isNotEmail( cards_mail ) ) {
                        Toast.makeText(
                                this,
                                getResources().getString(
                                        R.string.icard_version_mail_error ),
                                Toast.LENGTH_SHORT ).show();
                        return;
                    }
                }
                if ( TextUtils.isEmpty( cards_index ) ) {
                    Toast.makeText( this, "公司网址不能为空", Toast.LENGTH_SHORT ).show();
                    return;
                } else {
                    if ( Utils.isNotIndex( cards_index ) ) {
                        Toast.makeText(
                                this,
                                getResources().getString(
                                        R.string.icard_version_website_error ),
                                Toast.LENGTH_SHORT ).show();
                        return;
                    }
                }
                if ( !IstroopConstants.isLogin ) {
                    if ( isFirstIn ) {
                        IstroopConstants.card_temp = "/ICard/setCard/?params[Name]="
                                + cards_name
                                + "&params[Phone]="
                                + cards_mobile
                                + "&params[Mail]="
                                + cards_mail
                                + "&params[Company]="
                                + cards_company
                                + "&params[Department]="
                                + cards_part
                                + "&params[Position]="
                                + cards_job
                                + "&params[CompanyWeb]="
                                + cards_index
                                + "&params[Address]="
                                + cards_address
                                + "&params[Sign]="
                                + cards_signature
                                + "&params[Weixin]="
                                + cards_weixin
                                + "&is_default=1";
                        Intent intent = new Intent( ICardTagCardActivity.this,
                                                    MainActivity.class );
                        startActivity( intent );
                        finish();
                    }
                } else {
                    if ( card_id != null ) {
                        new Thread() {
                            public void run() {
                                String result = HttpTools.userInfo(
                                        IstroopConstants.URL_PATH
                                                + "/ICard/setCard/?cid=" + card_id
                                                + "&params[Name]=" + cards_name
                                                + "&params[Phone]=" + cards_mobile
                                                + "&params[Mail]=" + cards_mail
                                                + "&params[Company]="
                                                + cards_company
                                                + "&params[Department]="
                                                + cards_part + "&params[Position]="
                                                + cards_job
                                                + "&params[CompanyWeb]="
                                                + cards_index + "&params[Address]="
                                                + cards_address + "&params[Sign]="
                                                + cards_signature
                                                + "&params[Weixin]=" + cards_weixin
                                                + "&is_default=1",
                                        IstroopConstants.cookieStore );
                                try {
                                    if ( result != null ) {
                                        JSONObject object = new JSONObject( result );
                                        if ( object.getBoolean( "success" ) ) {
                                            Message message = Message.obtain();
                                            message.what = ICARD_TAG_CARD_SET_SUCCESS;
                                            handler.sendMessage( message );
                                        }
                                    } else {
                                        Message message = Message.obtain();
                                        message.what = ICARD_TAG_CARD_SET_FAIL;
                                        handler.sendMessage( message );
                                    }
                                } catch ( JSONException e ) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    } else {
                        Log.i( TAG, "进入添加页面" );
                        new Thread() {
                            public void run() {
                                // 传入数据库http://api.ttfind.so/ICard/setCard/?cid=1&params[title]=**
                                String result = HttpTools.userInfo(
                                        IstroopConstants.URL_PATH
                                                + "/ICard/setCard/?params[Name]="
                                                + cards_name + "&params[Phone]="
                                                + cards_mobile + "&params[Mail]="
                                                + cards_mail + "&params[Company]="
                                                + cards_company
                                                + "&params[Department]="
                                                + cards_part + "&params[Position]="
                                                + cards_job
                                                + "&params[CompanyWeb]="
                                                + cards_index + "&params[Address]="
                                                + cards_address + "&params[Sign]="
                                                + cards_signature
                                                + "&params[Weixin]=" + cards_weixin
                                                + "&is_default=1",
                                        IstroopConstants.cookieStore );
                                Log.i( TAG, "添加用户信息要服务器返回结果:" + result );
                                try {
                                    if ( result != null ) {
                                        JSONObject object = new JSONObject( result );
                                        if ( object.getBoolean( "success" ) ) {
                                            Message message = Message.obtain();
                                            message.what = ICARD_TAG_CARD_ADD_SUCCESS;
                                            handler.sendMessage( message );
                                        }
                                    } else {
                                        Message message = Message.obtain();
                                        message.what = ICARD_TAG_CARD_ADD_FAIL;
                                        handler.sendMessage( message );
                                    }
                                } catch ( JSONException e ) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                }
                break;
            case R.id.icard_tag_card_preview:
                getText();
                if ( TextUtils.isEmpty( cards_name ) ) {
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.icard_version_name_null ),
                            Toast.LENGTH_SHORT ).show();
                    return;
                }
                if ( TextUtils.isEmpty( cards_mobile ) ) {
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.icard_version_mobile_null ),
                            Toast.LENGTH_SHORT ).show();
                    return;
                }
                if ( Utils.isNotPhoneNumber( cards_mobile ) ) {
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.icard_version_mobile_error ),
                            Toast.LENGTH_SHORT ).show();
                    return;
                }
                if ( !TextUtils.isEmpty( cards_mail ) ) {
                    if ( Utils.isNotEmail( cards_mail ) ) {
                        Toast.makeText(
                                this,
                                getResources().getString(
                                        R.string.icard_version_mail_error ),
                                Toast.LENGTH_SHORT ).show();
                        return;
                    }
                }
                if ( TextUtils.isEmpty( cards_index ) ) {
                    Toast.makeText( this, "公司网址不能为空", Toast.LENGTH_SHORT ).show();
                    return;
                } else {
                    if ( Utils.isNotIndex( cards_index ) ) {
                        Toast.makeText(
                                this,
                                getResources().getString(
                                        R.string.icard_version_website_error ),
                                Toast.LENGTH_SHORT ).show();
                        return;
                    }
                }

                Intent intent = new Intent( ICardTagCardActivity.this,
                                            ICardTagPreview.class );
                Bundle bundle = new Bundle();
                Log.i( TAG, "发送的:" + Arrays.toString( cardInfos ) );
                bundle.putStringArray( "cardInfos", cardInfos );
                intent.putExtras( bundle );
                startActivity( intent );
                break;
            case R.id.icard_card_cancel_rl:
                Log.i( TAG, "点击icard_card_cancel_rl" );
                finish();
                break;
            default:
                break;
        }
    }

    class CardHander extends Handler {
        @Override
        public void handleMessage( Message msg ) {
            super.handleMessage( msg );
            switch ( msg.what ) {
                case ICARD_TAG_CARD_ADD_SUCCESS:
                    Toast.makeText( ICardTagCardActivity.this,
                                    getResources().getString( R.string.save_success ), Toast.LENGTH_SHORT ).show();
                    if ( !TextUtils.isEmpty( cards_type ) ) {
                        BroadcastHelper.sendBroadCast( ICardTagCardActivity.this,
                                                       IstroopConstants.WHERE_PAGE_ACTION,
                                                       IstroopConstants.WHERE_PAGE_KEY, 4 );
                        initCards();
                    } else {
                        // initCards();
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString( "cards_name", cards_name );
                        bundle.putString( "cards_mobile", cards_mobile );
                        bundle.putString( "cards_mail", cards_mail );
                        bundle.putString( "cards_company", cards_company );
                        bundle.putString( "cards_part", cards_part );
                        bundle.putString( "cards_job", cards_job );
                        bundle.putString( "cards_index", cards_index );
                        bundle.putString( "cards_address", cards_address );
                        bundle.putString( "cards_signature", cards_signature );
                        bundle.putString( "cards_weixin", cards_weixin );
                        intent.putExtras( bundle );
                        setResult( RESULT_OK, intent );
                        finish();
                    }
                    break;
                case ICARD_TAG_CARD_ADD_FAIL:
                    Toast.makeText( ICardTagCardActivity.this,
                                    getResources().getString( R.string.save_fail ), Toast.LENGTH_SHORT ).show();
                    break;
                case ICARD_TAG_CARD_SUCCESS:
                    if ( map != null ) {
                        icard_tag_card_name.setText( map.get( "cards_name" ) );
                        icard_tag_card_mobile.setText( map.get( "cards_mobile" ) );
                        icard_tag_card__mail.setText( map.get( "cards_mail" ) );
                        icard_tag_card_conpany.setText( map.get( "cards_company" ) );
                        icard_tag_card_part.setText( map.get( "cards_part" ) );
                        icard_tag_card_job.setText( map.get( "cards_job" ) );
                        icard_tag_card_index.setText( map.get( "cards_index" ) );
                        icard_tag_card_address.setText( map.get( "cards_address" ) );
                        Log.i( TAG, "个性签名:" + map.get( "cards_signature" ) );
                        if ( !TextUtils.isEmpty( map.get( "cards_signature" ) )
                                && map.get( "cards_signature" ) != null
                                && !"null".equals( map.get( "cards_signature" ) ) ) {
                            icard_tag_card_signature.setText( map
                                                                      .get( "cards_signature" ) );
                        } else {
                            icard_tag_card_signature.setText( "" );
                            icard_tag_card_signature.setHint( "" );
                        }
                        if ( !TextUtils.isEmpty( map.get( "cards_weixin" ) )
                                && map.get( "cards_weixin" ) != null ) {
                            icard_tag_card_weixin.setText( map.get( "cards_weixin" ) );
                        } else {
                            icard_tag_card_weixin.setHint( "" );
                            icard_tag_card_weixin.setText( "" );
                        }
                        icard_tag_card_name.setSelection( icard_tag_card_name
                                                                  .getText().toString().trim().length() );
                    }
                    break;
                case ICARD_TAG_CARD_SET_SUCCESS:
                    Toast.makeText( ICardTagCardActivity.this,
                                    getResources().getString( R.string.edit_success ), Toast.LENGTH_SHORT )
                            .show();
                    initCards();
                    finish();
                    break;
                case ICARD_TAG_CARD_SET_FAIL:
                    Toast.makeText( ICardTagCardActivity.this,
                                    getResources().getString( R.string.edit_fail ), Toast.LENGTH_SHORT ).show();
                    break;
                case ICARD_TAG_CARDS_SUCCESS:
                    Log.i( TAG, "更新cards" );
                    if ( ICardTagCardsActivity.adapter != null ) {
                        ICardTagCardsActivity.adapter.notifyDataSetChanged();
                    } else {
                        BroadcastHelper.sendBroadCast( ICardTagCardActivity.this,
                                                       IstroopConstants.WHERE_PAGE_ACTION,
                                                       IstroopConstants.WHERE_PAGE_KEY, 4 );
                    }
                    finish();
                    break;
                case ICARD_TAG_CARDS_FAIL:
                    finish();
                    break;
                default:
                    break;
            }
        }
    }
}
