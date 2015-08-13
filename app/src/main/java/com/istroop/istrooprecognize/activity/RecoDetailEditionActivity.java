package com.istroop.istrooprecognize.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.istroop.istrooprecognize.BaseActivity;
import com.istroop.istrooprecognize.IstroopConstants;
import com.istroop.istrooprecognize.R;
import com.istroop.istrooprecognize.utils.ImageAsyncTask;

public class RecoDetailEditionActivity extends BaseActivity {

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.detail_edition );
        TextView leftBtn_edition = ( TextView ) findViewById( R.id.leftBtn_edition );
        leftBtn_edition.setOnClickListener( v -> finish() );
        String nickString = getIntent().getStringExtra( "DB_tag_title" );
        String userinfoString = getIntent().getStringExtra( "DB_tag_url" );
        if ( "copy".equals( nickString ) ) {
            return;
        }
        if ( "copy".equals( userinfoString ) ) {
            return;
        }
        String[] strings = userinfoString.split( "==" );

        TextView nicktTextView = ( TextView ) findViewById( R.id.nickText );
        nicktTextView.setText( nickString );

        ImageView headImageView = ( ImageView ) findViewById( R.id.userhead );
        //imageLoader.displayImage(strings[0], headImageView);
        ImageAsyncTask task = new ImageAsyncTask( RecoDetailEditionActivity.this, headImageView, IstroopConstants.
                mLruCache, new ProgressBar( this ) );
        Bitmap bitmap = task.getBitmapFromMemoryCache( strings[0] );
        if ( bitmap != null ) {
            headImageView.setImageBitmap( bitmap );
        } else {
            task.execute( strings[0] );
        }

        TextView qqTextView = ( TextView ) findViewById( R.id.qqText );
        qqTextView.setText( "Q     Q:" + clearNullWithString( strings[3] ) );
        TextView phoneTextView = ( TextView ) findViewById( R.id.phoneText );
        phoneTextView.setText( "手机号:" + clearNullWithString( strings[2] ) );
        TextView contractTextView = ( TextView ) findViewById( R.id.contractText );
        contractTextView.setText( "联系人:" + clearNullWithString( strings[4] ) );

    }

    String clearNullWithString( String aStr ) {
        if ( aStr.equals( "暂无" ) ) {
            return "";
        } else {
            return aStr;
        }
    }

}
