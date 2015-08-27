package com.istroop.istrooprecognize.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.istroop.istrooprecognize.BaseActivity;
import com.istroop.istrooprecognize.R;

public class ICardRegisterActivity extends BaseActivity implements OnClickListener {

    private static final int ICARD_REGISTER2_MOBILE = 1;
    private static final int ICARD_REGISTER2_MAIL = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actionsheet);
        init();
    }

    public void init() {
        Button design_head_photo = (Button) findViewById(R.id.design_head_photo);
        Button design_head_album = (Button) findViewById(R.id.design_head_album);
        Button design_head_quit = (Button) findViewById(R.id.design_head_quit);
        design_head_photo.setText(getResources().getString(R.string.icard_login_register_mobile));
        design_head_album.setVisibility(View.GONE);
        design_head_quit.setText(getResources().getString(R.string.design_headimage_quit));
        design_head_quit.setOnClickListener(this);
        design_head_photo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.design_head_photo:
                Intent intent = new Intent(ICardRegisterActivity.this, ICardRegistreMobileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("mobile_findpwd", "超级图片");
                intent.putExtras(bundle);
                startActivityForResult(intent, ICARD_REGISTER2_MOBILE);
                break;
            case R.id.design_head_quit:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case ICARD_REGISTER2_MOBILE:
                setResult(RESULT_OK, data);
                finish();
                break;
            case ICARD_REGISTER2_MAIL:
                setResult(RESULT_OK, data);
                finish();
                break;
            default:
                break;
        }
    }
}
