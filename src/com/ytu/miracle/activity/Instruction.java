package com.ytu.miracle.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Instruction extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.instruction);
		Button ok = (Button) findViewById(R.id.ok);
		ok.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				finish();
			}
		});
	}
}
