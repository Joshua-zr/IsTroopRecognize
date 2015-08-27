package com.istroop.istrooprecognize.ui.activity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.TextView;

import com.istroop.istrooprecognize.BaseActivity;
import com.istroop.istrooprecognize.R;
import com.lidroid.xutils.BitmapUtils;

public class RecoDetailMapActivity extends BaseActivity {

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.detail_map );
        TextView leftBtn_map = ( TextView ) findViewById( R.id.leftBtn_map );
        leftBtn_map.setOnClickListener( v -> finish() );
        ImageView iv_map = ( ImageView ) findViewById( R.id.iv_map );

        String titleString = getIntent().getStringExtra( "DB_tag_title" );
        String linkStr = getIntent().getStringExtra( "DB_tag_url" );
        String[] deStrings = getDegreeFromString( linkStr );
        float latitude = Float.parseFloat( deStrings[0] );
        float longitude = Float.parseFloat( deStrings[1] );
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics( dm );
        int widthPixels = dm.widthPixels;
        String uri = "http://api.map.baidu.com/staticimage?center=" +
                latitude + "," + longitude + "&width=" + widthPixels + "&height=" + widthPixels + "&zoom=11&markers=" + latitude + "," + longitude;
        BitmapUtils bitmapUtils = new BitmapUtils( this );
        bitmapUtils.display( iv_map, uri );
//        iv_map.setImageBitmap( bitmap );
        TextView maptitleTextView = ( TextView ) findViewById( R.id.maptitleText );
        maptitleTextView.setText( titleString );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    String[] getDegreeFromString( String aString ) {
        int locLength = "http://api.map.baidu.com/staticimage?center=".length();
        int zoomIndex = aString.indexOf( "&" );
        String degreeString;
        if ( zoomIndex == -1 ) {
            degreeString = aString.substring( locLength, aString.length() );
        } else {
            degreeString = aString.substring( locLength, zoomIndex );
        }
        String[] degreeStrings = degreeString.split( "," );
        if ( degreeStrings.length != 2 ) {
            degreeStrings = degreeString.split( "%2C" );
        }
        return degreeStrings;
    }
}
