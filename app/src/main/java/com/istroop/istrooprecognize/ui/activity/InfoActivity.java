package com.istroop.istrooprecognize.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.istroop.istrooprecognize.BaseActivity;
import com.istroop.istrooprecognize.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class InfoActivity extends BaseActivity {
	
	public String DB_fileurl;
	public String DB_tag_title;
	public String DB_tag_url;
	public String DB_tag_desc;
	public int DB_tag_type;

	private TextView info_tv;
	private int wmid;
	private String result;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info);
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if(extras != null){
			wmid = extras.getInt("wmid");
			result = extras.getString("result");
		}
		info_tv = (TextView) findViewById(R.id.info_tv);
		json();
	}
	private void json() {
		try {
			JSONObject resultObject=new JSONObject(result);
			boolean success=resultObject.getBoolean("success");
			//{"data":{"3596":"无权限访问的超图"},"success":true}resultObject
			if (success) {
				JSONObject temObject=resultObject.getJSONObject("data");
				if(temObject.optJSONObject(wmid+"") == null){
					String data = temObject.getString(wmid+"");
					Toast.makeText(InfoActivity.this,data,Toast.LENGTH_SHORT).show();
					return;
				}
				JSONObject dataObject = temObject.getJSONObject(wmid+"");
				DB_fileurl=dataObject.getString("fileid");
				if(dataObject.optJSONObject("tags")==null){
					Toast.makeText(InfoActivity.this,"图片没有更多的信息!",Toast.LENGTH_SHORT).show(); 
					return;
				}
				JSONObject tagObject=dataObject.getJSONObject("tags");
				Iterator keys = tagObject.keys();
				JSONObject contentObject;
				ArrayList<String> arr = new ArrayList<>();
				while(keys.hasNext()) {
					String key = (String) keys.next();
					arr.add(key);
				}
				JSONObject jsonObject = tagObject.getJSONObject(arr.get(arr.size()-1));
				String typeString = jsonObject.getString("type");
				contentObject = jsonObject.getJSONObject("content");
				DB_tag_type=tagTypewithString(typeString);
				if (DB_tag_type==3) {//copyright
					if(contentObject.length() == 4){
						String url = contentObject.getString("url");
						String title = contentObject.getString("title");
						String desc = contentObject.getString("desc");
						String uname = contentObject.getString("uname");
						info_tv.setText("链接:"+url+"\n"+"标题:"+title+"\n"+"描述:"+desc
								+"\n"+"用户名:"+uname);
					}else if(contentObject.length() !=10 && contentObject.length() !=9){
						String realname = contentObject.getString("realname");
						String company = contentObject.getString("company");
						String job = contentObject.getString("job");
						String companyUrl = contentObject.getString("companyUrl");
						String email = contentObject.getString("email");
						String phone = contentObject.getString("phone");
						String weixin = contentObject.getString("weixin");
						String introduce = contentObject.getString("introduce");
						DB_tag_title = realname;
						DB_tag_url=company+"=="+job+"=="+companyUrl+"=="+email+
								"=="+phone+"=="+weixin+"=="+introduce;
						DB_tag_desc = "copyright";
						info_tv.setText("姓名:"+realname+"\n"+"电话:"+phone+"\n"+"邮箱:"+email+"\n"
								+"公司:"+company+"\n"+"工作:"+job+"\n"+"公司网址:"+companyUrl+"\n"
								+"微信:"+weixin+"\n"+"个性签名:"+introduce);
					}else{
						String name=contentObject.getString("name");
						String phone=contentObject.getString("phone");					
						String mail=contentObject.getString("mail");
						String company=contentObject.getString("company");
						String department=contentObject.getString("department");
						String position=contentObject.getString("position");
						String companyweb=contentObject.getString("companyweb");
						String address=contentObject.getString("address");
						String sign=contentObject.getString("sign");
						String weixin=contentObject.getString("weixin");
						DB_tag_title = name;
						DB_tag_url=phone+"=="+mail+"=="+company+"=="+department+
								"=="+position+"=="+companyweb+"=="+address+"=="+sign+"=="+weixin;
						DB_tag_desc = "";
						info_tv.setText("姓名:"+name+"\n"+"电话:"+phone+"\n"+"邮箱:"+mail+"\n"
								+"公司:"+company+"\n"+"部门:"+department+"\n"+"职务:"+position+"\n"
								+"地址:"+address+"\n"+"公司网址:"+companyweb+"\n"+"微信:"+weixin+"\n"
								+"个性签名:"+sign);
					}
				}else if (DB_tag_type==4||DB_tag_type==5) {//pic personage
					DB_tag_url=contentObject.getString("url");
					DB_tag_title=contentObject.getString("desc");
					DB_tag_desc=contentObject.getString("desc");
					info_tv.setText("链接:"+DB_tag_url+"\n"+"标题:"+DB_tag_title);
				}else if(DB_tag_type==8){//shopping
					info_tv.setText("链接:"+DB_tag_url);
				}else if(DB_tag_type==0){//text
					DB_tag_title=contentObject.getString("title");
					DB_tag_desc=contentObject.getString("desc");
					info_tv.setText("标题:"+DB_tag_title+"\n"+"描述:"+DB_tag_desc);
				}else{//link
					DB_tag_url=contentObject.getString("url");
					DB_tag_title=contentObject.getString("title");
					if(!contentObject.isNull("desc")){
						DB_tag_desc=contentObject.getString("desc");
					}else{
						DB_tag_desc = "";
					}
					info_tv.setText("链接:"+DB_tag_url+"\n"+"标题:"+DB_tag_title+"\n"+"描述:"+DB_tag_desc);
				}

			}else {
				Toast.makeText(InfoActivity.this, "error", Toast.LENGTH_SHORT).show();
			}
		} catch (JSONException e) {
			Toast.makeText(InfoActivity.this, "error", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
	
	public int tagTypewithString(String aString) {
		if ("text".equals(aString)) {
			return 0;
		}else if ("link".equals(aString))
		{
			return 1;

		}else if ("music".equals(aString))
		{
			return 2;
		}
		else if ("copyright".equals(aString))
		{
			return 3;

		}else if ("pic".equals(aString))
		{
			return 4;

		}else if ("personage".equals(aString))
		{
			return 5;
		}else if ("video".equals(aString))
		{
			return 6;
		}
		else if ("map".equals(aString)) {
			return 7;
		}else if("shopping".equals(aString)){
			return 8;
		}else {
			return -1;
		}


	}
}
