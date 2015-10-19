package com.istroop.istrooprecognize.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.istroop.istrooprecognize.BaseActivity;
import com.istroop.istrooprecognize.IstroopConstants;
import com.istroop.istrooprecognize.R;
import com.istroop.istrooprecognize.utils.Okhttps;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ICardTagCardsActivity extends BaseActivity implements OnClickListener {

    private static final   String TAG                              = "ICardTagCardsActivity";
    protected static final int    ICARD_TAG_CARDS_SUCCESS          = 4;
    protected static final int    ICARD_TAG_CARDS                  = 5;
    protected static final int    ICARD_TAG_CARDS_SET              = 6;
    protected static final int    ICARD_TAG_CARDS_FAIL             = 7;
    private static final   int    ICARD_TAG_CARDS_DEL_SUCCESS      = 8;
    private static final   int    ICARD_TAG_CARDS_DEL_FAIL         = 9;
    protected static final int    ICARD_TAG_CARDS_SET_SUCCESS      = 10;
    protected static final int    ICARD_TAG_CARDS_SET_FAIL         = 11;
    protected static final int    ICARD_TAG_CARDS_ADD_SUCCESS      = 12;
    protected static final int    ICARD_TAG_CARDS_ADD_FAIL         = 13;
    protected static final int    ICARD_TAG_CARDS_SET_DEFAULT_FAIL = 14;
    private       ListView                           icard_order_addresses_lv;
    public static AddressAdapter                     adapter;
    public static ArrayList<HashMap<String, String>> cardsList;
    private       boolean                            isSelected;
    private       String                             name;
    private       String                             job;

    private String mobile;
    private String mail;
    private String company;
    private String part;
    private String index;
    private String address;
    private String signature;
    private String weixin;
    private String cards_type;
    private int is_default_number = -1;

    private CardsHandler handler = new CardsHandler();

    private Okhttps okhttps;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.icard_order_addresses );
        init();
    }


    public void init() {
        okhttps = Okhttps.getInstance();
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if ( extras != null ) {
            cards_type = extras.getString( "cards_type" );
        }
        isSelected = false;
        TextView icard_order_addresses_cannel = ( TextView ) findViewById( R.id.icard_order_addresses_cannel );
        RelativeLayout icard_order_addresses_save = ( RelativeLayout ) findViewById( R.id.icard_order_addresses_save );
        icard_order_addresses_lv = ( ListView ) findViewById( R.id.icard_order_addresses_lv );
        TextView design_type_name = ( TextView ) findViewById( R.id.design_type_name );
        RelativeLayout icard_cards_rl = ( RelativeLayout ) findViewById( R.id.icard_cards_rl );
        ImageView icard_cards_menu = ( ImageView ) findViewById( R.id.icard_cards_menu );
        design_type_name.setText( getResources().getString( R.string.version_title ) );
        adapter = new AddressAdapter();
        icard_order_addresses_lv.addFooterView( initFootView() );
        icard_order_addresses_cannel.setOnClickListener( this );
        icard_order_addresses_save.setOnClickListener( this );
        icard_cards_menu.setOnClickListener( this );
        if ( cards_type != null ) {
            icard_cards_rl.setVisibility( View.GONE );
            icard_cards_menu.setVisibility( View.VISIBLE );
        } else {
            icard_cards_rl.setVisibility( View.VISIBLE );
            icard_cards_menu.setVisibility( View.GONE );
        }

        if ( !TextUtils.isEmpty( IstroopConstants.card_temp ) ) {
            //进行添加操作
            addVersion();
        } else {
            initCards();
        }
        icard_order_addresses_lv.setOnItemClickListener( ( parent, view, position, id ) -> {
            Log.i( TAG, "条目点击事件的位置:" + position );
            if ( position == cardsList.size() ) {
                Intent intent1 = new Intent( ICardTagCardsActivity.this, ICardTagCardActivity.class );
                if ( cards_type != null ) {
                    Bundle bundle = new Bundle();
                    bundle.putString( "cards_type", cards_type );
                    intent1.putExtras( bundle );
                }
                startActivityForResult( intent1, ICARD_TAG_CARDS );
            } else {
                is_default_number = position;
                if ( adapter.getSelectIndex() == -1 ) {
                    adapter.setSelectIndex( position );
                } else {
                    adapter.setSelectIndex( position );
                }
                adapter.notifyDataSetChanged();
            }
        } );
    }


    private void addVersion() {
        new Thread() {
            public void run() {
                //传入数据库http://api.ttfind.so/ICard/setCard/?cid=1&params[title]=**
                try {
                    String result = okhttps.get( IstroopConstants.URL_PATH + IstroopConstants.card_temp );
//                        HttpTools.userInfo( IstroopConstants.URL_PATH + IstroopConstants.card_temp,
//                                                    IstroopConstants.cookieStore );
                    Log.i( TAG, "添加用户信息要服务器返回结果:" + result );
                    if ( result != null ) {
                        JSONObject object = new JSONObject( result );
                        if ( object.getBoolean( "success" ) ) {
                            Message message = Message.obtain();
                            message.what = ICARD_TAG_CARDS_ADD_SUCCESS;
                            handler.sendMessage( message );
                        }
                    } else {
                        Message message = Message.obtain();
                        message.what = ICARD_TAG_CARDS_ADD_FAIL;
                        handler.sendMessage( message );
                    }
                } catch ( JSONException | IOException e ) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult( requestCode, resultCode, data );
        switch ( requestCode ) {
            case ICARD_TAG_CARDS:
                initCards();
                break;

            default:
                break;
        }
    }

    private void initCards() {
        new Thread() {
            public void run() {
                Log.i( TAG, "进入子线程" );
                /*cardsList = helper.queryALL();*/
                try {
                    String cards = okhttps.get( IstroopConstants.URL_PATH + "/ICard/MyCard/" );
//                        HttpTools.userInfo( IstroopConstants.URL_PATH + "/ICard/MyCard/", IstroopConstants.cookieStore );
                    Log.i( TAG, "获取用户的名片信息:" + cards );
                    if ( cards != null ) {
                        cardsList = new ArrayList<>();
                        JSONObject object = new JSONObject( cards );
                        if ( object.getBoolean( "success" ) ) {
                            JSONArray jsonArray = object.getJSONArray( "data" );
                            HashMap<String, String> map;
                            //{"success":true,"data":[]}
                            if ( jsonArray.length() != 0 ) {
                                for ( int i = 0; i < jsonArray.length(); i++ ) {
                                    map = new HashMap<>();
                                    String cards_id = jsonArray.getJSONObject( i ).getString( "cid" );
                                    String is_default = jsonArray.getJSONObject( i ).getString( "is_default" );
                                    String time_create = jsonArray.getJSONObject( i ).getString( "time_create" );

                                    JSONObject params = jsonArray.getJSONObject( i ).getJSONObject( "params" );
                                    String cards_name = params.getString( "Name" );
                                    String cards_mobile = params.getString( "Phone" );
                                    String cards_mail = params.getString( "Mail" );
                                    String cards_company;
                                    if ( params.isNull( "Company" ) ) {
                                        cards_company = "";
                                    } else {
                                        cards_company = params.getString( "Company" );
                                    }
                                    String cards_part = params.getString( "Department" );
                                    String cards_job = params.getString( "Position" );
                                    String cards_index = params.getString( "CompanyWeb" );
                                    String cards_address = params.getString( "Address" );
                                    String cards_signature = params.getString( "Sign" );
                                    String cards_weixin = params.getString( "Weixin" );
                                    map.put( "cards_id", cards_id );
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
                                    map.put( "is_default", is_default );
                                    map.put( "time_create", time_create );
                                    cardsList.add( map );
                                }
                                Message message = Message.obtain();
                                message.what = ICARD_TAG_CARDS_SUCCESS;
                                handler.sendMessage( message );
                            } else {
                                Message message = Message.obtain();
                                message.what = ICARD_TAG_CARDS_FAIL;
                                handler.sendMessage( message );
                            }
                        } else {
                            Message message = Message.obtain();
                            message.what = ICARD_TAG_CARDS_FAIL;
                            handler.sendMessage( message );
                        }
                    }
                } catch ( JSONException | IOException e ) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private View initFootView() {
        View view = View.inflate( ICardTagCardsActivity.this, R.layout.icard_tag_map_item, null );
        TextView tv = ( TextView ) view.findViewById( R.id.icard_tag_map_item );
        tv.setText( getResources().getString( R.string.version_new ) );
        tv.setGravity( Gravity.CENTER );
        tv.setTextColor( getResources().getColor( R.color.orange ) );
        return view;
    }

    @Override
    public void onClick( View v ) {
        switch ( v.getId() ) {
            case R.id.icard_order_addresses_cannel:
                Intent intent1 = new Intent();
                setResult( RESULT_OK, intent1 );
                finish();
                break;
            case R.id.icard_order_addresses_save:
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString( "cards_name", name );
                bundle.putString( "cards_mobile", mobile );
                bundle.putString( "cards_mail", mail );
                bundle.putString( "cards_company", company );
                bundle.putString( "cards_part", part );
                bundle.putString( "cards_job", job );
                bundle.putString( "cards_index", index );
                bundle.putString( "cards_address", address );
                bundle.putString( "cards_signature", signature );
                bundle.putString( "cards_weixin", weixin );
                intent.putExtras( bundle );
                setResult( RESULT_OK, intent );
                //设置默认名片
                setDefault();
                //finish();
                break;
            case R.id.icard_cards_menu:
                if ( IstroopConstants.sm != null ) {
                    IstroopConstants.sm.toggle();
                }
                break;
            default:
                break;
        }

    }


    private void setDefault() {
        new Thread() {
            public void run() {
                if ( is_default_number == -1 ) {
                    Message message = Message.obtain();
                    message.what = ICARD_TAG_CARDS_SET_DEFAULT_FAIL;
                    handler.sendMessage( message );
                    return;
                }
/*				"&params[Name]="+name.trim()
                +"&params[Phone]="+mobile+"&params[Mail]="+mail
				+"&params[Company]="+company+"&params[Department]="+part
				+"&params[Position]="+job+"&params[CompanyWeb]="+index
				+"&params[Address]="+address+"&params[Sign]="+signature
				+"&params[Weixin]="+weixin+*/
                try {
                    String result = okhttps.get( IstroopConstants.URL_PATH + "/ICard/setCard/?cid=" );
//                        HttpTools.userInfo( IstroopConstants.URL_PATH + "/ICard/setCard/?cid=" + cardsList.get( is_default_number ).get( "cards_id" ) + "&is_default=1", IstroopConstants.cookieStore );
                    Log.i( TAG, "设置默认返回的信息:" + result );
                    if ( result != null ) {
                        JSONObject object = new JSONObject( result );
                        if ( object.getBoolean( "success" ) ) {
                            Message message = Message.obtain();
                            message.what = ICARD_TAG_CARDS_SET_SUCCESS;
                            handler.sendMessage( message );
                        }
                    } else {
                        Message message = Message.obtain();
                        message.what = ICARD_TAG_CARDS_SET_FAIL;
                        handler.sendMessage( message );
                    }
                } catch ( JSONException | IOException e ) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    public class AddressAdapter extends BaseAdapter {

        private int selectIndex = is_default_number;

        public int getSelectIndex() {
            return selectIndex;
        }

        public void setSelectIndex( int selectIndex ) {
            this.selectIndex = selectIndex;
        }

        @Override
        public int getCount() {
            if ( cardsList == null || cardsList.size() == 0 ) {
                return 0;
            } else {
                return cardsList.size();
            }
        }

        @Override
        public Object getItem( int position ) {
            return cardsList.get( position );
        }

        @Override
        public long getItemId( int position ) {
            return position;
        }

        @Override
        public View getView( final int position, View convertView, ViewGroup parent ) {
            final ViewHolder holder;
            if ( convertView == null ) {
                holder = new ViewHolder();
                convertView = View.inflate( ICardTagCardsActivity.this, R.layout.icard_order_addresses_item, null );
                holder.iv = ( ImageView ) convertView.findViewById( R.id.icard_order_addresses_item_iv );
                holder.iv2 = ( ImageView ) convertView.findViewById( R.id.icard_order_address_item_iv2 );
                holder.tv1 = ( TextView ) convertView.findViewById( R.id.icard_order_addresses_item_tv1 );
                holder.tv2 = ( TextView ) convertView.findViewById( R.id.icard_order_addresses_item_tv2 );
                holder.delete = ( Button ) convertView.findViewById( R.id.icard_order_address_item_delete );
                holder.icard_order_address_item_right = ( RelativeLayout ) convertView.findViewById( R.id.icard_order_address_item_right );
                convertView.setTag( holder );
            } else {
                holder = ( ViewHolder ) convertView.getTag();
            }
            LayoutParams lp2 = new LayoutParams( 200, LayoutParams.MATCH_PARENT );
            holder.icard_order_address_item_right.setLayoutParams( lp2 );
            if ( getSelectIndex() == position ) {
                holder.iv.setVisibility( View.VISIBLE );
                holder.iv.setBackgroundResource( R.drawable.card_select );
                name = cardsList.get( position ).get( "cards_name" );
                mobile = cardsList.get( position ).get( "cards_mobile" );
                mail = cardsList.get( position ).get( "cards_mail" );
                company = cardsList.get( position ).get( "cards_company" );
                part = cardsList.get( position ).get( "cards_part" );
                job = cardsList.get( position ).get( "cards_job" );
                index = cardsList.get( position ).get( "cards_index" );
                address = cardsList.get( position ).get( "cards_address" );
                signature = cardsList.get( position ).get( "cards_signature" );
                weixin = cardsList.get( position ).get( "cards_weixin" );
                holder.iv2.setOnClickListener( v -> {
                    Log.i( TAG, "修改地址信息的点击事件:" + isSelected );
                    Intent intent = new Intent( ICardTagCardsActivity.this, ICardTagCardActivity.class );
                    Bundle bundle = new Bundle();
                    bundle.putString( "card_id", cardsList.get( position ).get( "cards_id" ) );
                    intent.putExtras( bundle );
                    startActivityForResult( intent, ICARD_TAG_CARDS_SET );
                } );

            } else {
                holder.iv.setVisibility( View.INVISIBLE );
            }

            if ( is_default_number == position ) {
                holder.iv.setVisibility( View.VISIBLE );
                //is_default_number = -1;
            }
            holder.icard_order_address_item_right.setOnClickListener( v -> {
                selectIndex = -1;
                v.setVisibility( View.INVISIBLE );
                removeFromListView( position );
            } );

			/*(String cards_name, String cards_mobile,String cards_mail,
            String cards_company,String cards_part,String cards_job,
			String cards_index,String cards_address,String cards_signature,String cards_weixin )*/
            if ( TextUtils.isEmpty( cardsList.get( position ).get( "cards_job" ) ) || "null".equals( cardsList.get( position ).get( "cards_job" ) ) ) {
                holder.tv1.setText( cardsList.get( position ).get( "cards_name" ) );
            } else {
                holder.tv1.setText( cardsList.get( position ).get( "cards_name" )
                                            + " | " + cardsList.get( position ).get( "cards_job" ) );
            }
            holder.tv2.setText( cardsList.get( position ).get( "cards_company" ) + "  " + cardsList.get( position ).get( "cards_part" )
                                        + "\n" + cardsList.get( position ).get( "cards_mobile" ) + "  " + cardsList.get( position ).get( "cards_mail" ) );
            return convertView;
        }

    }


    private void removeFromListView( int position ) {
        if ( position < cardsList.size() ) {
            HashMap<String, String> picMap = cardsList.get( position );
            final String cards_id = picMap.get( "cards_id" );
            new Thread() {
                public void run() {
                    try {
                        String delresult = okhttps.get( IstroopConstants.URL_PATH + "/ICard/delCard/?cid=" + cards_id );
//                            HttpTools.userInfo( IstroopConstants.URL_PATH + "/ICard/delCard/?cid=" + cards_id,
//                                                           IstroopConstants.cookieStore );
                        if ( delresult != null ) {
                            JSONObject jsonObject = new JSONObject( delresult );
                            if ( jsonObject.getBoolean( "success" ) ) {
                                Log.i( TAG, "删除成功" );
                                Message message = Message.obtain();
                                message.what = ICARD_TAG_CARDS_DEL_SUCCESS;
                                handler.sendMessage( message );
                            }
                        } else {
                            Message message = Message.obtain();
                            message.what = ICARD_TAG_CARDS_DEL_FAIL;
                            handler.sendMessage( message );
                        }
                    } catch ( JSONException e ) {
                        e.printStackTrace();
                        Message message = Message.obtain();
                        message.what = ICARD_TAG_CARDS_DEL_FAIL;
                        handler.sendMessage( message );
                    } catch ( IOException e ) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } else {
            Toast.makeText( ICardTagCardsActivity.this, "请稍后重试", Toast.LENGTH_SHORT ).show();
        }
    }

    public class ViewHolder {
        ImageView      iv;
        ImageView      iv2;
        TextView       tv1;
        TextView       tv2;
        Button         delete;
        RelativeLayout icard_order_address_item_right;
    }

    class CardsHandler extends Handler {
        @Override
        public void handleMessage( Message msg ) {
            super.handleMessage( msg );
            switch ( msg.what ) {
                case ICARD_TAG_CARDS_SUCCESS:
                    for ( int i = 0; i < cardsList.size(); i++ ) {
                        if ( "1".equals( cardsList.get( i ).get( "is_default" ) ) ) {
                            is_default_number = i;
                            name = cardsList.get( i ).get( "cards_name" );
                            mobile = cardsList.get( i ).get( "cards_mobile" );
                            mail = cardsList.get( i ).get( "cards_mail" );
                            company = cardsList.get( i ).get( "cards_company" );
                            part = cardsList.get( i ).get( "cards_part" );
                            job = cardsList.get( i ).get( "cards_job" );
                            index = cardsList.get( i ).get( "cards_index" );
                            address = cardsList.get( i ).get( "cards_address" );
                            signature = cardsList.get( i ).get( "cards_signature" );
                            weixin = cardsList.get( i ).get( "cards_weixin" );
                        }
                        //按时间顺序排序
                        for ( int j = 1; j < cardsList.size() - i; j++ ) {
                            HashMap<String, String> objects;
                            if ( Long.parseLong( cardsList.get( j - 1 ).get( "time_create" ) ) < Long.parseLong( cardsList.get( j ).get( "time_create" ) ) ) {
                                objects = cardsList.get( j - 1 );
                                cardsList.set( j - 1, cardsList.get( j ) );
                                cardsList.set( j, objects );
                            }
                        }
                    }
                    icard_order_addresses_lv.setAdapter( adapter );
                    break;
                case ICARD_TAG_CARDS_FAIL:
                    icard_order_addresses_lv.setAdapter( adapter );
                    break;
                case ICARD_TAG_CARDS_DEL_SUCCESS:
                    Log.i( TAG, "删除后cards" );
                    is_default_number = -1;
                    initCards();
                    break;
                case ICARD_TAG_CARDS_DEL_FAIL:
                    Toast.makeText( ICardTagCardsActivity.this, getResources().getString( R.string.icard_chaojitupian_delete_success ), Toast.LENGTH_SHORT ).show();
                    break;
                case ICARD_TAG_CARDS_SET_SUCCESS:
                    Toast.makeText( ICardTagCardsActivity.this, getResources().getString( R.string.set_success ), Toast.LENGTH_SHORT ).show();
                    finish();
                    break;
                case ICARD_TAG_CARDS_SET_FAIL:
                    Toast.makeText( ICardTagCardsActivity.this, getResources().getString( R.string.set_fail ), Toast.LENGTH_SHORT ).show();
                    break;
                case ICARD_TAG_CARDS_ADD_SUCCESS:
                    IstroopConstants.card_temp = "";
                    initCards();
                    break;
                case ICARD_TAG_CARDS_ADD_FAIL:
                    icard_order_addresses_lv.setAdapter( adapter );
                    break;
                case ICARD_TAG_CARDS_SET_DEFAULT_FAIL:
                    Toast.makeText( ICardTagCardsActivity.this, getResources().getString( R.string.icard_select_tag_set ), Toast.LENGTH_SHORT ).show();
                    break;
                default:
                    break;
            }
        }
    }
}
