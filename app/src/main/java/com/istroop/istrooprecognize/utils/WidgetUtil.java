package com.istroop.istrooprecognize.utils;

import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

public class WidgetUtil {
	public static void invisible(EditText et,final View view){
		et.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){
					view.setVisibility(View.INVISIBLE);
				}
			}
		});
	}
}
