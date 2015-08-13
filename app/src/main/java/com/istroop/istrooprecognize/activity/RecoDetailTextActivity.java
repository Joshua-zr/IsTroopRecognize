package com.istroop.istrooprecognize.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.istroop.istrooprecognize.BaseActivity;
import com.istroop.istrooprecognize.R;

public class RecoDetailTextActivity extends BaseActivity {

    private TextView leftBtn_text;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.detail_text );
        leftBtn_text = ( TextView ) findViewById( R.id.leftBtn_text );

        String titleString = ( String ) getIntent().getSerializableExtra( "DB_tag_title" );
        String descString = ( String) getIntent().getSerializableExtra("DB_tag_desc");

        TextView titleTextView = (TextView) findViewById(R.id.detail_title);
        titleTextView.setText(titleString);

        TextView descTextView = (TextView) findViewById(R.id.descText);
        descTextView.setText(descString);
        leftBtn_text.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
