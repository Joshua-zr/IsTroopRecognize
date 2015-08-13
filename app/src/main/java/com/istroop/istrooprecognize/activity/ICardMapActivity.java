package com.istroop.istrooprecognize.activity;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.istroop.istrooprecognize.BaseActivity;
import com.istroop.istrooprecognize.R;

import java.util.ArrayList;

public class ICardMapActivity extends BaseActivity {
    private static final String TAG = "ICardMapActivity";
    private ArrayList<String> list_address;

    public ICardMapActivity() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            list_address = bundle.getStringArrayList("list");
        }
        TextView icard_map_return = ( TextView ) findViewById( R.id.icard_map_return );
        setContentView(R.layout.icard_map);
        icard_map_return.setOnClickListener( v -> finish() );
        // 对数据库进行查询
        for (int i = 0; i < list_address.size(); i++) {
            String[] location = list_address.get( i ).split( ":" );
            if (location.length > 1) {
                Log.i(TAG, "长度为:" + location.length + location[0] + "经度:"
                        + location[1] + "纬度:" + location[2]);
                final String picUrl = "http://api.map.baidu.com/staticimage?center=116.403874,39.914888&width=300&height=200&zoom=11";
                Log.i(TAG, "图片的url:" + picUrl);
            }
        }
    }
}
