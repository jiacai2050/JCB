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
	// չʾ������
	private String[] texts = new String[] { "��������(��ʱ��)", "��������(����ϵ��)", "�������",
			"����ͨ��¼", "����ͨ��¼", "��ͨ��¼����Evernote", "�����ŵ���Evernote(��ʱ��)", "�����ŵ���Evernote(����ϵ��)"};
	// չʾ��ͼƬ
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

	// ����������ListView��չʾ��һ����Ԫ��View
	public View getView(int position, View convertView, ViewGroup parent) {
		// �Ż�ListView
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
		// �����ı���ͼƬ��Ȼ�󷵻����View������ListView��Item��չʾ
		cache.mTextView.setText(texts[position]);
		cache.mImageView.setImageResource(images[position]);
		return convertView;
	}
}

// Ԫ�صĻ�����,�����Ż�ListView
class ItemViewCache {
	public TextView mTextView;
	public ImageView mImageView;
}
