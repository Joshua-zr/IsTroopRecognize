package com.istroop.istrooprecognize.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
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

public class ICardTagInfo extends BaseActivity implements OnClickListener {

    protected static final String TAG                  = "ICardTagInfo";
    protected static final int    TAG_INFO_ADD_SUCCESS = 1;
    protected static final int    TAG_INFO_ADD_FAIL    = 2;
    protected static final int    TAG_INFO_ADD_NULL    = 3;
    private RelativeLayout design_tag_link_info;
    private EditText       design_tag_link_exp_info;
    private RelativeLayout design_tag_title_info;
    private TextView       design_tag_title_tv_info;
    private EditText       design_tag_title_exp_info;
    private RelativeLayout design_tag_desc_info;
    private EditText       design_tag_desc_exp_info;
    private RelativeLayout design_tag_video_map_rl_info;
    private TextView       design_tag_video_map_title_info;
    private TextView       design_tag_video_map_desc_info;
    private ImageView      icard_tag_line2_info;
    private ImageView      icard_tag_line3_info;
    private String[]       infos;
    private String substring = "";
    private String video_link;
    private int             position = -1;
    private boolean         isClick  = false;
    private IcardTagHandler handler  = new IcardTagHandler();
    private String[] contents;
    private String[] types;
    private Okhttps  okhttps;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.icard_tag_info );
        init();
    }

    private void init() {

        okhttps = Okhttps.getInstance();
        design_tag_link_info = ( RelativeLayout ) findViewById( R.id.design_tag_link_info );
        design_tag_link_exp_info = ( EditText ) findViewById( R.id.design_tag_link_exp_info );
        design_tag_title_info = ( RelativeLayout ) findViewById( R.id.design_tag_title_info );
        design_tag_title_tv_info = ( TextView ) findViewById( R.id.design_tag_title_tv_info );
        design_tag_title_exp_info = ( EditText ) findViewById( R.id.design_tag_title_exp_info );
        design_tag_desc_info = ( RelativeLayout ) findViewById( R.id.design_tag_desc_info );
        design_tag_desc_exp_info = ( EditText ) findViewById( R.id.design_tag_desc_exp_info );
        design_tag_video_map_rl_info = ( RelativeLayout ) findViewById( R.id.design_tag_video_map_rl_info );
        design_tag_video_map_title_info = ( TextView ) findViewById( R.id.design_tag_video_map_title_info );
        design_tag_video_map_desc_info = ( TextView ) findViewById( R.id.design_tag_video_map_desc_info );
        icard_tag_line2_info = ( ImageView ) findViewById( R.id.icard_tag_line2_info );
        icard_tag_line3_info = ( ImageView ) findViewById( R.id.icard_tag_line3_info );
        TextView icard_tag_info_cannel = ( TextView ) findViewById( R.id.icard_tag_info_cannel );
        TextView icard_tag_info_title = ( TextView ) findViewById( R.id.icard_tag_info_title );
        TextView icard_tag_info_save = ( TextView ) findViewById( R.id.icard_tag_info_save );
        types = new String[] {
                getResources().getString( R.string.version_title ),
                getResources().getString( R.string.link_title ),
                getResources().getString( R.string.text_title ),
                getResources().getString( R.string.map_title ),
                getResources().getString( R.string.video_title ),
                getResources().getString( R.string.pic_title ),
                getResources().getString( R.string.person_title ) };
        icard_tag_info_cannel.setOnClickListener( this );
        icard_tag_info_save.setOnClickListener( this );
        design_tag_link_info.setOnClickListener( this );
        design_tag_title_info.setOnClickListener( this );
        design_tag_desc_info.setOnClickListener( this );
        design_tag_video_map_rl_info.setOnClickListener( this );
        Intent data = getIntent();
        Bundle bundle = data.getExtras();
        String tag = bundle.getString( "tag_type", "添加标记" );
        infos = bundle.getStringArray( "copyrights" );
        contents = bundle.getStringArray( "contents" );
        for ( int i = 0; i < types.length; i++ )
            if ( types[i].equals( tag ) ) {
                substring = types[i];
                position = i;
                if ( 1 == i ) {
                    icard_tag_info_title.setText( getResources().getString(
                            R.string.link_title ) );
                    // setLink("www.ichaotu.com","超级图片","给你不一样的体验!");
                    if ( contents != null && contents.length != 0 ) {
                        setLink( contents[1], contents[2], contents[3] );
                    } else {
                        setLink( "", "", "" );
                    }
                } else if ( 4 == i ) {
                    icard_tag_info_title.setText( getResources().getString(
                            R.string.video_title ) );
                    // setVideo("www.ichaotu.com","");
                    if ( contents != null && contents.length != 0 ) {
                        setVideo( contents[2] );
                    } else {
                        setVideo( "" );
                    }

                } else if ( 3 == i ) {
                    icard_tag_info_title.setText( getResources().getString(
                            R.string.map_title ) );
                    // setMap("北京市海淀区","");
                    if ( contents != null && contents.length != 0 ) {
                        setMap( contents[2] );
                    } else {
                        setMap( "" );
                    }

                } else if ( 0 == i ) {
                    icard_tag_info_title.setText( getResources().getString(
                            R.string.version_title ) );
                    Intent intent = new Intent( ICardTagInfo.this,
                                                ICardTagCardActivity.class );
                    if ( infos != null && infos.length != 0 ) {
                        String[] copyrights = new String[] { infos[4],
                                infos[6], infos[7], infos[8], infos[9],
                                infos[5], infos[10], infos[11], infos[12],
                                infos[13] };
                        Bundle bundle2 = new Bundle();
                        bundle2.putStringArray( "copyrights", copyrights );
                        intent.putExtras( bundle2 );
                        // TODO
                    }
                    startActivity( intent );
                } else if ( 2 == i ) {
                    icard_tag_info_title.setText( getResources().getString(
                            R.string.text_title ) );
                    // setText("文本名称","个性描述");
                    if ( contents != null && contents.length != 0 ) {
                        setText( contents[2], contents[3] );
                    } else {
                        setText( "", "" );
                    }

                } else if ( 5 == i ) {
                    icard_tag_info_title.setText( getResources().getString(
                            R.string.pic_title ) );
                    // setPic("图片链接","个性描述");
                    if ( contents != null && contents.length != 0 ) {
                        setPerson( contents[1], contents[3] );
                    } else {
                        setPic( "", "" );
                    }

                } else if ( 6 == i ) {
                    icard_tag_info_title.setText( getResources().getString(
                            R.string.person_title ) );
                    // setPerson("个人微博URL","个性描述");
                    if ( contents != null && contents.length != 0 ) {
                        setPic( contents[1], contents[3] );
                    } else {
                        setPerson( "", "" );
                    }
                }
            }
    }

    @Override
    public void onClick( View v ) {
        switch ( v.getId() ) {
            case R.id.design_tag_link_info:
                break;
            case R.id.design_tag_title_info:
                break;
            case R.id.design_tag_desc_info:
                break;
            case R.id.design_tag_video_map_rl_info:
                String text;
                if ( infos != null && infos.length != 0 ) {
                    text = infos[2];
                } else {
                    text = design_tag_video_map_desc_info.getText().toString()
                            .trim();
                }
                Intent intent2 = new Intent( ICardTagInfo.this,
                                             ICardTagVideoMapActivity.class );
                Bundle bundle = new Bundle();
                bundle.putString( "type", substring );
                bundle.putString( "text", text );
                bundle.putInt( "position", position );
                intent2.putExtras( bundle );
                startActivityForResult( intent2,
                                        IstroopConstants.ICARD_DESIGN_TAG_VIDEO_MAP );
                break;
            case R.id.icard_tag_info_cannel:
                finish();
                break;
            case R.id.icard_tag_info_save:
                if ( isClick ) {
                    return;
                } else {
                    isClick = true;
                }
                // TODO
                String link_sure = design_tag_link_exp_info.getText().toString()
                        .trim();
                String title_sure = design_tag_title_exp_info.getText().toString()
                        .trim();
                String desc_sure = design_tag_desc_exp_info.getText().toString()
                        .trim();
                String video_map_sure = design_tag_video_map_desc_info.getText()
                        .toString().trim();
                String temp = "";
                for ( int i = 0; i < types.length; i++ ) {
                    if ( types[i].equals( substring ) ) {
                        if ( i == 1 ) {
                            if ( !TextUtils.isEmpty( link_sure ) ) {
                                if ( Utils.isNotIndex( link_sure ) ) {
                                    isClick = false;
                                    Toast.makeText(
                                            ICardTagInfo.this,
                                            getResources().getString(
                                                    R.string.tag_info_url_error ),
                                            Toast.LENGTH_SHORT ).show();
                                    return;
                                } else {
                                /*
                                 * if(!TextUtils.isEmpty(title_sure)){
								 * if(!TextUtils.isEmpty(desc_sure)){ temp =
								 * "&content[title]="
								 * +title_sure+"&content[url]="+link_sure+
								 * "&content[desc]="+desc_sure+"&type=link";
								 * }else{ Toast.makeText(ICardTagInfo.this,
								 * "请填写标记信息", Toast.LENGTH_SHORT).show();
								 * return; } }else{
								 * Toast.makeText(ICardTagInfo.this, "请填写标记信息",
								 * Toast.LENGTH_SHORT).show(); return; }
								 */
                                    temp = "&content[title]=" + title_sure
                                            + "&content[url]=" + link_sure
                                            + "&content[desc]=" + desc_sure
                                            + "&type=link";
                                }
                            } else {
                                isClick = false;
                                Toast.makeText(
                                        ICardTagInfo.this,
                                        getResources().getString(
                                                R.string.tag_info_url_null ),
                                        Toast.LENGTH_SHORT ).show();
                                return;
                            }
                        } else if ( i == 4 ) {
                            if ( !TextUtils.isEmpty( video_map_sure ) ) {
                                // 进行视频保存操作
                                temp = "&content[title]=" + video_map_sure
                                        + "&content[url]=" + video_link
                                        + "&type=video";
                            } else {
                                isClick = false;
                                Toast.makeText(
                                        ICardTagInfo.this,
                                        getResources().getString(
                                                R.string.tag_info_video_error ),
                                        Toast.LENGTH_SHORT ).show();
                                return;
                            }
                        } else if ( i == 3 ) {
                            if ( !TextUtils.isEmpty( video_map_sure ) ) {
                                // 进行map保存操作
                                String[] split = video_link.split( ":" );
                                if ( split.length == 1 ) {
                                    return;
                                }
                                temp = "&content[title]=" + video_map_sure
                                        + "&content[lat]="
                                        + Integer.parseInt( split[1] ) / 1E6
                                        + "&content[lng]="
                                        + Integer.parseInt( split[0] ) / 1E6
                                        + "&type=map";
                            } else {
                                isClick = false;
                                Toast.makeText(
                                        ICardTagInfo.this,
                                        getResources().getString(
                                                R.string.tag_info_map_error ),
                                        Toast.LENGTH_SHORT ).show();
                                return;
                            }
                        } else if ( i == 0 ) {
                            isClick = false;
                        } else if ( i == 2 ) {
                            if ( !TextUtils.isEmpty( title_sure ) ) {
                                // 进行文字保存操作
                                temp = "&content[title]=" + title_sure
                                        + "&content[desc]=" + desc_sure
                                        + "&type=text";
                            } else {
                                isClick = false;
                                Toast.makeText(
                                        ICardTagInfo.this,
                                        getResources().getString(
                                                R.string.tag_info_title_null ),
                                        Toast.LENGTH_SHORT ).show();
                                return;
                            }
                        } else if ( i == 5 ) {
                            if ( !TextUtils.isEmpty( link_sure ) ) {
                                if ( Utils.isNotIndex( link_sure ) ) {
                                    isClick = false;
                                    Toast.makeText(
                                            ICardTagInfo.this,
                                            getResources().getString(
                                                    R.string.tag_info_url_error ),
                                            Toast.LENGTH_SHORT ).show();
                                    return;
                                } else {
                                    temp = "&content[url]=" + link_sure
                                            + "&content[desc]=" + desc_sure
                                            + "&type=pic";
                                }
                            } else {
                                isClick = false;
                                Toast.makeText(
                                        ICardTagInfo.this,
                                        getResources().getString(
                                                R.string.tag_info_url_null ),
                                        Toast.LENGTH_SHORT ).show();
                                return;
                            }
                        } else if ( i == 6 ) {
                            if ( !TextUtils.isEmpty( link_sure ) ) {
                                if ( Utils.isNotIndex( link_sure ) ) {
                                    isClick = false;
                                    Toast.makeText(
                                            ICardTagInfo.this,
                                            getResources().getString(
                                                    R.string.tag_info_url_error ),
                                            Toast.LENGTH_SHORT ).show();
                                    return;
                                } else {
                                    temp = "&content[url]=" + link_sure
                                            + "&content[desc]=" + desc_sure
                                            + "&type=personage";
                                }
                            } else {
                                isClick = false;
                                Toast.makeText(
                                        ICardTagInfo.this,
                                        getResources().getString(
                                                R.string.tag_info_url_null ),
                                        Toast.LENGTH_SHORT ).show();
                                return;
                            }
                        }
                    }
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
                            message.what = TAG_INFO_ADD_SUCCESS;
                            handler.sendMessage( message );
                        }
                    } else {
                        Message message = Message.obtain();
                        message.what = TAG_INFO_ADD_NULL;
                        handler.sendMessage( message );
                    }
                } catch ( JSONException e ) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = TAG_INFO_ADD_FAIL;
                    handler.sendMessage( message );
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void setPerson( String url, String desc ) {
        icard_tag_line2_info.setVisibility( View.GONE );
        icard_tag_line3_info.setVisibility( View.VISIBLE );
        design_tag_video_map_rl_info.setVisibility( View.GONE );
        design_tag_link_info.setVisibility( View.VISIBLE );
        design_tag_title_info.setVisibility( View.GONE );
        design_tag_desc_info.setVisibility( View.VISIBLE );
        if ( TextUtils.isEmpty( url ) ) {
            // design_tag_link_exp.setText("个人微博URL");
            design_tag_link_exp_info.setText( "" );
            design_tag_link_exp_info.setHint( getResources().getString(
                    R.string.icard_tag_info_person_hint ) );
        } else {
            design_tag_link_exp_info.setText( url );
            design_tag_link_exp_info.setSelection( url.length() );
        }
        if ( TextUtils.isEmpty( desc ) ) {
            // design_tag_desc_exp.setText("个性描述");
            design_tag_desc_exp_info.setText( "" );
            design_tag_desc_exp_info.setHint( R.string.icard_tag_info_desc_hint );
        } else {
            design_tag_desc_exp_info.setText( desc );
            design_tag_desc_exp_info.setSelection( desc.length() );
        }

    }

    private void setPic( String url, String desc ) {
        icard_tag_line2_info.setVisibility( View.GONE );
        icard_tag_line3_info.setVisibility( View.VISIBLE );
        design_tag_video_map_rl_info.setVisibility( View.GONE );
        design_tag_link_info.setVisibility( View.VISIBLE );
        design_tag_title_info.setVisibility( View.GONE );
        design_tag_desc_info.setVisibility( View.VISIBLE );
        if ( TextUtils.isEmpty( url ) ) {
            // design_tag_link_exp.setText("图片链接");
            design_tag_link_exp_info.setText( "" );
            design_tag_link_exp_info.setHint( getResources().getString(
                    R.string.icard_tag_info_url_hint ) );
        } else {
            design_tag_link_exp_info.setText( url );
            design_tag_link_exp_info.setSelection( url.length() );
        }
        if ( TextUtils.isEmpty( desc ) ) {
            // design_tag_desc_exp.setText("个性描述");
            design_tag_desc_exp_info.setText( "" );
            design_tag_desc_exp_info.setHint( getResources().getString(
                    R.string.icard_tag_info_desc_hint ) );
        } else {
            design_tag_desc_exp_info.setText( desc );
            design_tag_desc_exp_info.setSelection( desc.length() );
        }

    }

    private void setText( String title, String desc ) {
        icard_tag_line2_info.setVisibility( View.VISIBLE );
        icard_tag_line3_info.setVisibility( View.VISIBLE );
        design_tag_video_map_rl_info.setVisibility( View.GONE );
        design_tag_link_info.setVisibility( View.GONE );
        design_tag_title_info.setVisibility( View.VISIBLE );
        design_tag_desc_info.setVisibility( View.VISIBLE );
        design_tag_title_tv_info.setText( getResources().getString(
                R.string.tag_info_title ) );
        if ( TextUtils.isEmpty( title ) ) {
            design_tag_title_exp_info.setText( "" );
            design_tag_title_exp_info.setHint( getResources().getString(
                    R.string.tag_info_title ) );
        } else {
            design_tag_title_exp_info.setText( title );
            design_tag_title_exp_info.setSelection( title.length() );
        }
        if ( TextUtils.isEmpty( desc ) ) {
            // design_tag_desc_exp.setText("个性描述");
            design_tag_desc_exp_info.setText( "" );
            design_tag_desc_exp_info.setHint( getResources().getString(
                    R.string.icard_tag_info_desc_hint ) );
        } else {
            design_tag_desc_exp_info.setText( desc );
            design_tag_desc_exp_info.setSelection( desc.length() );
        }
    }

    private void setMap( String title ) {
        icard_tag_line2_info.setVisibility( View.GONE );
        icard_tag_line3_info.setVisibility( View.GONE );
        design_tag_link_info.setVisibility( View.GONE );
        design_tag_title_info.setVisibility( View.GONE );
        design_tag_desc_info.setVisibility( View.GONE );
        design_tag_video_map_rl_info.setVisibility( View.VISIBLE );
        design_tag_title_tv_info.setText( getResources().getString(
                R.string.tag_info_title ) );
        design_tag_video_map_title_info.setText( getResources().getString(
                R.string.tag_info_map ) );
        if ( TextUtils.isEmpty( title ) ) {
            design_tag_video_map_desc_info.setText( "" );
        } else {
            design_tag_video_map_desc_info.setText( title );
        }

    }

    private void setVideo( String title ) {
        icard_tag_line2_info.setVisibility( View.GONE );
        icard_tag_line3_info.setVisibility( View.GONE );
        design_tag_link_info.setVisibility( View.GONE );
        design_tag_title_info.setVisibility( View.GONE );
        design_tag_desc_info.setVisibility( View.GONE );
        design_tag_video_map_rl_info.setVisibility( View.VISIBLE );
        design_tag_title_tv_info.setText( getResources().getString(
                R.string.tag_info_title ) );
        design_tag_video_map_title_info.setText( getResources().getString(
                R.string.tag_info_video ) );
        if ( TextUtils.isEmpty( title ) ) {
            design_tag_video_map_desc_info.setText( "" );
        } else {
            design_tag_video_map_desc_info.setText( title );
        }

    }

    private void setLink( String url, String title, String desc ) {
        icard_tag_line2_info.setVisibility( View.VISIBLE );
        icard_tag_line3_info.setVisibility( View.VISIBLE );
        design_tag_video_map_rl_info.setVisibility( View.GONE );
        design_tag_link_info.setVisibility( View.VISIBLE );
        design_tag_title_info.setVisibility( View.VISIBLE );
        design_tag_desc_info.setVisibility( View.VISIBLE );
        design_tag_title_tv_info.setText( getResources().getString(
                R.string.tag_info_title ) );
        if ( TextUtils.isEmpty( url ) ) {
            // design_tag_link_exp.setText("www.ichaotu.com");
            design_tag_link_exp_info.setText( "" );
            design_tag_link_exp_info.setHint( getResources().getString(
                    R.string.icard_tag_info_url_hint ) );

        } else {
            design_tag_link_exp_info.setText( url );
            design_tag_link_exp_info.setSelection( url.length() );
        }
        if ( TextUtils.isEmpty( title ) ) {
            // design_tag_title_exp.setText("超级图片");
            design_tag_title_exp_info.setText( "" );
            design_tag_title_exp_info.setHint( getResources().getString(
                    R.string.tag_info_title ) );
        } else {
            design_tag_title_exp_info.setText( title );
            design_tag_title_exp_info.setSelection( title.length() );
        }
        if ( TextUtils.isEmpty( title ) ) {
            // design_tag_desc_exp.setText("给你不一样的体验!");
            design_tag_desc_exp_info.setText( "" );
            design_tag_desc_exp_info.setHint( getResources().getString(
                    R.string.icard_tag_info_desc_hint ) );
        } else {
            design_tag_desc_exp_info.setText( desc );
            design_tag_desc_exp_info.setSelection( desc.length() );
        }
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        if ( data == null ) {
            return;
        }
        switch ( requestCode ) {
            case IstroopConstants.ICARD_DESIGN_TAG_DESC:
                design_tag_desc_exp_info.setText( getResultText( data ) );
                break;
            case IstroopConstants.ICARD_DESIGN_TAG_LINK:
                design_tag_link_exp_info.setText( getResultText( data ) );
                break;
            case IstroopConstants.ICARD_DESIGN_TAG_TITLE:
                design_tag_title_exp_info.setText( getResultText( data ) );
                break;
            case IstroopConstants.ICARD_DESIGN_TAG_VIDEO_MAP:
                Bundle extras = data.getExtras();
                if ( extras != null ) {
                    video_link = extras.getString( "tag_video_url",
                                                   "www.ichaotu.com" );
                    String video_title = extras.getString( "tag_video_title",
                                                           "北京市海淀区" );
                    design_tag_video_map_desc_info.setText( video_title );
                }
                break;

            default:
                break;
        }
        super.onActivityResult( requestCode, resultCode, data );
    }

    private String getResultText( Intent data ) {
        Bundle bundle_desc = data.getExtras();
        return bundle_desc.getString( "icardtagdesc", "添加名称" );
    }

    class IcardTagHandler extends Handler {
        @Override
        public void handleMessage( Message msg ) {
            super.handleMessage( msg );
            switch ( msg.what ) {
                case TAG_INFO_ADD_SUCCESS:
                    Toast.makeText( ICardTagInfo.this,
                                    getResources().getString( R.string.save_success ),
                                    Toast.LENGTH_SHORT ).show();
                    Intent intent = new Intent();
                    setResult( RESULT_OK, intent );
                    // TODO 更新列表
                    finish();
                    break;
                case TAG_INFO_ADD_FAIL:
                    // TODO
                    break;
                case TAG_INFO_ADD_NULL:
                    // TODO
                    break;
                default:
                    break;
            }
        }
    }
}
