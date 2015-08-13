package com.istroop.istrooprecognize.utils;

import android.content.Context;
import android.content.Intent;

public class BroadcastHelper {

	public static void sendBroadCast(Context context,String action,String key,int value) {
		Intent intent = new Intent();
		intent.setAction(action);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.putExtra(key, value);
		context.sendBroadcast(intent);
	}

}
