package com.ytu.miracle.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import au.com.bytecode.opencsv.CSVReader;

public class PhoneUtils {

	private Context context;

	public PhoneUtils(Context context) {
		this.context = context;
	}

	public String findNameByPhoneNum(String num) {
		ContentResolver resolver = context.getContentResolver();

		Uri uri = Uri
				.parse("content://com.android.contacts/data/phones/filter/"
						+ num);
		Cursor c = resolver.query(uri, new String[] { "display_name" }, null,
				null, null);
		if (c.moveToNext()) {
			return c.getString(0);
		}
		return num;// 没有找到返回手机号
	}
	
	public static int getFileCount(String file) {
		
		if (file.endsWith("xml")) {
			File f = new File(file);
			SAXReader reader = new SAXReader();
				Document document = null;
				try {
					document = reader.read(f);
				} catch (DocumentException e) {
					e.printStackTrace();
				}
				Element root = document.getRootElement();
				return root.elements().size();
		} else if (file.endsWith("csv")) {
			try {
				CSVReader reader = new CSVReader(new InputStreamReader(
						new FileInputStream(file), "GBK"));
				int size = reader.readAll().size();
				reader.close();
				return size;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				Workbook workbook = Workbook.getWorkbook(new File(file));
				int n = workbook.getSheet(0).getRows();
				workbook.close();
				return n - 1; // 减去表头那一行
			} catch (BiffException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
			
		return 0;
	}
}
