package com.ytu.miracle.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class Setting extends Activity {

	private String smsType = "excel";
	private String contactType = "excel";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);

		Button ok = (Button) findViewById(R.id.ok);
		RadioGroup sms = (RadioGroup) findViewById(R.id.sms);
		RadioGroup contact = (RadioGroup) findViewById(R.id.contact);
		
		smsType = getIntent().getStringExtra("smsType");
		contactType = getIntent().getStringExtra("contactType");

		if (smsType.equals("excel")) {
			RadioButton rb = (RadioButton) sms.getChildAt(0);
			rb.setChecked(true);
		} else {
			RadioButton rb = (RadioButton) sms.getChildAt(1);
			rb.setChecked(true);
		}
		if (contactType.equals("excel")) {
			RadioButton rb = (RadioButton) contact.getChildAt(0);
			rb.setChecked(true);
		} else if (contactType.equals("xml")) {
			RadioButton rb = (RadioButton) contact.getChildAt(1);
			rb.setChecked(true);
		} else {
			RadioButton rb = (RadioButton) contact.getChildAt(2);
			rb.setChecked(true);
		}
		ok.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("smsType", smsType);
				intent.putExtra("contactType", contactType);
				setResult(2011, intent);
				finish();
			}
		});
		sms.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.xml) {
					smsType = "xml";
				} else {
					smsType = "excel";
				}
			}
		});

		contact.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.xml:
					contactType = "xml";
					break;
				case R.id.csv:
					contactType = "csv";
					break;
				case R.id.excel:
					contactType = "excel";
					break;
				}

			}
		});
	}
}
