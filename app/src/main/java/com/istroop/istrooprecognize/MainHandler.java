package com.istroop.istrooprecognize;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.istroop.istrooprecognize.ui.activity.ICardTagPreview;
import com.istroop.istrooprecognize.ui.activity.RecoDetailEditionActivity;
import com.istroop.istrooprecognize.ui.activity.RecoDetailMapActivity;
import com.istroop.istrooprecognize.ui.activity.RecoDetailPicActivity;
import com.istroop.istrooprecognize.ui.activity.RecoDetailTextActivity;
import com.istroop.istrooprecognize.ui.activity.RecoDetailWebActivity;
import com.istroop.istrooprecognize.ui.fragment.RecoFragment;
import com.istroop.istrooprecognize.utils.HisDBHelper;
import com.istroop.istrooprecognize.utils.Okhttps;
import com.istroop.istrooprecognize.utils.Utils;
import com.istroop.openapi.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by joshua-zr on 8/31/15.
 */
public class MainHandler extends Handler {
    private static final String TAG = MainHandler.class.getSimpleName();

    private Activity mActivity;
    private Fragment mfragment;
    private String   hisInfo;
    private boolean  menu_icon_notClickable;

    public  int     DB_wm_id;
    public  int     DB_tag_type;
    public  long    DB_mtime;
    public  String  DB_fileurl;
    public  String  DB_tag_title;
    public  String  DB_tag_url;
    public  String  DB_tag_desc;
    public  String  DB_location;
    private String  shopping_url;
    private String  dB_is_history;
    private String  dB_pid;
    private Handler mHandler;

    public MainHandler( Fragment fragment ) {
        mfragment = fragment;
        mActivity = fragment.getActivity();
        mHandler = this;
    }

