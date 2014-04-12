package com.ytu.miracle.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.widget.Toast;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.OnClientCallback;
import com.evernote.thrift.transport.TTransportException;

public class ContactUtils {

	private ContentResolver cr;
	private Context context;
	private ArrayList<ContentProviderOperation> ops;
	private int rawContactInsertIndex;

	private String[] phoneType = { "custom", "home", "mobile", "work", "other" };
	private String[] emailType = { "custom", "home", "work", "other", "mobile" };

	private ProgressDialog dialog;

	private int row = 1;
	private int col = 0;

	public ContactUtils(Context context, ProgressDialog dialog) {
		this.context = context;
		this.cr = context.getContentResolver();

		this.dialog = dialog;
	}

	public void exportContactXML(Cursor cur) {
		String path = Environment.getExternalStorageDirectory().getPath();
		Calendar c = Calendar.getInstance();
		String fileName = "通信录备份" + c.get(Calendar.YEAR) + "年"
				+ (c.get(Calendar.MONTH) + 1) + "月" + c.get(Calendar.DATE)
				+ "日" + c.get(Calendar.HOUR_OF_DAY) + "-"
				+ c.get(Calendar.MINUTE) + "-" + c.get(Calendar.SECOND)
				+ ".xml";
		File contact = new File(path + "/" + fileName);
		if (!contact.exists()) {
			try {
				contact.createNewFile();
				Document document = DocumentHelper.createDocument();
				document.addElement("all").addAttribute("number", "0");
				XMLWriter output = new XMLWriter(new FileWriter(contact)); // 保存文档
				output.write(document);
				output.close();
			} catch (IOException e) {
				Log.i("myerror", "message文件创建失败");
				e.printStackTrace();
			}
		}
		int progress = 0;
		while (cur.moveToNext()) {

			dialog.setProgress(++progress);
			SAXReader reader = new SAXReader();
			Document document = null;
			try {
				document = reader.read(contact);
			} catch (DocumentException e2) {
				e2.printStackTrace();
			}
			Element root = document.getRootElement();

			root.attribute("number").setValue(String.valueOf(cur.getCount()));// number

			Element contactElement = root.addElement("contact");

			Element idAttr = contactElement.addElement("id");
			idAttr.setText(String.valueOf(cur.getPosition() + 1));

			Element nameAttr = contactElement.addElement("name");
			String name = cur.getString(cur.getColumnIndex("display_name"));
			nameAttr.setText(name);

			// 获得联系人的ID号
			String contactId = cur.getString(cur.getColumnIndex("_id"));
			// 查看该联系人有多少个电话号码。如果没有这返回值为0
			int phoneCount = cur
					.getInt(cur
							.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
			if (phoneCount > 0) {
				// 获得联系人的电话号码
				Cursor cursor = cr.query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID
								+ " = " + contactId, null, null);
				if (cursor.moveToFirst()) {
					do {
						// 遍历所有的电话号码
						String phoneNumber = cursor
								.getString(cursor
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						String phoneTypeNum = cursor
								.getString(cursor
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
						int index = Integer.valueOf(phoneTypeNum);
						if (index > 4) {
							index = 0;
						}
						// phones.add(phoneType[index] + ":" + phoneNumber);
						Element phoneAttr = contactElement.addElement("phone");
						phoneAttr.addAttribute("type", phoneType[index]);
						phoneAttr.setText(phoneNumber);
					} while (cursor.moveToNext());

				}
				cursor.close();
			}

			// 获取该联系人邮箱
			Cursor cursor = cr.query(
					ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
							+ contactId, null, null);
			if (cursor.moveToFirst()) {
				do {
					// 遍历所有的email
					String emailTypeNum = cursor
							.getString(cursor
									.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
					String emailValue = cursor
							.getString(cursor
									.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
					// emails.add(emailType[Integer.valueOf(emailTypeNum)] + ":"
					// + emailValue);
					Element emailAttr = contactElement.addElement("email");
					emailAttr.addAttribute("type",
							emailType[Integer.valueOf(emailTypeNum)]);
					emailAttr.setText(emailValue);
				} while (cursor.moveToNext());

			}
			cursor.close();

			// 获取该联系人地址
			cursor = cr
					.query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " = " + contactId, null, null);
			if (cursor.moveToFirst()) {
				do {
					// 遍历所有的地址
					String street = cursor
							.getString(cursor
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
					String city = cursor
							.getString(cursor
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
					String country = cursor
							.getString(cursor
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
					if (country == null) {
						country = "";
					}
					if (street == null) {
						street = "";
					}
					if (city == null) {
						city = "";
					}
					// address.add(country + city + street);
					Element addressAttr = contactElement.addElement("address");
					addressAttr.setText(country + city + street);
				} while (cursor.moveToNext());
			}
			cursor.close();
			// 获取备注信息
			cursor = cr.query(Data.CONTENT_URI, new String[] { Data._ID,
					Note.NOTE }, Data.CONTACT_ID + "=?" + " AND "
					+ Data.MIMETYPE + "='" + Note.CONTENT_ITEM_TYPE + "'",
					new String[] { contactId }, null);
			if (cursor.moveToFirst()) {
				do {
					String noteinfo = cursor.getString(cursor
							.getColumnIndex(Note.NOTE));
					Element noteAttr = contactElement.addElement("note");
					noteAttr.setText(noteinfo);
				} while (cursor.moveToNext());

			}
			cursor.close();

			// document.setRootElement(root);
			/** 格式化输出，类型IE浏览一样 */
			OutputFormat format = OutputFormat.createPrettyPrint();
			/** 指定XML编码 */
			format.setEncoding("UTF-8");
			try {
				XMLWriter writer = new XMLWriter(new FileOutputStream(contact),
						format);
				writer.write(document);
				writer.close();
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		cur.close();
		dialog.dismiss();

	}

	public void exportContactCSV(Cursor cur) {
		String path = Environment.getExternalStorageDirectory().getPath();
		Calendar c = Calendar.getInstance();
		String fileName = "通信录备份" + c.get(Calendar.YEAR) + "年"
				+ (c.get(Calendar.MONTH) + 1) + "月" + c.get(Calendar.DATE)
				+ "日" + c.get(Calendar.HOUR_OF_DAY) + "-"
				+ c.get(Calendar.MINUTE) + "-" + c.get(Calendar.SECOND)
				+ ".csv";
		String[] fields = new String[5];
		CSVWriter writer = null;
		try {
			writer = new CSVWriter(new FileWriter(path + "/" + fileName));
		} catch (IOException e1) {
			e1.printStackTrace();
			Log.i("writer", "CSVWriter构造失败了");
		}
		int progress = 0;
		while (cur.moveToNext()) {
			String name = cur.getString(cur.getColumnIndex("display_name"));
			fields[0] = name;

			// 获得联系人的ID号
			String contactId = cur.getString(cur.getColumnIndex("_id"));
			// 查看该联系人有多少个电话号码。如果没有这返回值为0
			int phoneCount = cur
					.getInt(cur
							.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
			if (phoneCount > 0) {
				fields[1] = getPhone(contactId);
			} else {
				fields[1] = "";
			}
			// 获取该联系人邮箱
			fields[2] = getEmail(contactId);
			// 获取该联系人地址
			fields[3] = getAddress(contactId);
			// 获取备注信息
			fields[4] = getNote(contactId);

			writer.writeNext(fields);
			dialog.setProgress(++progress);
		}
		cur.close();
		dialog.dismiss();
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void exportContactExcel(Cursor cur) throws Exception {
		String path = Environment.getExternalStorageDirectory().getPath();
		Calendar c = Calendar.getInstance();
		String fileName = "通信录备份" + c.get(Calendar.YEAR) + "年"
				+ (c.get(Calendar.MONTH) + 1) + "月" + c.get(Calendar.DATE)
				+ "日" + c.get(Calendar.HOUR_OF_DAY) + "-"
				+ c.get(Calendar.MINUTE) + "-" + c.get(Calendar.SECOND)
				+ ".xls";
		WritableWorkbook book = Workbook.createWorkbook(new File(path + "/"
				+ fileName));
		WritableSheet sheet = book.createSheet("第一页 ", 0);
		String[] cont = { "姓名", "电话", "邮箱", "地址", "备注" };
		for (int i = 0; i < 5; i++) {
			sheet.addCell(new Label(i, 0, cont[i]));
		}
		int progress = 0;
		while (cur.moveToNext()) {
			String name = cur.getString(cur.getColumnIndex("display_name"));
			sheet.addCell(new Label(col++, row, name));
			// 获得联系人的ID号
			String contactId = cur.getString(cur.getColumnIndex("_id"));
			// 查看该联系人有多少个电话号码。如果没有这返回值为0
			int phoneCount = cur
					.getInt(cur
							.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
			if (phoneCount > 0) {
				sheet.addCell(new Label(col++, row, getPhone(contactId)));
			} else {
				sheet.addCell(new Label(col++, row, ""));
			}

			// 获取该联系人邮箱
			sheet.addCell(new Label(col++, row, getEmail(contactId)));
			// 获取该联系人地址
			sheet.addCell(new Label(col++, row, getAddress(contactId)));
			// 获取备注信息
			sheet.addCell(new Label(col++, row, getNote(contactId)));

			row++; // 添加新的一行
			col = 0;
			dialog.setProgress(++progress);
		} // cursor遍历完毕
		cur.close();
		dialog.dismiss();

		book.write();
		book.close();

	}

	public void exportContactEvernote(Cursor cur,
			EvernoteSession mEvernoteSession) {
		com.evernote.edam.type.Note note = new com.evernote.edam.type.Note();
		Calendar c = Calendar.getInstance();
		String fileName = "通信录备份" + c.get(Calendar.YEAR) + "年"
				+ (c.get(Calendar.MONTH) + 1) + "月" + c.get(Calendar.DATE)
				+ "日" + c.get(Calendar.HOUR_OF_DAY) + "-"
				+ c.get(Calendar.MINUTE) + "-" + c.get(Calendar.SECOND);
		note.setTitle(fileName);
		StringBuilder msg = new StringBuilder();
		while (cur.moveToNext()) {
			msg.append("<p>");
			String name = cur.getString(cur.getColumnIndex("display_name"));
			msg.append("姓名:" + name);
			// 获得联系人的ID号
			String contactId = cur.getString(cur.getColumnIndex("_id"));
			// 查看该联系人有多少个电话号码。如果没有这返回值为0
			int phoneCount = cur
					.getInt(cur
							.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
			if (phoneCount > 0) {
				msg.append("&nbsp;&nbsp;&nbsp;&nbsp;电话:" + getPhone(contactId));
			} 

			// 获取该联系人邮箱
			String emails = getEmail(contactId);
			if (""!=emails) {
				msg.append("&nbsp;&nbsp;&nbsp;&nbsp;邮箱:" + emails);	
			}
			
			// 获取该联系人地址
			String addresses = getAddress(contactId);
			if(""!=addresses)
			msg.append("&nbsp;&nbsp;&nbsp;&nbsp;地址:" + addresses);
			// 获取备注信息
			String notes = getNote(contactId);
			if(""!=notes)
			msg.append("&nbsp;&nbsp;&nbsp;&nbsp;备注:" + notes);

			msg.append("</p>");
		}
		cur.close();
		note.setContent(EvernoteUtil.NOTE_PREFIX + msg.toString()
				+ EvernoteUtil.NOTE_SUFFIX);
		try {
			mEvernoteSession
					.getClientFactory()
					.createNoteStoreClient()
					.createNote(
							note,
							new OnClientCallback<com.evernote.edam.type.Note>() {
								@Override
								public void onException(Exception exception) {
									Toast.makeText(context, "保存失败",
											Toast.LENGTH_LONG).show();
									dialog.dismiss();
								}

								@Override
								public void onSuccess(
										com.evernote.edam.type.Note data) {
									// TODO Auto-generated method stub
									dialog.dismiss();
									Toast.makeText(context, "通信录成功导入到Evernote中",
											Toast.LENGTH_LONG).show();
								}
							});
		} catch (TTransportException exception) {
			Toast.makeText(context, "error", Toast.LENGTH_LONG).show();
		}

	}

	private String getPhone(String contactId) {
		Cursor cursor = cr.query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
						+ contactId, null, null);
		StringBuffer sb = new StringBuffer();
		if (cursor.moveToFirst()) {
			int i = 1;
			do {
				// 遍历所有的电话号码
				String phoneNumber = cursor
						.getString(cursor
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

				if (i == 1) {
					sb.append(phoneNumber);
				} else {
					sb.append(";" + phoneNumber);
				}
				i++;
			} while (cursor.moveToNext());

		}
		return sb.toString();
	}

	private String getEmail(String contactId) {
		Cursor cursor = cr.query(
				ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
						+ contactId, null, null);
		StringBuffer sb = new StringBuffer();
		if (cursor.moveToFirst()) {
			int i = 1;

			do {
				// 遍历所有的email
				String emailValue = cursor
						.getString(cursor
								.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
				if (i == 1) {
					sb.append(emailValue);
				} else {
					sb.append(";" + emailValue);
				}
				i++;
			} while (cursor.moveToNext());
		} else {
			sb.append("");
		}
		cursor.close();
		return sb.toString();
	}

	private String getAddress(String contactId) {
		Cursor cursor = cr.query(
				ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
				null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
						+ contactId, null, null);
		if (cursor.moveToFirst()) {
			// 遍历所有的地址
			String street = cursor
					.getString(cursor
							.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
			String city = cursor
					.getString(cursor
							.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
			String country = cursor
					.getString(cursor
							.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
			if (country == null) {
				country = "";
			}
			if (street == null) {
				street = "";
			}
			if (city == null) {
				city = "";
			}
			cursor.close();
			return country + city + street;
		} else {
			cursor.close();
			return "";
		}

	}

	private String getNote(String contactId) {
		Cursor cursor = cr.query(Data.CONTENT_URI, new String[] { Data._ID,
				Note.NOTE }, Data.CONTACT_ID + "=?" + " AND " + Data.MIMETYPE
				+ "='" + Note.CONTENT_ITEM_TYPE + "'",
				new String[] { contactId }, null);
		StringBuffer sb = new StringBuffer();
		if (cursor.moveToFirst()) {

			do {
				String noteinfo = cursor.getString(cursor
						.getColumnIndex(Note.NOTE));
				sb.append(noteinfo);
			} while (cursor.moveToNext());
		} else {
			sb.append("");
		}
		cursor.close();
		return sb.toString();
	}

	public void importContactXML(String f, ProgressDialog dialog) {

		File file = new File(f.replaceAll("\\.\\w{1,4}$", ".xml"));
		SAXReader reader = new SAXReader();
		try {
			Document document = reader.read(file);
			Element root = document.getRootElement();
			@SuppressWarnings("unchecked")
			List<Element> list = root.elements();

			int progress = 0;
			for (Element e : list) {

				ops = new ArrayList<ContentProviderOperation>();
				rawContactInsertIndex = ops.size();
				ops.add(ContentProviderOperation
						.newInsert(RawContacts.CONTENT_URI)
						.withValue(RawContacts.ACCOUNT_TYPE, null)
						.withValue(RawContacts.ACCOUNT_NAME, null).build());

				List<Element> l = e.elements();
				for (Element ele : l) {
					String n = ele.getName();
					if ("name".equals(n)) {
						addName(ele.getText());
					} else if ("phone".equals(n)) {
						addPhone(ele.getText());
					} else if ("email".equals(n)) {
						addEmail(ele.getText());
					} else if ("address".equals(n)) {
						addAddress(ele.getText());
					} else if ("note".equals(n)) {
						addNote(ele.getText());
					}
				}
				submit();// 只有submit才能向通信录数据库去写入
				dialog.setProgress(++progress);
			}

		} catch (DocumentException e) {
			e.printStackTrace();
		}
		dialog.dismiss();

	}

	public void importContactCSV(String f, ProgressDialog dialog) {

		try {
			CSVReader reader = new CSVReader(new InputStreamReader(
					new FileInputStream(f), "GBK"));
			String[] nextLine = null;

			int progress = 0;
			while ((nextLine = reader.readNext()) != null) {

				ops = new ArrayList<ContentProviderOperation>();
				rawContactInsertIndex = ops.size();
				ops.add(ContentProviderOperation
						.newInsert(RawContacts.CONTENT_URI)
						.withValue(RawContacts.ACCOUNT_TYPE, null)
						.withValue(RawContacts.ACCOUNT_NAME, null).build());

				addName(nextLine[0] == null ? "" : nextLine[0]);
				if (nextLine.length > 1) {
					if (nextLine[1] == null) {
						addPhone("");
					} else {
						for (String s : nextLine[1].split(";")) {
							addPhone(s);
						}
					}
				}

				if (nextLine.length > 2) {
					if (nextLine[2] == null) {
						addEmail("");
					} else {
						for (String s : nextLine[2].split(";")) {
							addEmail(s);
						}
					}
				}

				if (nextLine.length > 3) {

					addAddress(nextLine[3] == null ? "" : nextLine[3]);
				}
				if (nextLine.length > 4) {
					addNote(nextLine[4] == null ? "" : nextLine[4]);

				}
				submit();// 只有submit才能向通信录数据库去写入
				dialog.setProgress(++progress);
			}
			dialog.dismiss();
			reader.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void importContactExcel(String f, ProgressDialog dialog) {

		try {
			Workbook workbook = Workbook.getWorkbook(new File(f));
			Sheet sheet = workbook.getSheet(0);

			for (int i = 1; i < sheet.getRows(); i++) {

				ops = new ArrayList<ContentProviderOperation>();
				rawContactInsertIndex = ops.size();
				ops.add(ContentProviderOperation
						.newInsert(RawContacts.CONTENT_URI)
						.withValue(RawContacts.ACCOUNT_TYPE, null)
						.withValue(RawContacts.ACCOUNT_NAME, null).build());

				addName(sheet.getRow(i)[0].getContents());

				if (sheet.getRow(i).length > 1) {
					if (sheet.getRow(i)[1].getContents().trim().equals("")) {
						addPhone(" ");
					} else {
						for (String s : sheet.getRow(i)[1].getContents().split(
								";")) {
							addPhone(s);
						}
					}
				}

				if (sheet.getRow(i).length > 2) {
					if (sheet.getRow(i)[2].getContents().trim().equals("")) {
						addEmail(" ");
					} else {
						for (String s : sheet.getRow(i)[2].getContents().split(
								";")) {
							addEmail(s);
						}
					}
				}

				if (sheet.getRow(i).length > 3) {

					addAddress(sheet.getRow(i)[3].getContents());
				}
				if (sheet.getRow(i).length > 4) {
					addNote(sheet.getRow(i)[4].getContents());

				}
				dialog.setProgress(i);
				submit();
			}
			workbook.close();
			dialog.dismiss();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BiffException e) {
			e.printStackTrace();
		}
	}

	public void addName(String displayname) {

		ops.add(ContentProviderOperation
				.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(Data.RAW_CONTACT_ID,
						rawContactInsertIndex)
				.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
				.withValue(StructuredName.DISPLAY_NAME, displayname).build());
	}

	public void addPhone(String phone) {
		ops.add(ContentProviderOperation
				.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(Data.RAW_CONTACT_ID,
						rawContactInsertIndex)
				.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
				.withValue(Phone.NUMBER, phone)
				.withValue(Phone.TYPE, Phone.TYPE_MOBILE)
				.withValue(Phone.LABEL, "手机号").build());
	}

	public void addEmail(String email) {
		ops.add(ContentProviderOperation
				.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(Data.RAW_CONTACT_ID,
						rawContactInsertIndex)
				.withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
				.withValue(Email.DATA, email)
				.withValue(Email.TYPE, Email.TYPE_WORK).build());

	}

	public void addAddress(String street) {
		ops.add(ContentProviderOperation
				.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(Data.RAW_CONTACT_ID,
						rawContactInsertIndex)
				.withValue(Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE)
				// .withValue(StructuredPostal.COUNTRY, "lujiacai@163.com")
				// .withValue(StructuredPostal.CITY, "")
				.withValue(StructuredPostal.STREET, street).build());

	}

	public void addNote(String note) {
		ops.add(ContentProviderOperation
				.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(Data.RAW_CONTACT_ID,
						rawContactInsertIndex)
				.withValue(Data.MIMETYPE, Note.CONTENT_ITEM_TYPE)
				.withValue(Note.NOTE, note).build());

	}

	public void submit() {

		ContentProviderResult[] results = null;
		try {
			results = cr.applyBatch(ContactsContract.AUTHORITY, ops);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			e.printStackTrace();
		}
		// for (ContentProviderResult result : results) {
		// Log.i("cursor", result.uri.toString());
		// }
	}
}
