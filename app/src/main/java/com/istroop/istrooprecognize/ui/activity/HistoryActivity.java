package com.istroop.istrooprecognize.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.istroop.istrooprecognize.BaseActivity;
import com.istroop.istrooprecognize.IstroopConstants;
import com.istroop.istrooprecognize.MyApplication;
import com.istroop.istrooprecognize.R;
import com.istroop.istrooprecognize.utils.BitmapUtil;
import com.istroop.istrooprecognize.utils.HisDBHelper;
import com.istroop.istrooprecognize.utils.ImageAsyncTask;
import com.istroop.istrooprecognize.utils.Okhttps;
import com.istroop.istrooprecognize.utils.OnTabActivityResultListener;
import com.istroop.istrooprecognize.utils.UploadClient;
import com.istroop.istrooprecognize.utils.UploadClientPic;
import com.istroop.istrooprecognize.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class HistoryActivity extends BaseActivity implements OnClickListener,
        OnTabActivityResultListener {

    private static final   String TAG                = "HistoryActivity";
    protected static final int    HIS_INFO_BG        = 4;
    protected static final int    HIS_SUCCESS        = 5;
    protected static final int    HIS_FAIL           = 6;
    protected static final int    HIS_DELETE_SUCCESS = 7;
    private static final   int    ICARD_HOME_TOP     = 11;
    public static  HisAdapter                         adapter;
    public         TextView                           title;
    public static  ArrayList<HashMap<String, Object>> arrayList; // 存储ListItem的集合
    public static  ListView                           listView;
    private        ImageView                          his_menu;
    private        PopupWindow                        mPopupWindow;
    private static ArrayList<HashMap<String, Object>> pop_list;
    private static TextView                           his_error;
    private static boolean                            isExit;
    private        Okhttps                            okhttps;

    public HistoryActivity() {
        super();
    }

    private HistoryHandler handler = new HistoryHandler();

    private String[] types;

    @Override
    protected void onResume() {
        super.onResume();

        if ( IstroopConstants.isLogin ) {
            initHis();
        } else {
            HisDBHelper dbHelper = new HisDBHelper( this );
            arrayList = dbHelper.queryALL();
            Log.i( TAG, "history:" + arrayList );
            if ( arrayList != null ) {
                if ( arrayList.size() != 0 ) {
                    listView.setVisibility( View.VISIBLE );
                    his_error.setVisibility( View.INVISIBLE );
                    pop_list = arrayList;
                    adapter = new HisAdapter();
                    listView.setAdapter( adapter );
                } else {
                    listView.setVisibility( View.INVISIBLE );
                    his_error.setVisibility( View.VISIBLE );
                }
            } else {
                listView.setVisibility( View.INVISIBLE );
                his_error.setVisibility( View.VISIBLE );
            }
        }
    }

    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.his );

        okhttps = Okhttps.getInstance();
        title = ( TextView ) findViewById( R.id.titleText );
        his_error = ( TextView ) findViewById( R.id.his_error );
        his_menu = ( ImageView ) findViewById( R.id.his_menu );
        types = new String[] {
                getResources().getString( R.string.version_title ),
                getResources().getString( R.string.link_title ),
                getResources().getString( R.string.text_title ),
                getResources().getString( R.string.map_title ),
                getResources().getString( R.string.video_title ),
                getResources().getString( R.string.pic_title ),
                getResources().getString( R.string.person_title ) };

        pop_list = new ArrayList<>();
        PopAdapter popAdapter = new PopAdapter();

        View popupView = getLayoutInflater().inflate(
                R.layout.icard_order_number_sale, null );

        ListView his_pop_lv = ( ListView ) popupView
                .findViewById( R.id.his_pop_lv );
        Button logout = new Button( this );
        AbsListView.LayoutParams params = new AbsListView.LayoutParams( AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT );
        logout.setLayoutParams( params );
        logout.setText( "退出登录" );
        logout.setTextColor( getResources().getColor( R.color.white ) );
        logout.setTextSize( 16.0f );
        logout.setBackgroundColor( getResources().getColor( R.color.orange ) );
        logout.setOnClickListener( v -> {
            IstroopConstants.isLogin = false;
            IstroopConstants.isMobile = false;
            deleteLoginInfo( getApplicationContext() );
            MainActivity.tabHost.setCurrentTab( 0 );
            mPopupWindow.dismiss();
            mPopupWindow = null;
        } );
        his_pop_lv.addFooterView( logout );
        his_pop_lv.setAdapter( popAdapter );

        his_pop_lv.setOnItemClickListener( ( parent, view, position, id ) -> {

            ViewHolder holder = ( ViewHolder ) view.getTag();
            holder.icon
                    .setImageResource( IstroopConstants.imageResIDPresseds[position] );
            if ( position == 0 ) {
                // card 3
                arrayList = new ArrayList<>();
                for ( int i = 0; i < pop_list.size(); i++ ) {
                    int pop_type = ( Integer ) pop_list.get( i ).get(
                            "his_tag_type" );
                    if ( pop_type == 3 ) {
                        arrayList.add( pop_list.get( i ) );
                    }
                }
            } else if ( position == 1 ) {
                // link 1
                arrayList = new ArrayList<>();
                for ( int i = 0; i < pop_list.size(); i++ ) {
                    int pop_type = ( Integer ) pop_list.get( i ).get(
                            "his_tag_type" );
                    if ( pop_type == 1 ) {
                        arrayList.add( pop_list.get( i ) );
                    }
                }
            } else if ( position == 2 ) {
                // text 0
                arrayList = new ArrayList<>();
                for ( int i = 0; i < pop_list.size(); i++ ) {
                    int pop_type = ( Integer ) pop_list.get( i ).get(
                            "his_tag_type" );
                    if ( pop_type == 0 ) {
                        arrayList.add( pop_list.get( i ) );
                    }
                }
            } else if ( position == 3 ) {
                // map 7
                arrayList = new ArrayList<>();
                for ( int i = 0; i < pop_list.size(); i++ ) {
                    int pop_type = ( Integer ) pop_list.get( i ).get(
                            "his_tag_type" );
                    if ( pop_type == 7 ) {
                        arrayList.add( pop_list.get( i ) );
                    }
                }
            } else if ( position == 4 ) {
                // video 6
                arrayList = new ArrayList<>();
                for ( int i = 0; i < pop_list.size(); i++ ) {
                    int pop_type = ( Integer ) pop_list.get( i ).get(
                            "his_tag_type" );
                    if ( pop_type == 6 ) {
                        arrayList.add( pop_list.get( i ) );
                    }
                }
            } else if ( position == 5 ) {
                // pic 4
                arrayList = new ArrayList<>();
                for ( int i = 0; i < pop_list.size(); i++ ) {
                    int pop_type = ( Integer ) pop_list.get( i ).get(
                            "his_tag_type" );
                    if ( pop_type == 4 ) {
                        arrayList.add( pop_list.get( i ) );
                    }
                }
            } else if ( position == 6 ) {
                // person 5
                arrayList = new ArrayList<>();
                for ( int i = 0; i < pop_list.size(); i++ ) {
                    int pop_type = ( Integer ) pop_list.get( i ).get(
                            "his_tag_type" );
                    if ( pop_type == 5 ) {
                        arrayList.add( pop_list.get( i ) );
                    }
                }
            }

            Log.i( TAG, "poplist:" + pop_list.size() );
            Log.i( TAG, "pop后的list:" + arrayList.size() );
            if ( arrayList != null && arrayList.size() != 0 ) {
                listView.setVisibility( View.VISIBLE );
                his_error.setVisibility( View.INVISIBLE );
                adapter.notifyDataSetChanged();
            } else {
                listView.setVisibility( View.INVISIBLE );
                his_error.setVisibility( View.VISIBLE );
            }
            mPopupWindow.dismiss();
        } );
        mPopupWindow = new PopupWindow( popupView, ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT, true );
        mPopupWindow.setTouchable( true );
        mPopupWindow.setOutsideTouchable( true );
        mPopupWindow.setBackgroundDrawable( new BitmapDrawable( getResources(),
                                                                ( Bitmap ) null ) );
        listView = ( ListView ) findViewById( R.id.listView );
        his_menu.setOnClickListener( this );
        listView.setCacheColorHint( Color.TRANSPARENT );
        listView.setAlwaysDrawnWithCacheEnabled( true );
        listView.setOnScrollListener( new OnScrollListener() {

            @Override
            public void onScrollStateChanged( AbsListView view, int scrollState ) {
                switch ( scrollState ) {
                    case SCROLL_STATE_IDLE:
                        break;
                    case SCROLL_STATE_TOUCH_SCROLL:
                        break;
                    case SCROLL_STATE_FLING:
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onScroll( AbsListView view, int firstVisibleItem,
                                  int visibleItemCount, int totalItemCount ) {
            }
        } );

        listView.setOnItemClickListener( ( list, view, position, id ) -> {
            listView.setSelector( R.drawable.list_bg );
            HashMap<String, Object> picMap = arrayList.get( position );
            String pic = ( String ) picMap.get( "his_fileurl" );
            int type = ( Integer ) picMap.get( "his_tag_type" );
            String picUri = ( String ) picMap.get( "his_tag_url" );
            String titlesString = ( String ) picMap.get( "his_tag_title" );
            String descString = ( String ) picMap.get( "his_tag_desc" );
            if ( type == 1 || type == 2 || type == 5 || type == 6 ) {
                Intent webIntent = new Intent( HistoryActivity.this,
                                               RecoDetailWebActivity.class );
                webIntent.putExtra( "picUrl", picUri );
                startActivity( webIntent );
            } else if ( type == 0 ) {
                Intent textIntent = new Intent( HistoryActivity.this,
                                                RecoDetailTextActivity.class );
                textIntent.putExtra( "DB_tag_title", titlesString );
                textIntent.putExtra( "DB_tag_desc", descString );
                startActivity( textIntent );

            } else if ( type == 4 ) {
                Intent piciIntent = new Intent( HistoryActivity.this,
                                                RecoDetailPicActivity.class );
                piciIntent.putExtra( "DB_tag_title", titlesString );
                piciIntent.putExtra( "DB_tag_url", picUri );
                startActivity( piciIntent );

            } else if ( type == 7 ) {
                Intent mapIntent = new Intent( HistoryActivity.this,
                                               RecoDetailMapActivity.class );
                mapIntent.putExtra( "DB_tag_url", picUri );
                mapIntent.putExtra( "DB_tag_title", titlesString );
                startActivity( mapIntent );

            } else if ( type == 3 ) {
                titlesString = titlesString + "==" + picUri;
                String[] split = titlesString.split( "==" );
                if ( !TextUtils.isEmpty( descString )
                        && !"null".equals( descString ) ) {
                    if ( "copy".equals( descString ) ) {
                        Intent editionIntent = new Intent(
                                HistoryActivity.this,
                                RecoDetailEditionActivity.class );
                        editionIntent.putExtra( "DB_tag_url", titlesString );
                        editionIntent.putExtra( "DB_tag_title", picUri );
                        startActivity( editionIntent );
                    } else {
                        Intent editionIntent = new Intent(
                                HistoryActivity.this,
                                ICardTagPreview.class );
                        Bundle bundle = new Bundle();
                        bundle.putStringArray( "cardInfos", split );
                        bundle.putString( "headurl", pic );
                        editionIntent.putExtras( bundle );
                        startActivity( editionIntent );
                    }
                } else {
                    Intent editionIntent = new Intent(
                            HistoryActivity.this, ICardTagPreview.class );
                    Bundle bundle = new Bundle();
                    bundle.putStringArray( "cardInfos", split );
                    bundle.putString( "card_type", "reco" );
                    editionIntent.putExtras( bundle );
                    /*
                     * editionIntent.putExtra("DB_tag_url", picUri);
                     * editionIntent.putExtra("DB_tag_title", titlesString);
                     */
                    startActivity( editionIntent );
                }
            }
        } );
    }

    /**
     * 初始化：主要做数据请求
     */
    private void initHis() {
        new Thread() {
            public void run() {
                String histories = null;

                try {
                    histories = okhttps.get( IstroopConstants.URL_PATH + "/ICard/MyHistory/" );
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
                // TODO
                Log.i( TAG, "历史记录:" + histories );
                if ( histories != null ) {
                    if ( "联网失败".equals( histories ) ) {
                        Message message = Message.obtain();
                        message.what = HIS_FAIL;
                        handler.sendMessage( message );
                        return;
                    }
                    try {
                        arrayList = new ArrayList<>();
                        JSONObject jsonObject = new JSONObject( histories );
                        if ( jsonObject.getBoolean( "success" ) ) {
                            JSONArray jsonArray = jsonObject
                                    .getJSONArray( "data" );
                            HashMap<String, Object> historyItemMap;
                            for ( int i = 0; i < jsonArray.length(); i++ ) {
                                // 为什么不用bean呢
                                historyItemMap = new HashMap<>();

                                String pid = jsonArray.getJSONObject( i )
                                        .getString( "pid" );

                                JSONObject params = jsonArray.getJSONObject( i )
                                        .getJSONObject( "params" );

                                String his_wm_id = params.getString( "wmid" );

                                int his_tag_type = params.getInt( "Type" );

                                String his_tag_title = params
                                        .getString( "Title" );

                                long his_mtime = params.getLong( "Createtime" );

                                String his_tag_desc = params.getString( "Desc" );

                                String his_tag_url;

                                if ( params.isNull( "Link" ) ) {
                                    his_tag_url = "";
                                } else {
                                    his_tag_url = params.getString( "Link" );
                                }

                                String his_location = params
                                        .getString( "Address" );

                                String his_fileurl = params
                                        .getString( "PicUrl" );

                                Log.i( TAG, "从网络获取的list的长度:" + his_fileurl );
                                // 受不了了，更改map
                                historyItemMap.put( "pid", pid );
                                historyItemMap.put( "his_wm_id", his_wm_id );
                                historyItemMap.put( "his_fileurl", his_fileurl );
                                historyItemMap.put( "his_tag_type",
                                                    his_tag_type );
                                historyItemMap.put( "his_tag_title",
                                                    his_tag_title );
                                historyItemMap.put( "his_tag_url", his_tag_url );
                                historyItemMap.put( "his_tag_desc",
                                                    his_tag_desc );
                                historyItemMap.put( "his_mtime", his_mtime );

                                historyItemMap.put( "his_location",
                                                    his_location );

                                arrayList.add( historyItemMap );
                            }
                            Log.i( TAG, "从网络获取的list的长度:" + arrayList.size() );
                            HisDBHelper dbHelper = new HisDBHelper(
                                    HistoryActivity.this );
                            ArrayList<HashMap<String, Object>> all = dbHelper
                                    .queryALL();
                            for ( int i = 0; i < all.size(); i++ ) {
                                arrayList.add( all.get( i ) );
                            }
                            Log.i( TAG, "全部的list的长度:" + arrayList.size() );
                            Message message = Message.obtain();
                            message.what = HIS_SUCCESS;
                            handler.sendMessage( message );
                        } else {
                            Message message = Message.obtain();
                            message.what = HIS_FAIL;
                            handler.sendMessage( message );
                        }
                    } catch ( JSONException e ) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    // TODO 这是给那个做的？
    private void loadImage( ImageView my_info_head_exp, String picUrl ) {
        ImageAsyncTask task = new ImageAsyncTask( this,
                                                  IstroopConstants.mLruCache );
        Bitmap bitmap = task.getBitmapFromMemoryCache( picUrl );
        Bitmap cache = task.getBitmapFileCache( picUrl );
        if ( bitmap != null ) {
            my_info_head_exp.setImageBitmap( BitmapUtil
                                                     .getCircleBitmap( bitmap ) );
        } else if ( cache != null ) {
            my_info_head_exp
                    .setImageBitmap( BitmapUtil.getCircleBitmap( cache ) );
        } else {
            Bitmap circleBitmap = BitmapUtil.getCircleBitmap( BitmapFactory
                                                                      .decodeResource( getResources(), R.drawable.default_head ) );
            my_info_head_exp.setImageBitmap( circleBitmap );
            task.execute( picUrl );
        }
    }

    private void loadImage2( ImageView my_info_head_exp, String picUrl ) {
        ImageAsyncTask task = new ImageAsyncTask( this,
                                                  IstroopConstants.mLruCache );
        Bitmap bitmap = task.getBitmapFromMemoryCache( picUrl );
        Bitmap cache = task.getBitmapFileCache( picUrl );
        if ( bitmap != null ) {
            my_info_head_exp.setImageBitmap( bitmap );
        } else if ( cache != null ) {
            my_info_head_exp.setImageBitmap( cache );
        } else {
            my_info_head_exp.setImageResource( R.drawable.home_top );
            task.execute( picUrl );
        }
    }

    @Override
    public void onClick( View v ) {
        switch ( v.getId() ) {
            case R.id.his_menu:
                mPopupWindow.showAsDropDown( his_menu, 0, 10 );
                break;
        }
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode,
                                     Intent data ) {
        super.onActivityResult( requestCode, resultCode, data );
        switch ( requestCode ) {
            case ICARD_HOME_TOP:
                if ( data != null ) {
                    Bundle bundle = data.getExtras();
                    if ( bundle != null ) {
                        UploadClient.upload(
                                Environment.getExternalStorageDirectory().getPath() + "/istroop/ichaotu_upload.jpg",
                                msg -> {
                                    Log.i( TAG, "上传图片返回的信息:" + msg );
                                    try {
                                        JSONObject object = new JSONObject( msg );
                                        if ( object.getBoolean( "success" ) ) {
                                            JSONObject dataInfo = object
                                                    .getJSONObject( "data" );
                                            JSONObject headInfo = dataInfo
                                                    .getJSONObject( "0" );
                                            String fileid = headInfo
                                                    .getString( "fileid" );
                                            String set_sign = okhttps.get(
                                                    IstroopConstants.URL_PATH
                                                            + "/Mobile/setinfo?bg_img="
                                                            + fileid );
                                            JSONObject jsonObject = new JSONObject(
                                                    set_sign );
                                            String data1 = null;
                                            if ( jsonObject
                                                    .getBoolean( "success" ) ) {
                                                data1 = jsonObject
                                                        .getString( "data" );
                                                Log.i( TAG, "成功修改后返回的信息"
                                                        + data1 );
                                            } else {
                                                data1 = jsonObject
                                                        .getString( "data" );
                                            }
                                            Message message = Message.obtain();
                                            message.what = HIS_INFO_BG;
                                            message.obj = data1;
                                            handler.sendMessage( message );
                                        }
                                    } catch ( JSONException | IOException e ) {
                                        e.printStackTrace();
                                    }
                                } );
                    }
                }
                break;
            default:
                break;
        }
    }

    public class HisAdapter extends BaseAdapter {

        private static final String TAG = "HisAdapter";

        private int selectPosition = -1;

        @Override
        public int getCount() {
            if ( arrayList == null || arrayList.size() == 0 ) {
                return 0;
            } else {
                return arrayList.size();
            }
        }

        @Override
        public Object getItem( int index ) {

            return arrayList.get( index );
        }

        @Override
        public long getItemId( int index ) {

            return index;
        }

        @Override
        public View getView( final int index, View convertView, ViewGroup parent ) {

            // 将图片信息存入了一个HashMap
            HashMap<String, Object> picMap = arrayList.get( index );
            ViewHolder holder; // 该容器用来存放控件对象

            if ( convertView == null ) {
                convertView = View.inflate( HistoryActivity.this,
                                            R.layout.hisitem, null );
                holder = new ViewHolder();
                holder.typeImage = ( ImageView ) convertView
                        .findViewById( R.id.picType );
                holder.title = ( TextView ) convertView
                        .findViewById( R.id.picTitle );
                holder.createTime = ( TextView ) convertView
                        .findViewById( R.id.picCreatetime );
                holder.desc = ( TextView ) convertView
                        .findViewById( R.id.picDesc );
                holder.address = ( TextView ) convertView
                        .findViewById( R.id.picAddress );
                holder.picImage = ( ImageView ) convertView
                        .findViewById( R.id.picImage );
                holder.deleteBtn = ( Button ) convertView
                        .findViewById( R.id.pic_delete );
                holder.his_item_right = ( RelativeLayout ) convertView
                        .findViewById( R.id.his_item_right );
                convertView.setTag( holder ); // 将存放对象的容器存进视图对象中

            } else {

                holder = ( ViewHolder ) convertView.getTag(); // 直接拿出控件对象
            }

            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams( 200,
                                                                           ViewGroup.LayoutParams.MATCH_PARENT );

            holder.his_item_right.setLayoutParams( lp2 );
            int type = ( Integer ) picMap.get( "his_tag_type" );
            int imageId = getResources().getIdentifier( "type" + type,
                                                        "drawable", getPackageName() );

            if ( index == selectPosition ) {
                holder.deleteBtn.setVisibility( View.VISIBLE );
            } else {
                holder.deleteBtn.setVisibility( View.INVISIBLE );
            }
            holder.his_item_right.setOnClickListener( v -> {
                selectPosition = -1;
                // v.setVisibility(View.INVISIBLE);
                removeFromListView( index );
            } );
            holder.typeImage.setImageResource( imageId );

            if ( TextUtils.isEmpty( ( String ) picMap.get( "his_tag_title" ) ) ) {
                holder.title.setText( ( String ) picMap.get( "his_tag_desc" ) );
            }
            holder.title.setText( ( String ) picMap.get( "his_tag_title" ) );

            long time = ( Long ) picMap.get( "his_mtime" );
            if ( ( time + "" ).length() == 10 ) {
                String time_temp = time + "000";
                Log.i( TAG, "his+处理后的时间:" + time_temp );
                time = Long.parseLong( time_temp );
            }
            Date date = new Date( time );
            SimpleDateFormat format = new SimpleDateFormat( "MM月dd日 HH:mm" );
            holder.createTime.setText( format.format( date ) );

            if ( TextUtils.isEmpty( ( String ) picMap.get( "his_tag_desc" ) )
                    || "null".equals( picMap.get( "his_tag_desc" ) ) ) {
                holder.title.setText( ( String ) picMap.get( "his_tag_title" ) );
            }
            Log.i( TAG, "" + picMap.get( "his_tag_desc" ) );
            if ( !TextUtils.isEmpty( ( String ) picMap.get( "his_tag_desc" ) )
                    && !"null".equals( picMap.get( "his_tag_desc" ) ) ) {
                holder.desc.setText( ( String ) picMap.get( "his_tag_desc" ) );
            } else {
                holder.desc.setText( ( String ) picMap.get( "his_tag_title" ) );
            }
            String[] location = picMap.get( "his_location" ).toString()
                    .split( ":" );
            if ( location[0].equals( "暂无" ) ) {
                holder.address.setText( "     " );
            } else {
                holder.address.setText( location[0] );
            }

            String picUri = ( String ) picMap.get( "his_fileurl" );
            String picUrl = "http://tstatics.tujoin.com/print.php?w=140&h=140&t=c&url="
                    + picUri;
            loadImage( holder, picUrl );
            holder.picImage.setOnClickListener( arg0 -> {
                Intent intent = new Intent( HistoryActivity.this,
                                            ImagePagerActivity.class );
                Utils.log( TAG, "picUrl:" + picUrl + "   number:" + arrayList.size(), 5 );
                intent.putExtra( "picUrl", picUri );
                intent.putExtra( "number", arrayList.size() + "" );
                startActivity( intent );
            } );
            return convertView;
        }

        private void loadImage( final ViewHolder holder, final String picUrl ) {
            ImageAsyncTask task = new ImageAsyncTask( HistoryActivity.this,
                                                      holder.picImage, IstroopConstants.mLruCache );
            Bitmap bitmap = task.getBitmapFromMemoryCache( picUrl );
            Bitmap cache = task.getBitmapFileCache( picUrl );
            if ( bitmap != null ) {
                holder.picImage.setImageBitmap( bitmap );
            } else if ( cache != null ) {
                holder.picImage.setImageBitmap( cache );
            } else {
                // holder.picImage.setImageResource(R.drawable.picture_init);
                task.execute( picUrl );
            }
            // imageDownloaderpic.download(picUrl, holder.picImage);
        }

        private void removeFromListView( final int position ) {
            HashMap<String, Object> picMap = arrayList.get( position );
            if ( IstroopConstants.isLogin ) {
                final String wm_pid = ( String ) picMap.get( "pid" );
                if ( TextUtils.isEmpty( wm_pid ) || "null".equals( wm_pid ) ) {
                    int wm_id = ( Integer ) picMap.get( "his_wm_id" );
                    HisDBHelper hisDBHelper = new HisDBHelper(
                            HistoryActivity.this );
                    hisDBHelper.deleteFromDB( wm_id );
                    arrayList.remove( position );
                    notifyDataSetChanged();
                } else {
                    new Thread() {
                        public void run() {
                            try {
                                String delInfo = okhttps.get( IstroopConstants.URL_PATH
                                                                      + "/ICard/delHistory/?pid="
                                                                      + wm_pid );
                                if ( delInfo != null ) {
                                    JSONObject object = new JSONObject( delInfo );
                                    if ( object.getBoolean( "success" ) ) {
                                        arrayList.remove( position );
                                        Message message = Message.obtain();
                                        message.what = HIS_DELETE_SUCCESS;
                                        handler.sendMessage( message );
                                        // notifyDataSetChanged();
                                    } else {
                                        Log.i( TAG, "删除失败" );
                                    }
                                }
                            } catch ( JSONException | IOException e ) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
            } else {
                int wm_pid = ( Integer ) picMap.get( "his_wm_id" );
                HisDBHelper hisDBHelper = new HisDBHelper( HistoryActivity.this );
                hisDBHelper.deleteFromDB( wm_pid );
                arrayList.remove( position );
                notifyDataSetChanged();
            }

        }

        public class ViewHolder {
            ImageView      typeImage; // 类型图片
            TextView       title; // 显示标题
            TextView       createTime; // 扫描时间
            TextView       desc; // 描述
            TextView       address; // 扫描地址
            ImageView      picImage; // 图片缩略图
            Button         deleteBtn; // 删除按钮
            RelativeLayout his_item_right; // TODO 这是什么？
        }
    }

    @Override
    public void onTabActivityResult( int requestCode, int resultCode,
                                     Intent data ) {
        switch ( requestCode ) {
            case ICARD_HOME_TOP:
                Log.i( TAG, "data:" + data );
                if ( data != null ) {
                    Bundle bundle = data.getExtras();
                    if ( bundle != null ) {
                        UploadClientPic.upload(
                                Environment.getExternalStorageDirectory().getPath() + "/istroop/ichaotu_upload.jpg",
                                msg -> {
                                    Log.i( TAG, "上传图片返回的信息:" + msg );
                                    try {
                                        JSONObject object = new JSONObject( msg );
                                        if ( object.getBoolean( "success" ) ) {
                                            JSONArray jsonArray = object
                                                    .getJSONArray( "data" );
                                            JSONObject jsonInfo = jsonArray
                                                    .getJSONObject( 0 );
                                            String savename = jsonInfo
                                                    .getString( "savename" );
                                            String fileid = "group1" + savename;
                                            Log.i( TAG, "fildid:" + fileid );
                                            String set_sign = okhttps.get( IstroopConstants.URL_PATH
                                                                                   + "/Mobile/setinfo?bg_img="
                                                                                   + fileid );
                                            JSONObject jsonObject = new JSONObject(
                                                    set_sign );
                                            String data1 = null;
                                            if ( jsonObject
                                                    .getBoolean( "success" ) ) {
                                                data1 = jsonObject
                                                        .getString( "data" );
                                                Log.i( TAG, "成功修改后返回的信息"
                                                        + data1 );
                                            } else {
                                                data1 = jsonObject
                                                        .getString( "data" );
                                            }
                                            Message message = Message.obtain();
                                            message.what = HIS_INFO_BG;
                                            message.obj = data1;
                                            handler.sendMessage( message );
                                        }
                                    } catch ( JSONException | IOException e ) {
                                        e.printStackTrace();
                                    }
                                } );
                    }
                }

                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        // Log.i( TAG, "his返回键被点击" + IstroopConstants.mCamera );
        exit();
    }

    private void exit() {
        if ( !isExit ) {
            isExit = true;
            Toast.makeText( getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT ).show();
            // 利用handler延迟发送更改状态信息
            handler.sendEmptyMessageDelayed( 10, 2000 );
        } else {
            MyApplication.getInstance().exit();
        }
    }

    public class PopAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return IstroopConstants.types.length;
        }

        @Override
        public Object getItem( int position ) {
            return IstroopConstants.imageResIDPresseds[position];
        }

        @Override
        public long getItemId( int position ) {
            return position;
        }

        @Override
        public View getView( int position, View convertView, ViewGroup parent ) {
            ViewHolder holder;
            if ( convertView == null ) {
                holder = new ViewHolder();
                convertView = View.inflate( HistoryActivity.this,
                                            R.layout.reco_pop_lv_item, null );
                holder.icon = ( ImageView ) convertView
                        .findViewById( R.id.reco_pop_lv_item_icon );
                holder.desc = ( TextView ) convertView
                        .findViewById( R.id.reco_pop_lv_item_desc );
                convertView.setTag( holder );
            } else {
                holder = ( ViewHolder ) convertView.getTag();
            }
            holder.icon
                    .setImageResource( IstroopConstants.imageResIDs[position] );
            holder.desc.setText( types[position] );
            return convertView;
        }

    }

    private void deleteLoginInfo( Context ctx ) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(
                "config", MODE_PRIVATE );
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.clear();
        edit.apply();
    }

    public class ViewHolder {
        private ImageView icon;
        private TextView  desc;
    }

    class HistoryHandler extends Handler {
        @Override
        public void handleMessage( Message msg ) {
            super.handleMessage( msg );
            switch ( msg.what ) {
                case HIS_INFO_BG:
                    String data = ( String ) msg.obj;
                    // Toast.makeText(HistoryActivity.this, data,
                    // Toast.LENGTH_SHORT).show();
                    break;
                case HIS_SUCCESS:
                    Log.i( TAG, "对login的信息进行初始化" );

                    if ( arrayList != null && arrayList.size() > 0 ) {
                        // TODO 这两个对象是干什么的？
                        listView.setVisibility( View.VISIBLE );
                        his_error.setVisibility( View.INVISIBLE );
                        // TODO 这是在排序吗？通过时间进行排序？
                        for ( int i = 0; i < arrayList.size() - 1; i++ ) {
                            for ( int j = 1; j < arrayList.size() - i; j++ ) {
                                HashMap<String, Object> objects;
                                if ( ( Long ) arrayList.get( j - 1 ).get(
                                        "his_mtime" ) < ( Long ) arrayList.get( j )
                                        .get( "his_mtime" ) ) {
                                    objects = arrayList.get( j - 1 );
                                    arrayList.set( j - 1, arrayList.get( j ) );
                                    arrayList.set( j, objects );
                                }
                            }
                        }

                        pop_list = arrayList;
                        adapter = new HisAdapter();
                        listView.setAdapter( adapter );
                    } else {
                        listView.setVisibility( View.INVISIBLE );
                        his_error.setVisibility( View.VISIBLE );
                    }
                    break;
                case HIS_FAIL:
                    listView.setVisibility( View.INVISIBLE );
                    his_error.setVisibility( View.VISIBLE );
                    break;
                case HIS_DELETE_SUCCESS:
                    adapter.notifyDataSetChanged();
                    break;
                case 10: // TODO 你这是要闹那样!!!!!!!!!!!!!!!!!!!
                    isExit = false;
                    break;
                default:
                    break;
            }
        }
    }

}