    public void handleMessage( Message msg ) {
        switch ( msg.what ) {
            case RecoFragment.MESAGE_SCAN_DATA:
                scandata();
                menu_icon_notClickable = false;
                break;
            case RecoFragment.RECO_ALBUM_FAIL:

                menu_icon_notClickable = false;
                Toast.makeText( mActivity,
                                mActivity.getResources().getString( R.string.reco_error ),
                                Toast.LENGTH_SHORT ).show();
                break;
            case RecoFragment.RECO_ALBUM_SUCCESS:
                // centerPro.setVisibility(View.INVISIBLE);
                final int wm_id = ( Integer ) msg.obj;
                new Thread() {
                    public void run() {
                        loadPicInfo( wm_id );
                    }
                }.start();
                break;
            case IstroopConstants.IAMessages_RESULT_NULL:
                ( ( RecoFragment ) mfragment ).centerProVisibility( View.INVISIBLE );
                break;
            case IstroopConstants.IAMessages_UNKNOWN_ERROR:
                String str = ( String ) msg.obj;
                if ( !TextUtils.isEmpty( str ) ) {
                    Toast.makeText( mActivity, str, Toast.LENGTH_LONG ).show();
                }
                ( ( RecoFragment ) mfragment ).centerProVisibility( View.INVISIBLE );
                break;
            case IstroopConstants.IAMessages_NETSWORK_SLOW:
                Toast.makeText( mActivity, "无网络反应，请检查网络配置", Toast.LENGTH_LONG )
                        .show();
                ( ( RecoFragment ) mfragment ).centerProVisibility( View.INVISIBLE );
                break;
            case IstroopConstants.IAMessages_SERVICE_ERROR:

                Dialog serviceDialog = new AlertDialog.Builder( mActivity )
                        .setTitle( "服务器异常" )
                        .setMessage( "服务器异常,请联系服务器负责人" )
                        .setPositiveButton( "确定",
                                            ( dialog, which ) -> {
                                                ( ( RecoFragment ) mfragment ).centerProVisibility( View.INVISIBLE );
                                            } ).create();
                serviceDialog.show();
                break;
            case IstroopConstants.IAMessages_NETWORK_ERROR:
                Dialog networkDialog = new AlertDialog.Builder( mActivity )
                        .setTitle( "网络错误" )
                        .setMessage( "无法连接到网络，请检查网络配置" )
                        .setPositiveButton( "确定",
                                            ( dialog, which ) -> {
                                                ( ( RecoFragment ) mfragment ).centerProVisibility( View.INVISIBLE );
                                            } ).create();
                networkDialog.show();
                break;
            case IstroopConstants.IAMessages_SHOW_PROGRESS:
                ( ( RecoFragment ) mfragment ).centerProVisibility( View.INVISIBLE );
                ( ( RecoFragment ) mfragment ).recoLineVisibility( View.VISIBLE );
                break;
            case IstroopConstants.IAMessages_SUB_FLAG_NO_WATERMARK:
                ( ( RecoFragment ) mfragment ).requestPreviewFrame();
                break;

            case IstroopConstants.IAMessages_SUB_WATERMARK_ID:
//				Log.i("Main", "info got:" + msg.arg1);
                if ( DB_tag_type == 8 ) {
                    ( ( RecoFragment ) mfragment ).centerProVisibility( View.INVISIBLE );
                    Intent webIntent = new Intent( mActivity,
                                                   RecoDetailWebActivity.class );
                    webIntent.putExtra( "picUrl", shopping_url );
                    // webIntent.putExtra("picType", "shooping");
                    // Log.i("shopping_url", "处理后的地址:"+shopping_url.trim());
                    mActivity.startActivity( webIntent );
                    break;
                } else if ( DB_tag_type < 0 ) {
                    Dialog serviceDialog2 = new AlertDialog.Builder( mActivity )
                            // .setTitle("服务器异常")
                            .setMessage( "这张超级图片里没有更多信息噢~" )
                            .setPositiveButton( "确定",
                                                ( dialog, which ) -> {
                                                    ( ( RecoFragment ) mfragment ).centerProVisibility( View.INVISIBLE );
                                                } ).create();
                    serviceDialog2.show();
                    break;
                }

                HisDBHelper dbHelper = new HisDBHelper( mActivity );
                DB_mtime = System.currentTimeMillis();
//				LogUtil.i(TAG, "当前的时间:" + DB_mtime);
                String time = DB_mtime + "";
                time = time.substring( 0, 10 );
                DB_mtime = Long.parseLong( time );
//				LogUtil.i(TAG, "处理后的当前的时间:" + DB_mtime);
                if ( DB_location == null ) {
                    DB_location = "暂无";
                }
                if ( IstroopConstants.isLogin ) {
//					LogUtil.i(TAG, "是否存进数据库:" + dB_is_history);
                    hisInfo = null;
                    if ( "1".equals( dB_is_history ) ) {
                        if ( dB_pid != null ) {
                            new Thread() {
                                @Override
                                public void run() {
                                    Okhttps okhttps = Okhttps.getInstance();
                                    try {
                                        okhttps.get( IstroopConstants.URL_PATH
                                                             + "/ICard/setHistory/?pid="
                                                             + dB_pid + "&params[wmid]="
                                                             + DB_wm_id
                                                             + "&params[Type]="
                                                             + DB_tag_type
                                                             + "&params[Title]="
                                                             + DB_tag_title.trim()
                                                             + "&params[Createtime]="
                                                             + DB_mtime
                                                             + "&params[Desc]="
                                                             + DB_tag_desc
                                                             + "&params[Link]="
                                                             + DB_tag_url
                                                             + "&params[Address]="
                                                             + DB_location
                                                             + "&params[PicUrl]="
                                                             + DB_fileurl );

                                        if ( hisInfo != null ) {
                                            JSONObject jsonObject = new JSONObject( hisInfo );
                                            if ( jsonObject
                                                    .getBoolean( "success" ) ) {
                                                Message message = Message
                                                        .obtain();
                                                message.what = RecoFragment.HIS_ADD_SUCCESS;
                                                mHandler.sendMessage( message );
                                            } else {
                                                Message message = Message
                                                        .obtain();
                                                message.what = RecoFragment.HIS_ADD_FAIL;
                                                mHandler.sendMessage( message );
                                            }
                                        }
                                    } catch ( JSONException | IOException e ) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                        }
                    } else {
//						LogUtil.i(TAG, "正在进行存入数据库操作");
                        new Thread() {
                            @Override
                            public void run() {
                                DB_tag_title = replaceBlank( DB_tag_title );
                                DB_tag_desc = replaceBlank( DB_tag_desc );
                                DB_location = replaceBlank( DB_location );
                                Okhttps okhttps = Okhttps.getInstance();
                                try {
                                    okhttps.get( IstroopConstants.URL_PATH
                                                         + "/ICard/setHistory/?"
                                                         + "params[wmid]=" + DB_wm_id
                                                         + "&params[Type]="
                                                         + DB_tag_type
                                                         + "&params[Title]="
                                                         + DB_tag_title.split( " " )[0]
                                                         + "&params[Createtime]="
                                                         + DB_mtime + "&params[Desc]="
                                                         + DB_tag_desc
                                                         + "&params[Link]=" + DB_tag_url
                                                         + "&params[Address]="
                                                         + DB_location
                                                         + "&params[PicUrl]="
                                                         + DB_fileurl );
                                    if ( hisInfo != null ) {
                                        JSONObject jsonObject = new JSONObject(
                                                hisInfo );
                                        if ( jsonObject.getBoolean( "success" ) ) {
                                            Message message = Message.obtain();
                                            message.what = RecoFragment.HIS_ADD_SUCCESS;
                                            mHandler.sendMessage( message );
                                        } else {
                                            Message message = Message.obtain();
                                            message.what = RecoFragment.HIS_ADD_FAIL;
                                            mHandler.sendMessage( message );
                                        }
                                    }
                                } catch ( JSONException | IOException e ) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                } else {
                    boolean isIn = dbHelper.isInDB( DB_wm_id );
                    if ( isIn ) {
                        dbHelper.updateDB( DB_wm_id, DB_fileurl, DB_tag_type,
                                           DB_tag_url, DB_tag_title, DB_tag_desc,
                                           DB_mtime, DB_location );
                    } else {
                        dbHelper.insertIntoDB( DB_wm_id, DB_fileurl,
                                               DB_tag_type, DB_tag_url, DB_tag_title,
                                               DB_tag_desc, DB_mtime, DB_location );
                    }
                    Message message = Message.obtain();
                    message.what = RecoFragment.HIS_ADD_SUCCESS;
                    this.sendMessage( message );
                }

                break;
            case RecoFragment.HIS_ADD_SUCCESS:
                ( ( RecoFragment ) mfragment ).centerProVisibility( View.INVISIBLE );
                ( ( RecoFragment ) mfragment ).recoLineVisibility( View.VISIBLE );
                if ( DB_tag_type == 1 || DB_tag_type == 2 || DB_tag_type == 5
                        || DB_tag_type == 6 ) {
                    Intent webIntent = new Intent( mActivity,
                                                   RecoDetailWebActivity.class );
//					LogUtil.i(TAG, "url:" + DB_tag_url);
                    webIntent.putExtra( "picUrl", DB_tag_url );
                    mActivity.startActivity( webIntent );
                } else if ( DB_tag_type == 0 ) {
                    Intent textIntent = new Intent( mActivity,
                                                    RecoDetailTextActivity.class );
                    textIntent.putExtra( "DB_tag_title", DB_tag_title );
                    textIntent.putExtra( "DB_tag_desc", DB_tag_desc );
                    mActivity.startActivity( textIntent );

                } else if ( DB_tag_type == 4 ) {
                    Intent piciIntent = new Intent( mActivity,
                                                    RecoDetailPicActivity.class );
                    piciIntent.putExtra( "DB_tag_title", DB_tag_title );
                    piciIntent.putExtra( "DB_tag_url", DB_tag_url );
                    mActivity.startActivity( piciIntent );

                } else if ( DB_tag_type == 7 ) {
                    Intent mapIntent = new Intent( mActivity,
                                                   RecoDetailMapActivity.class );
                    mapIntent.putExtra( "DB_tag_url", DB_tag_url );
                    mapIntent.putExtra( "DB_tag_title", DB_tag_title );
                    mActivity.startActivity( mapIntent );

                } else if ( DB_tag_type == 3 ) {
                    DB_tag_title = DB_tag_title + "==" + DB_tag_url;
                    String[] split = DB_tag_title.split( "==" );
//					LogUtil.i(TAG, "描述信息:" + DB_tag_desc);
                    if ( !TextUtils.isEmpty( DB_tag_desc ) ) {
                        if ( "copy".equals( DB_tag_desc ) ) {
                            Intent editionIntent = new Intent(
                                    mActivity,
                                    RecoDetailEditionActivity.class );
                            editionIntent.putExtra( "DB_tag_url", DB_tag_url );
                            editionIntent
                                    .putExtra( "DB_tag_title", DB_tag_title );
                            mActivity.startActivity( editionIntent );
                        } else {
                            Intent editionIntent = new Intent(
                                    mActivity, ICardTagPreview.class );
                            Bundle bundle = new Bundle();
                            bundle.putStringArray( "cardInfos", split );
                            bundle.putString( "headurl", DB_fileurl );
                            editionIntent.putExtras( bundle );
                            mActivity.startActivity( editionIntent );
                        }
                    } else {
                        Intent editionIntent = new Intent( mActivity,
                                                           ICardTagPreview.class );
                        // editionIntent.putExtra("DB_tag_url", DB_tag_url);
                        Bundle bundle = new Bundle();
                        bundle.putStringArray( "cardInfos", split );
                        bundle.putString( "card_type", "reco" );
                        editionIntent.putExtras( bundle );
                        // editionIntent.putExtra("DB_tag_title", DB_tag_title);
                        mActivity.startActivity( editionIntent );
                    }
                }
                break;
            case RecoFragment.HIS_ADD_FAIL:
                Toast.makeText( mActivity, "保存失败", Toast.LENGTH_SHORT )
                        .show();
                break;
            default:
                Dialog serviceDialog2 = new AlertDialog.Builder( mActivity )
                        .setMessage( "这张超级图片里没有更多信息噢~" )
                        .setPositiveButton( "确定",
                                            ( dialog, which ) -> {
                                                ( ( RecoFragment ) mfragment ).centerProVisibility( View.INVISIBLE );
                                            } ).create();
                serviceDialog2.show();
                break;
        }
    }

    private void scandata() {
        new Thread( () -> {
            Okhttps okhttps = Okhttps.getInstance();
            String path = Constant.URL_PATH + "stat.gif?plat=android&type=scan&gps=" + Constant.coordinate.latitude + "," + Constant.coordinate.longitude + "&device_id=" + Constant.imei + "&device_type=" + Constant.model + "&appkey=" + Constant.appKey;
            try {
                String s = okhttps.get( path );
                Utils.log( TAG, s, 5 );
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        } ).start();
    }

    public void loadPicInfo( final int recoResult ) {
        Okhttps okhttps = Okhttps.getInstance();
//		LogUtil.i(TAG, "是否声音:" + IstroopConstants.isSound + "是否震动:"
//				+ IstroopConstants.isVibrator);
        Message msg = new Message();
        msg.what = RecoFragment.MESAGE_SCAN_DATA;
        this.sendMessage( msg );
        if ( IstroopConstants.isSound && IstroopConstants.isVibrator ) {
            setVibrator();
        } else if ( !IstroopConstants.isSound && IstroopConstants.isVibrator ) {
            setVibrator();
        } else {
            setVibrator();
        }
        DB_wm_id = recoResult;

        if ( !Utils.isConn( mActivity.getApplicationContext() ) ) {
            Message message = this.obtainMessage();
            message.what = IstroopConstants.IAMessages_NETWORK_ERROR;
            this.sendMessage( message );
            return;
        }

        // http://tapi.tujoin.com/ICard/getInfo/?wmid=2232
        // String picurlStr="http://print.ichaotu.com/api/info/?wmid="+DB_wm_id;
        String picurlStr = IstroopConstants.URL_PATH + "/ICard/getInfo/?wmid="
                + DB_wm_id;
//		LogUtil.i(TAG, "picurlStr:" + picurlStr);
        String picResult = null;

        try {
            if ( IstroopConstants.isLogin ) {
                picResult = okhttps.get( picurlStr );
//                picResult = HttpTools.userInfo( picurlStr,
//                                                IstroopConstants.cookieStore );
            } else {
                picResult = okhttps.get( picurlStr );
//                picResult = HttpTools.toString( picurlStr );
            }
//			LogUtil.i(TAG, "picResult:" + picResult);
            try {
                if ( picResult == null ) {
                    // 重新扫描数据
                    Message message = this.obtainMessage();
                    message.what = IstroopConstants.IAMessages_RESULT_NULL;
                    this.sendMessage( message );
                    return;
                } else if ( "联网失败".equals( picResult ) ) {
                    Message message = this.obtainMessage();
                    message.what = IstroopConstants.IAMessages_NETWORK_ERROR;
                    this.sendMessage( message );
                    return;
                }

                JSONObject resultObject = new JSONObject( picResult );
//				LogUtil.i(TAG, resultObject + "resultObject");
                boolean success = resultObject.getBoolean( "success" );
//				LogUtil.i(TAG, success + "状态成功与否");
                // {"data":{"3596":"无权限访问的超图"},"success":true}resultObject
                if ( success ) {
                    JSONObject temObject = resultObject.getJSONObject( "data" );
                    if ( temObject.optJSONObject( DB_wm_id + "" ) == null ) {
                        String data = temObject.getString( DB_wm_id + "" );
                        Message message = this.obtainMessage();
                        message.what = IstroopConstants.IAMessages_UNKNOWN_ERROR;
                        message.obj = data;
                        this.sendMessage( message );
                        return;
                    }
                    JSONObject dataObject = temObject.getJSONObject( DB_wm_id
                                                                             + "" );
                    DB_fileurl = dataObject.getString( "fileid" );
                    if ( TextUtils.isEmpty( DB_fileurl )
                            || "null".equals( DB_fileurl ) ) {
                        Message message = this.obtainMessage();
                        message.what = IstroopConstants.IAMessages_UNKNOWN_ERROR;
                        message.obj = "图片没有更多的信息!";
                        this.sendMessage( message );
                        return;
                    }
                    if ( dataObject.optJSONObject( "tags" ) == null ) {
                        Message message = this.obtainMessage();
                        message.what = IstroopConstants.IAMessages_UNKNOWN_ERROR;
                        message.obj = "图片没有更多的信息!";
                        this.sendMessage( message );
                        return;
                    }
                    JSONObject tagObject = dataObject.getJSONObject( "tags" );
                    if ( dataObject.isNull( "is_history" ) ) {
                        dB_is_history = "";
                    } else {
                        dB_is_history = dataObject.getString( "is_history" );
                    }
                    if ( dataObject.isNull( "pid" ) ) {
                        dB_pid = "";
                    } else {
                        dB_pid = dataObject.getString( "pid" );
                    }
//					LogUtil.i(TAG, "是否为历史记录:" + dB_is_history + "pid:" + dB_pid);
                    Iterator keys = tagObject.keys();
                    JSONObject contentObject;
                    ArrayList<String> arr = new ArrayList<>();
                    while ( keys.hasNext() ) {
                        String key = ( String ) keys.next();
                        arr.add( key );
                    }
//					LogUtil.i(TAG, "标记号码:" + arr.get(arr.size() - 1));
                    JSONObject jsonObject = tagObject.getJSONObject( arr.get( arr
                                                                                      .size() - 1 ) );
                    String typeString = jsonObject.getString( "type" );
                    contentObject = jsonObject.getJSONObject( "content" );
                    DB_tag_type = tagTypewithString( typeString );
                    if ( DB_tag_type == 3 ) {// copyright
//						LogUtil.i(TAG, "内容的数量:" + contentObject.length());
                        if ( contentObject.length() == 4 ) {
                            DB_tag_title = "copy";
                            DB_tag_url = "copy";
                            DB_tag_desc = "copy";
                        } else if ( contentObject.length() != 10
                                && contentObject.length() != 9 ) {
                            String realname = contentObject
                                    .getString( "realname" );
                            String company = contentObject.getString( "company" );
                            String job = contentObject.getString( "job" );
                            String companyUrl = contentObject
                                    .getString( "companyUrl" );
                            String email = contentObject.getString( "email" );
                            String phone = contentObject.getString( "phone" );
                            String weixin = contentObject.getString( "weixin" );
                            String introduce = contentObject
                                    .getString( "introduce" );
                            DB_tag_title = realname;
                            DB_tag_url = company + "==" + job + "=="
                                    + companyUrl + "==" + email + "==" + phone
                                    + "==" + weixin + "==" + introduce;
                            DB_tag_desc = "copyright";
                        } else {
                            String name = contentObject.getString( "name" );
                            String phone = contentObject.getString( "phone" );
                            String mail = contentObject.getString( "mail" );
                            String company = contentObject.getString( "company" );
                            String department = contentObject
                                    .getString( "department" );
                            String position = contentObject
                                    .getString( "position" );
                            String companyweb = contentObject
                                    .getString( "companyweb" );
                            String address = contentObject.getString( "address" );
                            String sign = contentObject.getString( "sign" );
                            String weixin = contentObject.getString( "weixin" );
                            DB_tag_title = name;
                            DB_tag_url = phone + "==" + mail + "==" + company
                                    + "==" + department + "==" + position
                                    + "==" + companyweb + "==" + address + "=="
                                    + sign + "==" + weixin;
                            DB_tag_desc = "";
//							LogUtil.i(TAG, "标题:" + DB_tag_title);
//							LogUtil.i(TAG, "描述:" + DB_tag_url);
                        }
                    } else if ( DB_tag_type == 4 || DB_tag_type == 5 ) {// pic
                        // personage
                        DB_tag_url = contentObject.getString( "url" );
                        DB_tag_title = contentObject.getString( "desc" );
                        DB_tag_desc = contentObject.getString( "desc" );
                    } else if ( DB_tag_type == 8 ) {// shopping
                        shopping_url = contentObject.getString( "url" );
//						LogUtil.i(TAG, "购物车:" + shopping_url);
                    } else if ( DB_tag_type == 0 ) {// text
                        DB_tag_title = contentObject.getString( "title" );
//						LogUtil.i(TAG, "标题" + DB_tag_title);
                        DB_tag_desc = contentObject.getString( "desc" );
//						LogUtil.i(TAG, "描述" + DB_tag_desc);
                    } else {// link
                        DB_tag_url = contentObject.getString( "url" );
//						LogUtil.i(TAG, "地址url:" + DB_tag_url);
                        DB_tag_title = contentObject.getString( "title" );
//						LogUtil.i(TAG, "标题" + DB_tag_title);
                        if ( !contentObject.isNull( "desc" ) ) {
                            DB_tag_desc = contentObject.getString( "desc" );
                        } else {
                            DB_tag_desc = "";
                        }
//						LogUtil.i(TAG, "描述" + DB_tag_desc);
                    }
                    // if(DB_tag_type==0||DB_tag_type==1||DB_tag_type==2||DB_tag_type==6||DB_tag_type==7||DB_tag_type==-1)
                    // shopping_url = shopping_url.trim();
                    // DB_tag_url = DB_tag_url.trim();
                    Message message = this.obtainMessage();
                    message.arg1 = 1;
                    message.what = IstroopConstants.IAMessages_SUB_WATERMARK_ID;
                    this.sendMessage( message );
                } else {
                    Message message = this.obtainMessage();
                    message.what = IstroopConstants.IAMessages_SERVICE_ERROR;
                    this.sendMessage( message );
                }

            } catch ( JSONException e ) {
                e.printStackTrace();
//				LogUtil.i(TAG, e.toString());
                Message message = this.obtainMessage();
                message.what = IstroopConstants.IAMessages_UNKNOWN_ERROR;
                message.obj = picResult;
                this.sendMessage( message );
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            Message message = this.obtainMessage();
            message.what = IstroopConstants.IAMessages_NETSWORK_SLOW;
            this.sendMessage( message );

        }
    }

    public static String replaceBlank( String str ) {
        String dest = "";
        if ( str != null ) {
            Pattern p = Pattern.compile( "\\s*|\t|\r|\n" );
            Matcher m = p.matcher( str );
            dest = m.replaceAll( "" );
        }
        return dest;
    }

    public int tagTypewithString( String aString ) {
        switch ( aString ) {
            case "text":
                return 0;
            case "link":
                return 1;
            case "music":
                return 2;
            case "copyright":
                return 3;
            case "pic":
                return 4;
            case "personage":
                return 5;
            case "video":
                return 6;
            case "map":
                return 7;
            case "shopping":
                return 8;
            default:
                return -1;
        }
    }

    private void setVibrator() {
        Vibrator vibrator = ( Vibrator ) mActivity.getSystemService( Context.VIBRATOR_SERVICE );
        long[] pattern = { 100, 300, 100, 300 };
        vibrator.vibrate( pattern, -1 );
    }
}
