package com.istroop.istrooprecognize.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ICardTagVideoMapActivity extends BaseActivity implements
        OnClickListener {

    protected static final int    TAG_NETWORK_ERROR     = 31;
    protected static final int    TAG_RESULT_ERROR      = 32;
    protected static final String TAG                   = "ICardTagItemActivity";
    protected static final int    ICARD_TAG_VIDEO_NULL  = 33;
    protected static final int    ICARD_TAG_VIDEO_MORE  = 34;
    protected static final int    TAG_video_ADD_SUCCESS = 35;
    protected static final int    TAG_video_ADD_FAIL    = 36;
    protected static final int    TAG_video_ADD_NULL    = 37;
    private EditText                           design_tag_video_et;
    private ListView                           design_tag_video_lv;
    private ArrayList<HashMap<String, String>> videosList;
    private String                             video_link;
    private String                             video_title;
    private ArrayList<Map<String, String>>     list;
    private MapAdapter                         mapAdapter;
    private ListView                           design_tag_map_lv;
    private VideoHandler handler = new VideoHandler();
    private VideoAdapter adapter;
    private String       type;
    private String[]     contents;
    private int position = 3;

    private Okhttps okhttps;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.icard_video_item );
        init();
    }

    public void init() {
        okhttps = Okhttps.getInstance();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        type = bundle.getString( "type", "类型" );
        String text = bundle.getString( "text" );
        position = bundle.getInt( "position" );
        contents = bundle.getStringArray( "contents" );
        Log.i( TAG, "类型:" + type );
        design_tag_video_et = ( EditText ) findViewById( R.id.design_tag_video_et );
        Button design_tag_video_bt = ( Button ) findViewById( R.id.design_tag_video_bt );
        design_tag_video_lv = ( ListView ) findViewById( R.id.design_tag_video_lv );
        TextView design_type_name = ( TextView ) findViewById( R.id.design_type_name );
        TextView design_tag_video_cannel = ( TextView ) findViewById( R.id.design_tag_video_cannel );
        RelativeLayout design_tag_video_save = ( RelativeLayout ) findViewById( R.id.design_tag_video_save );
        design_tag_map_lv = ( ListView ) findViewById( R.id.design_tag_map_lv );
        mapAdapter = new MapAdapter();
        design_type_name.setText( type );
        adapter = new VideoAdapter();
        videosList = new ArrayList<>();
        list = new ArrayList<>();
        if ( !TextUtils.isEmpty( text ) ) {
            design_tag_video_et.setText( text );
            design_tag_video_et.setSelection( text.length() );
        }
        // tagResultMap = new HashMap<String, String>();
        design_tag_video_bt.setOnClickListener( this );
        design_tag_video_save.setOnClickListener( this );
        design_tag_video_cannel.setOnClickListener( this );
        design_tag_map_lv.setOnItemClickListener( ( parent, view, position1, id ) -> {
            design_tag_map_lv.setVisibility( View.INVISIBLE );
            design_tag_video_lv.setVisibility( View.INVISIBLE );
            Map<String, String> map = list.get( position1 );
            String latitude = map.get( "latitude" );
            String longitude = map.get( "longitude" );
            String name = map.get( "name" );
            video_title = name;
            video_link = latitude + ":" + longitude;
            design_tag_video_et.setText( name );
            Log.i( TAG, latitude + ":" + longitude + ":" + name );
            Log.i( TAG, Integer.parseInt( latitude ) + ":"
                    + Integer.parseInt( longitude ) );
        } );
        design_tag_video_lv.setOnItemClickListener( ( parent, view, position1, id ) -> {
            if ( adapter.getSelectIndex() == -1 ) {
                adapter.setSelectIndex( position1 );
            } else {
                adapter.setSelectIndex( position1 );
            }
            adapter.notifyDataSetChanged();
        } );

        if ( position == 3 ) {
            design_type_name.setText( getResources().getString(
                    R.string.map_title ) );
            design_tag_video_et.setHint( getResources().getString(
                    R.string.tag_info_map ) );
            design_tag_map_lv.setVisibility( View.INVISIBLE );
            design_tag_video_lv.setVisibility( View.INVISIBLE );

        } else {
            design_type_name.setText( getResources().getString(
                    R.string.video_title ) );
            design_tag_video_et.setHint( getResources().getString(
                    R.string.tag_info_video_hint ) );
        }
    }

    @Override
    public void onClick( View v ) {
        switch ( v.getId() ) {
            case R.id.design_tag_video_bt:
                String video_key = design_tag_video_et.getText().toString().trim();
                if ( TextUtils.isEmpty( video_key ) ) {
                    Toast.makeText( ICardTagVideoMapActivity.this, "关键字不能为空", Toast.LENGTH_SHORT ).show();
                    return;
                }
                if ( position == 4 ) {
                    // 下面将进行联网操作根据关键字进行搜索
                    Log.i( TAG, "搜索关键字" + video_key );
                    design_tag_map_lv.setVisibility( View.INVISIBLE );
                    design_tag_video_lv.setVisibility( View.VISIBLE );
                    videosList = new ArrayList<>();
                    loadVideoDesc( video_key );
                } else if ( position == 3 ) {
                    list = new ArrayList<>();
                    Log.i( TAG, "搜索关键字" + video_key );
                }
                design_tag_map_lv.setVisibility( View.VISIBLE );
                design_tag_video_lv.setVisibility( View.INVISIBLE );
                design_tag_map_lv.setAdapter( mapAdapter );
            case R.id.design_tag_video_cannel:
                finish();
                break;
            case R.id.design_tag_video_save:
                if ( TextUtils.isEmpty( video_link ) || TextUtils.isEmpty( video_title ) ) {
                    Toast.makeText( ICardTagVideoMapActivity.this, "请选择后再确定",
                                    Toast.LENGTH_SHORT ).show();
                    return;
                }
                String temp = "";
                if ( "视频".equals( type ) ) {
                    temp = "&content[title]=" + video_title + "&content[url]="
                            + video_link + "&type=video";
                } else if ( "位置".equals( type ) ) {
                    String[] split = video_link.split( ":" );
                    if ( split.length == 1 ) {
                        return;
                    }
                    temp = "&content[title]=" + video_title + "&content[lat]="
                            + Integer.parseInt( split[1] ) / 1E6 + "&content[lng]="
                            + Integer.parseInt( split[0] ) / 1E6 + "&type=map";
                }
                if ( !TextUtils.isEmpty( temp ) ) {
                    saveTagInfo( temp );
                }
                break;
            default:
                break;
        }
    }

    private void saveTagInfo( final String temp ) {
        new Thread() {
            public void run() {
                // /ICard/setdefaulttag?default=1&content[title]=**&type=text&tid=***
                String info = null;
                try {
                    if ( contents != null && contents.length != 0 ) {
                        // 修改标记
                        info = okhttps.get( IstroopConstants.URL_PATH
                                                    + "/ICard/setdefaulttag?default=1" + temp + "&tid="
                                                    + contents[0] );
                    }
                    if ( !TextUtils.isEmpty( info ) ) {
                        Log.i( TAG, "添加标记返回的信息:" + info );
                        // 添加标记返回的信息:{"success":true,"data":"\u8bbe\u7f6e\u6210\u529f"}
                        JSONObject object = new JSONObject( info );
                        if ( object.getBoolean( "success" ) ) {
                            String data = object.getString( "data" );
                            Message message = Message.obtain();
                            message.obj = data;
                            message.what = TAG_video_ADD_SUCCESS;
                            handler.sendMessage( message );
                        }
                    } else {
                        Message message = Message.obtain();
                        message.what = TAG_video_ADD_NULL;
                        handler.sendMessage( message );
                    }
                } catch ( JSONException e ) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = TAG_video_ADD_FAIL;
                    handler.sendMessage( message );
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void loadVideoDesc( final String keyword ) {
        new Thread() {
            public void run() {
                if ( !RecoActivity.isConn( getApplicationContext() ) ) {
                    Message message = Message.obtain();
                    message.what = TAG_NETWORK_ERROR;
                    handler.sendMessage( message );
                    return;
                }
                try {
                    String encode = URLEncoder.encode( keyword, "UTF-8" );
                    String result = okhttps.get(
                            IstroopConstants.URL_PATH
                                    + "/util/youku?keyword=" + encode
                                    + "&page=1&count=15" );
                    if ( result == null ) {
                        Message message = Message.obtain();
                        message.what = TAG_RESULT_ERROR;
                        handler.sendMessage( message );
                        return;
                    }
                    Log.i( TAG, "进入对返回结果的解析" );
                    Log.i( TAG, "联网获取返回的结果:" + result );
                    JSONObject resultObject = new JSONObject( result );
                    if ( resultObject.getBoolean( "success" ) ) {
                        JSONObject dataInfos = resultObject
                                .getJSONObject( "data" );
                        Log.i( TAG, "视频的json数据返回的结果是:" + resultObject );
                        JSONArray videoArray = dataInfos.getJSONArray( "videos" );
                        HashMap<String, String> map;
                        for ( int i = 0; i < videoArray.length(); i++ ) {
                            JSONObject videoObject = videoArray
                                    .getJSONObject( i );
                            String video_id = videoObject.getString( "id" );
                            Log.i( TAG, "视频的id:" + video_id );
                            String video_title = videoObject.getString( "title" );
                            Log.i( TAG, "视频的标题:" + video_title );
                            String video_link = videoObject.getString( "link" );
                            Log.i( TAG, "视频的链接:" + video_link );
                            map = new HashMap<>();
                            map.put( "video_id", video_id );
                            map.put( "video_title", video_title );
                            map.put( "video_link", video_link );
                            videosList.add( map );
                        }
                        if ( videosList.size() == 0 ) {
                            // 对搜索结果为空进行处理
                            Message message = Message.obtain();
                            message.what = ICARD_TAG_VIDEO_NULL;
                            handler.sendMessage( message );
                        } else {
                            Message message = Message.obtain();
                            message.what = ICARD_TAG_VIDEO_MORE;
                            handler.sendMessage( message );
                        }
                    }
                } catch ( IOException e ) {
                    e.printStackTrace();
                    Log.i( TAG, " tag_io error" );
                } catch ( JSONException e ) {
                    e.printStackTrace();
                    Log.i( TAG, " tag_json error" );
                }
            }
        }.start();
    }

    public class VideoAdapter extends BaseAdapter {

        private int selectIndex = -1;

        @Override
        public int getCount() {
            return videosList.size();
        }

        public int getSelectIndex() {
            return selectIndex;
        }

        public void setSelectIndex( int selectIndex ) {
            this.selectIndex = selectIndex;
        }

        @Override
        public Object getItem( int position ) {
            return null;
        }

        @Override
        public long getItemId( int position ) {
            return position;
        }

        @Override
        public View getView( final int position, View convertView,
                             ViewGroup parent ) {
            final ViewHolder holder;
            if ( convertView == null ) {
                holder = new ViewHolder();
                convertView = View.inflate( ICardTagVideoMapActivity.this,
                                            R.layout.icard_design_tag_video_item, null );
                holder.design_tag_video_item_id = ( TextView ) convertView
                        .findViewById( R.id.design_tag_video_item_id );
                holder.design_tag_video_item_desc = ( TextView ) convertView
                        .findViewById( R.id.design_tag_video_item_desc );
                holder.design_tag_video_item_add = ( TextView ) convertView
                        .findViewById( R.id.design_tag_video_item_add );
                convertView.setTag( holder );
            } else {
                holder = ( ViewHolder ) convertView.getTag();
            }

            holder.design_tag_video_item_id.setText( position + 1 + "" );
            holder.design_tag_video_item_desc.setText( videosList.get( position )
                                                               .get( "video_title" ) );
            if ( getSelectIndex() == position ) {
                holder.design_tag_video_item_add.setVisibility( View.VISIBLE );
                video_link = videosList.get( position ).get( "video_link" );
                video_title = videosList.get( position ).get( "video_title" );
                design_tag_video_et.setText( video_title );
            } else {
                holder.design_tag_video_item_add.setVisibility( View.INVISIBLE );
            }
            return convertView;
        }
    }

    public class ViewHolder {
        private TextView design_tag_video_item_id;
        private TextView design_tag_video_item_desc;
        private TextView design_tag_video_item_add;
    }

    public class MapAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem( int position ) {
            return null;
        }

        @Override
        public long getItemId( int position ) {
            return position;
        }

        @Override
        public View getView( int position, View convertView, ViewGroup parent ) {

            View view = View.inflate( ICardTagVideoMapActivity.this,
                                      R.layout.icard_tag_map_item, null );
            TextView icard_tag_map_item = ( TextView ) view
                    .findViewById( R.id.icard_tag_map_item );
            icard_tag_map_item.setText( list.get( position ).get( "name" ) + " "
                                                + list.get( position ).get( "address" ) );
            return view;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if ( videosList != null ) {
            videosList = null;
        }
        finish();
    }

    @Override
    protected void onSaveInstanceState( @NonNull Bundle outState ) {
        super.onSaveInstanceState( outState );
    }

    @Override
    protected void onRestoreInstanceState( @NonNull Bundle savedInstanceState ) {
        super.onRestoreInstanceState( savedInstanceState );
    }

    class VideoHandler extends Handler {
        @Override
        public void handleMessage( Message msg ) {
            super.handleMessage( msg );
            switch ( msg.what ) {
                case ICARD_TAG_VIDEO_NULL:
                    Toast.makeText( ICardTagVideoMapActivity.this, "搜索结果为空", Toast.LENGTH_SHORT ).show();
                    break;
                case ICARD_TAG_VIDEO_MORE:
                    design_tag_video_lv.setAdapter( adapter );
                    break;
                case TAG_video_ADD_SUCCESS:
                    String data = ( String ) msg.obj;
                    Toast.makeText( ICardTagVideoMapActivity.this, data,
                                    Toast.LENGTH_SHORT ).show();
                    Intent intent = new Intent();
                    setResult( RESULT_OK, intent );
                    // TODO 更新列表
                    finish();
                    break;
                case TAG_video_ADD_FAIL:
                    break;
                case TAG_video_ADD_NULL:
                    break;
                default:
                    break;
            }
        }
    }
}
