package com.ytu.miracle.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

public class Tip extends Activity {

	private CheckBox box;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tip);
		Button ok = (Button) findViewById(R.id.ok);
		box = (CheckBox) findViewById(R.id.again);
		ok.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Log.i("box", String.valueOf(box.isChecked()));
				if (box.isChecked()) {
					SharedPreferences.Editor editor = getSharedPreferences(
							"tip", MODE_PRIVATE).edit();
					// ��������е�ֵ
					editor.putString("tip", "false");
					// �ύ����Ľ��
					editor.commit();
				}
				finish();
			}
		});
//		this.setFinishOnTouchOutside(false);
	}
}
