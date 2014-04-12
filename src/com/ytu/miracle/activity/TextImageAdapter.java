package com.ytu.miracle.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TextImageAdapter extends BaseAdapter {
	private Context mContext;
	// 展示的文字
	private String[] texts = new String[] { "导出短信(按时间)", "导出短信(按联系人)", "导入短信",
			"导出通信录", "导入通信录", "将通信录导入Evernote", "将短信导入Evernote(按时间)", "将短信导入Evernote(按联系人)"};
	// 展示的图片
	private int[] images = new int[] { R.drawable.exportsms,
			R.drawable.exportsms, R.drawable.importsms,
			R.drawable.exportcontact, R.drawable.importcontact ,R.drawable.evernote
			,R.drawable.evernote,R.drawable.evernote};

	public TextImageAdapter(Context context) {
		this.mContext = context;
	}
	public int getCount() {
		return texts.length;
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	// 用以生成在ListView中展示的一个个元素View
	public View getView(int position, View convertView, ViewGroup parent) {
		// 优化ListView
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.main2list, null);
			ItemViewCache viewCache = new ItemViewCache();
			viewCache.mTextView = (TextView) convertView.findViewById(R.id.txt);
			viewCache.mImageView = (ImageView) convertView
					.findViewById(R.id.img);
			convertView.setTag(viewCache);
		}
		ItemViewCache cache = (ItemViewCache) convertView.getTag();
		// 设置文本和图片，然后返回这个View，用于ListView的Item的展示
		cache.mTextView.setText(texts[position]);
		cache.mImageView.setImageResource(images[position]);
		return convertView;
	}
}

// 元素的缓冲类,用于优化ListView
class ItemViewCache {
	public TextView mTextView;
	public ImageView mImageView;
}
