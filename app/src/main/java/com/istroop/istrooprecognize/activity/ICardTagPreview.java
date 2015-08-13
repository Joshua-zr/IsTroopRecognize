package com.istroop.istrooprecognize.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.istroop.istrooprecognize.BaseActivity;
import com.istroop.istrooprecognize.IstroopConstants;
import com.istroop.istrooprecognize.R;
import com.istroop.istrooprecognize.utils.BitmapUtil;
import com.istroop.istrooprecognize.utils.ImageAsyncTask;

import java.util.Arrays;

public class ICardTagPreview extends BaseActivity implements OnClickListener {
    private static final String TAG = "ICardTagPreview2";
    private TextView icard_tag_preview2_name;
    private TextView icard_tag_preview2_job;
    private TextView icard_tag_preview2_company;
    private TextView icard_tag_preview2_index;
    private TextView icard_tag_preview2_weixin;
    private TextView icard_tag_preview2_mobile;
    private TextView icard_tag_preview2_mail;

    private String[] cardInfos;
    private String headurl;
    private ImageView icard_tag_preview2_head;
    // private ImageButton icard_tag_preview2_more2;
    private TextView icard_tag_preview2_address2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.icard_tag_card_preview);
        init();
    }

    public void init() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            cardInfos = bundle.getStringArray("cardInfos");
            Log.i(TAG, cardInfos != null ? Arrays.toString( cardInfos ) : null );
            headurl = bundle.getString("headurl");
            Log.i(TAG, "头像信息:" + headurl);
        }
        icard_tag_preview2_address2 = (TextView) findViewById(R.id.icard_tag_preview2_address2);
        TextView icard_tag_preview2_cannel2 = (TextView) findViewById(R.id.icard_tag_preview2_cannel2);
        icard_tag_preview2_head = (ImageView) findViewById(R.id.icard_tag_preview2_head);
        icard_tag_preview2_name = (TextView) findViewById(R.id.icard_tag_preview2_name);
        icard_tag_preview2_job = (TextView) findViewById(R.id.icard_tag_preview2_job);
        icard_tag_preview2_company = (TextView) findViewById(R.id.icard_tag_preview2_company);
        icard_tag_preview2_index = (TextView) findViewById(R.id.icard_tag_preview2_index);
        icard_tag_preview2_weixin = (TextView) findViewById(R.id.icard_tag_preview2_weixin);
        icard_tag_preview2_mobile = (TextView) findViewById(R.id.icard_tag_preview2_mobile);
        icard_tag_preview2_mail = (TextView) findViewById(R.id.icard_tag_preview2_mail);

        ImageButton icard_tag_preview2_mobiel_ing = (ImageButton) findViewById(R.id.icard_tag_preview2_mobiel_ing);
        ImageButton icard_tag_preview2_sms_ing = (ImageButton) findViewById(R.id.icard_tag_preview2_sms_ing);
        ImageButton icard_tag_preview2_mail_ing = (ImageButton) findViewById(R.id.icard_tag_preview2_mail_ing);

        TextView icard_tag_preview2_rightTv = (TextView) findViewById(R.id.icard_tag_preview2_rightTv);
        icard_tag_preview2_rightTv.setOnClickListener(this);
        icard_tag_preview2_cannel2.setOnClickListener( v -> finish() );
        icard_tag_preview2_mobiel_ing.setOnClickListener(this);
        icard_tag_preview2_sms_ing.setOnClickListener(this);
        icard_tag_preview2_mail_ing.setOnClickListener(this);
        initCard();
    }

    private void initCard() {
        if (cardInfos.length > 0) {
            icard_tag_preview2_name.setText(cardInfos[0]);
        }
        if (cardInfos.length > 1) {
            icard_tag_preview2_company.setText(cardInfos[1]);
        }
        if (cardInfos.length > 2) {
            icard_tag_preview2_job.setText(cardInfos[2]);
        }
        if (cardInfos.length > 3) {
            icard_tag_preview2_index.setText(cardInfos[3]);
        }
        if (cardInfos.length > 4) {
            icard_tag_preview2_mail.setText("E:" + cardInfos[4]);
        }
        if (cardInfos.length > 5) {
            icard_tag_preview2_mobile.setText("M:" + cardInfos[5]);
        }
        if (cardInfos.length > 6) {
            icard_tag_preview2_weixin.setText("W:" + cardInfos[6]);
        }
        if (cardInfos.length > 7) {
            icard_tag_preview2_address2.setText("A:" + cardInfos[7]);
        }
        if (headurl != null) {
            loadImage(icard_tag_preview2_head,
                    "http://tstatics.tujoin.com/print.php?w=140&h=140&t=c&url="
                            + headurl);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        /*
         * case R.id.icard_tag_preview2_more2: popupWindow.showAsDropDown(v, 0,
		 * 10); break;
		 */
            case R.id.icard_tag_preview2_mobiel_ing:
                Intent dailIntent = new Intent();
                dailIntent.setAction(Intent.ACTION_CALL);
                dailIntent.setData(Uri.parse("tel:" + cardInfos[5]));
                startActivity(dailIntent);
                break;
            case R.id.icard_tag_preview2_sms_ing:
                Uri uri = Uri.parse("smsto:" + cardInfos[5]);
                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                startActivity(intent);
                break;
            case R.id.icard_tag_preview2_mail_ing:
                Uri uri_mail = Uri.parse("mailto:" + cardInfos[4]);
                Intent it = new Intent(Intent.ACTION_SENDTO, uri_mail);
                startActivity(it);
                break;
            case R.id.icard_tag_preview2_rightTv:
                saveTo();
                break;
            default:
                finish();
                break;
        }
    }

    private void saveTo() {
        Intent it = new Intent(Contacts.Intents.Insert.ACTION);
        it.setType(Contacts.People.CONTENT_TYPE);
        it.putExtra(Contacts.Intents.Insert.NAME, cardInfos[0]);
        it.putExtra(Contacts.Intents.Insert.PHONE, cardInfos[5]);
        it.putExtra(Contacts.Intents.Insert.EMAIL, cardInfos[4]);
        it.putExtra(Contacts.Intents.Insert.COMPANY, cardInfos[1]);
        startActivity(it);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void loadImage(ImageView my_info_head_exp, String picUrl) {
        ImageAsyncTask task = new ImageAsyncTask(this,
                IstroopConstants.mLruCache);
        Bitmap bitmap = task.getBitmapFromMemoryCache(picUrl);
        Bitmap cache = task.getBitmapFileCache(picUrl);
        if (bitmap != null) {
            my_info_head_exp.setImageBitmap(BitmapUtil.getCircleBitmap(bitmap));
        } else if (cache != null) {
            my_info_head_exp.setImageBitmap(BitmapUtil.getCircleBitmap(cache));
        } else {
            Bitmap circleBitmap = BitmapUtil.getCircleBitmap(BitmapFactory
                    .decodeResource(getResources(), R.drawable.default_head));
            my_info_head_exp.setImageBitmap(circleBitmap);
            task.execute(picUrl);
        }
    }

}
