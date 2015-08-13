package com.istroop.istrooprecognize;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by joshua-zr on 15-4-1.
 */
public class BaseActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApplication.getInstance().addActivity(this);
    }
}