package com.ytu.miracle.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.ytu.miracle.util.FileChooseAdapter;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class FileChooseDialog extends ListActivity {
	private List<String> items = null;
	private List<String> paths = null;
	private String rootPath = "/";
	private String curPath = "/";
	private TextView mPath;

	private View oldItem = null;// 上一个选中的项目

	private boolean isFile = false;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.fileselect);
		mPath = (TextView) findViewById(R.id.mPath);
		Button buttonConfirm = (Button) findViewById(R.id.buttonConfirm);
		buttonConfirm.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent data = new Intent();
				String type = data.getStringExtra("type");
				data.putExtra("file", curPath);
				Log.i("file", curPath);
				if ("contact".equals(type)) {
					setResult(2012, data);
				} else {
					setResult(2013, data);
				}
				if (isFile) {
					finish();
				}

			}
		});
		Button buttonCancle = (Button) findViewById(R.id.buttonCancle);
		buttonCancle.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent data = new Intent();
				String type = data.getStringExtra("type");
				data.putExtra("file", "blank"); // 如果如果点击了取消按钮时设置file为blank
				if ("contact".equals(type)) {
					setResult(2012, data);
				} else {
					setResult(2013, data);
				}
				finish();
			}
		});
		getFileDir(rootPath);
	}

	private void getFileDir(String filePath) {
		mPath.setText(filePath);
		items = new ArrayList<String>();
		paths = new ArrayList<String>();
		File f = new File(filePath);
		File[] files = f.listFiles();

		if (!filePath.equals(rootPath)) {
			items.add("b1");
			paths.add(rootPath);
			items.add("b2");
			paths.add(f.getParent());
		}
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				// if (file.isDirectory()) {
				items.add(file.getName());
				paths.add(file.getPath());
				// }
			}
		}

		setListAdapter(new FileChooseAdapter(this, items, paths));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		File file = new File(paths.get(position));
		if (file.isDirectory()) {
			curPath = paths.get(position);
			getFileDir(paths.get(position));
			isFile = false;
		} else {
			isFile = true;
			if (oldItem != null) {
				oldItem.setBackgroundColor(Color.BLACK);
			}
			v.setBackgroundColor(Color.rgb(125, 38, 205));
			oldItem = v;
			curPath = file.getAbsolutePath();
			// Intent data = new Intent(FileChooseDialog.this,
			// MainActivity.class);
			// Bundle bundle = new Bundle();
			// bundle.putString("file", curPath);
			// Log.i("file", curPath);
			// data.putExtras(bundle);
			// setResult(2, data);
			// finish();
		}

	}

}