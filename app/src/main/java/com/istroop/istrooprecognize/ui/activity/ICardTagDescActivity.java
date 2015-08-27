package com.istroop.istrooprecognize.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.istroop.istrooprecognize.BaseActivity;
import com.istroop.istrooprecognize.ColorBarView;
import com.istroop.istrooprecognize.R;

public class ICardTagDescActivity extends BaseActivity implements OnClickListener {

    private static final String TAG = "SignatureActivity";
    private EditText signature_input;
    private TextView design_type_name;
    private String tag_type_item;
    private String tag_type;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.icard_signature);
        init();
    }

    public void init() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        tag_type_item = bundle.getString("tag_type_item", "默认文字");
        tag_type = bundle.getString("tag_type", "链接");
        text = bundle.getString("text");
        ColorBarView design_signature_bar = (ColorBarView) findViewById(R.id.design_signature_bar);
        signature_input = (EditText) findViewById(R.id.signature_input);
        TextView design_signature_cannel = (TextView) findViewById(R.id.design_signature_cannel);
        RelativeLayout design_signature_save = (RelativeLayout) findViewById(R.id.design_signature_save);
        design_type_name = (TextView) findViewById(R.id.design_type_name);
        TextView design_signature_color_text = (TextView) findViewById(R.id.design_signature_color_text);
        setTypeName();
        design_signature_cannel.setOnClickListener(this);
        design_signature_save.setOnClickListener(this);
        design_signature_bar.setVisibility(View.INVISIBLE);
        design_signature_color_text.setVisibility(View.INVISIBLE);
    }

    private void setTypeName() {
        if ("手机壳名称".equals(tag_type_item)) {
            design_type_name.setText("手机壳名称");
            signature_input.setHint("填写手机壳名称,10字内");
            if (text != null) {
                signature_input.setText(text);
                signature_input.setSelection(text.length());
            }
        } else if ("链接".equals(tag_type) && "链接地址".equals(tag_type_item)) {
            design_type_name.setText("链接地址");
            signature_input.setHint("填写任意链接地址,如:www.ichaotu.com");
            if (text != null) {
                signature_input.setText(text);
                signature_input.setSelection(text.length());
            }
        } else if ("链接".equals(tag_type) && "标题".equals(tag_type_item)) {
            design_type_name.setText("标题");
            signature_input.setHint("填写标题");
            if (text != null) {
                signature_input.setText(text);
                signature_input.setSelection(text.length());
            }
        } else if ("链接".equals(tag_type) && "描述".equals(tag_type_item)) {
            design_type_name.setText("描述");
            signature_input.setHint("填写描述");
            if (text != null) {
                signature_input.setText(text);
                signature_input.setSelection(text.length());
            }
        } else if ("人物".equals(tag_type) && "链接地址".equals(tag_type_item)) {
            design_type_name.setText("人物信息");
            signature_input.setHint("填写微博URL");
            if (text != null) {
                signature_input.setText(text);
                signature_input.setSelection(text.length());
            }
        } else if ("人物".equals(tag_type) && "描述".equals(tag_type_item)) {
            design_type_name.setText("人物描述");
            signature_input.setHint("填写人物描述");
            if (text != null) {
                signature_input.setText(text);
                signature_input.setSelection(text.length());
            }
        } else if ("版权".equals(tag_type) && "标题".equals(tag_type_item)) {
            design_type_name.setText("版权所有者");
            signature_input.setHint("填写版权所有者名称");
            if (text != null) {
                signature_input.setText(text);
                signature_input.setSelection(text.length());
            }
        } else if ("文字".equals(tag_type) && "标题".equals(tag_type_item)) {
            design_type_name.setText("文本标题");
            signature_input.setHint("填写标题");
            if (text != null) {
                signature_input.setText(text);
                signature_input.setSelection(text.length());
            }
        } else if ("文字".equals(tag_type) && "描述".equals(tag_type_item)) {
            design_type_name.setText("文本描述");
            signature_input.setHint("填写描述");
            if (text != null) {
                signature_input.setText(text);
                signature_input.setSelection(text.length());
            }
        } else if ("图片".equals(tag_type) && "链接地址".equals(tag_type_item)) {
            design_type_name.setText("图片地址");
            signature_input.setHint("填写图片链接地址");
            if (text != null) {
                signature_input.setText(text);
                signature_input.setSelection(text.length());
            }
        } else if ("图片".equals(tag_type) && "描述".equals(tag_type_item)) {
            design_type_name.setText("图片描述");
            signature_input.setHint("填写描述");
            if (text != null) {
                signature_input.setText(text);
                signature_input.setSelection(text.length());
            }
        } else if ("用户名".equals(tag_type_item)) {
            design_type_name.setText("用户名");
        } else if (getResources().getString(R.string.signature).equals(tag_type_item)) {
            design_type_name.setText(tag_type_item);
            signature_input.setHint(getResources().getString(R.string.signature_input));
            if (text != null) {
                signature_input.setText(text);
                signature_input.setSelection(text.length());
            }
        } else {
            design_type_name.setText("添加文字");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.design_signature_cannel:
                finish();
                break;
            case R.id.design_signature_save:
                String signature = signature_input.getText().toString().trim();
                if (TextUtils.isEmpty(signature)) {
                    Toast.makeText(ICardTagDescActivity.this, "内容不能为空", Toast.LENGTH_SHORT).show();
                    break;
                }
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("icardtagdesc", signature);
                Log.i(TAG, "描述:" + signature);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:
                break;
        }
    }
}
