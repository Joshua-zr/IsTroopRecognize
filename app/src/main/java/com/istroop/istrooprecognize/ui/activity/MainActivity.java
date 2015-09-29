package com.istroop.istrooprecognize.ui.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;

import com.istroop.istrooprecognize.IstroopConstants;
import com.istroop.istrooprecognize.R;
import com.istroop.istrooprecognize.utils.Utils;
import com.umeng.analytics.MobclickAgent;

@SuppressWarnings( "deprecation" )
public class MainActivity extends TabActivity {
    private static final   String HOME    = "Home";
    private static final   String HISTORY = "History";
    protected static final String TAG     = "MainActivity";
    public static TabHost tabHost;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        tabHost = this.getTabHost();
        View view1 = View.inflate( MainActivity.this, R.layout.tab, null );
        ( ( ImageView ) view1.findViewById( R.id.tab_imageview_icon ) )
                .setImageResource( R.drawable.tab_reco );
        TabHost.TabSpec spec1 = tabHost.newTabSpec( HOME ).setIndicator( view1 )
                .setContent( new Intent( this, RecoActivity.class ) );
        tabHost.addTab( spec1 );

        View view2 = View.inflate( MainActivity.this, R.layout.tab, null );
        ( ( ImageView ) view2.findViewById( R.id.tab_imageview_icon ) )
                .setImageResource( R.drawable.tab_his );
        TabHost.TabSpec spec2 = tabHost.newTabSpec( HISTORY ).setIndicator( view2 )
                .setContent( new Intent( this, HistoryActivity.class ) );
        tabHost.addTab( spec2 );

        final View childAt_0 = tabHost.getTabWidget().getChildAt( 0 );
        final View childAt_1 = tabHost.getTabWidget().getChildAt( 1 );

        childAt_0.getLayoutParams().height = Utils.dip2px( this, 50 );
        childAt_1.getLayoutParams().height = Utils.dip2px( this, 50 );

        childAt_1.setOnClickListener( v -> {
            if ( IstroopConstants.isLogin ) {
                tabHost.setCurrentTab( 1 );
            } else {
                Intent intent = new Intent( MainActivity.this,
                                            FastLoginActivity.class );
                startActivity( intent );
            }
        } );
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume( this );
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onResume( this );
    }

}
