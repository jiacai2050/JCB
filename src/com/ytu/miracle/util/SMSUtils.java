package com.ytu.miracle.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jxl.Cell;
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

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.type.Note;
import com.evernote.thrift.transport.TTransportException;
import com.ytu.miracle.activity.MainActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.Toast;

public class SMSUtils {
	private Context context;

	private String address;
	private String person;
	private String date;
	private String type;
	private String body;
	private String protocol;
	private String[] typeName = { "ȫ��", "�ռ���", "�ѷ���", "�ݸ�", "������", "����ʧ��",
			"������" };
	private String[] protocolName = { "����", "����" };
	private List<SMSItem> smsItems;

	private int row = 1;
	private int col = 0;

	private ProgressDialog dialog;

	public SMSUtils(Context context) {
		this.context = context;
		smsItems = new ArrayList<SMSItem>();
	}

	public void exportSMSEvernote(Cursor cursor, ProgressDialog dialog,
			EvernoteSession mEvernoteSession) {
		Note note = new Note();
		this.dialog = dialog;
		Calendar c = Calendar.getInstance();
		String fileName = "���ű���" + c.get(Calendar.YEAR) + "��"
				+ (c.get(Calendar.MONTH) + 1) + "��" + c.get(Calendar.DATE)
				+ "��" + c.get(Calendar.HOUR_OF_DAY) + "-"
				+ c.get(Calendar.MINUTE) + "-" + c.get(Calendar.SECOND);
		note.setTitle(fileName);
		StringBuilder msg = new StringBuilder();
		int index = 1;
		while (cursor.moveToNext()) {
			protocol = cursor.getString(cursor
					.getColumnIndex(SMSField.PROTOCOL));
			if (protocol == null) {
				protocol = "0";
			}
			address = cursor.getString(cursor.getColumnIndex(SMSField.ADDRESS));
			if (address == null) {
				address = "";
			}
			person = cursor.getString(cursor.getColumnIndex(SMSField.PERSON));
			if (person == null || "".equals(person)) {
				person = new PhoneUtils(context).findNameByPhoneNum(address);
			}
			date = cursor.getString(cursor.getColumnIndex(SMSField.DATE));
			if (date == null) {
				date = "";
			} else {
				SimpleDateFormat format = new SimpleDateFormat(
						"yyyy/MM/dd HH:mm");
				date = format.format(new Date(Long.valueOf(date)));
			}

			type = cursor.getString(cursor.getColumnIndex(SMSField.TYPE));
			if (type == null) {
				type = "0";
			}

			body = cursor.getString(cursor.getColumnIndex(SMSField.BODY));
			if (body == null) {
				body = "";
			}
			msg.append("<p>");
			msg.append(index + ".������" + person);
			msg.append("&nbsp;&nbsp;&nbsp;&nbsp;�绰��" + address);
			msg.append("&nbsp;&nbsp;&nbsp;&nbsp;���ڣ�" + date);
			msg.append("&nbsp;&nbsp;&nbsp;&nbsp;���ͣ�"
					+ typeName[Integer.valueOf(type)]);
			msg.append("&nbsp;&nbsp;&nbsp;&nbsp;���ݣ�" + body);
			msg.append("</p>");
			index++;
		}
		note.setContent(EvernoteUtil.NOTE_PREFIX + msg.toString()
				+ EvernoteUtil.NOTE_SUFFIX);

		try {
			mEvernoteSession
					.getClientFactory()
					.createNoteStoreClient()
					.createNote(note,
							new OnClientCallback<com.evernote.edam.type.Note>() {
								@Override
								public void onException(Exception exception) {
									SMSUtils.this.dialog.dismiss();
									Toast.makeText(context, "����ʧ��",
											Toast.LENGTH_LONG).show();
									exception.printStackTrace();
									// removeDialog(DIALOG_PROGRESS);
								}

								@Override
								public void onSuccess(
										com.evernote.edam.type.Note data) {
									SMSUtils.this.dialog.dismiss();
									Toast.makeText(context, "���ųɹ����뵽Evernote��",
											Toast.LENGTH_LONG).show();
								}
							});
		} catch (TTransportException e) {
			e.printStackTrace();
			dialog.dismiss();
		}

	}

	public void exportSMSXML(Cursor cursor, ProgressDialog dialog) {

		String path = Environment.getExternalStorageDirectory().getPath();
		Calendar c = Calendar.getInstance();
		String fileName = "���ű���" + c.get(Calendar.YEAR) + "��"
				+ (c.get(Calendar.MONTH) + 1) + "��" + c.get(Calendar.DATE)
				+ "��" + c.get(Calendar.HOUR_OF_DAY) + "-"
				+ c.get(Calendar.MINUTE) + "-" + c.get(Calendar.SECOND)
				+ ".xml";
		File message = new File(path + "/" + fileName);
		if (!message.exists()) {
			try {
				message.createNewFile();
				Document document = DocumentHelper.createDocument();
				document.addElement("all").addAttribute("number", "0");
				XMLWriter output = new XMLWriter(new FileWriter(message)); // �����ĵ�
				output.write(document);
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {

			int progress = 0;
			while (cursor.moveToNext()) {
				protocol = cursor.getString(cursor
						.getColumnIndex(SMSField.PROTOCOL));
				if (protocol == null) {
					protocol = "0";
				}
				address = cursor.getString(cursor
						.getColumnIndex(SMSField.ADDRESS));
				if (address == null) {
					address = "";
				}
				person = cursor.getString(cursor
						.getColumnIndex(SMSField.PERSON));
				if (person == null || "".equals(person)) {
					person = new PhoneUtils(context)
							.findNameByPhoneNum(address);
				}
				date = cursor.getString(cursor.getColumnIndex(SMSField.DATE));
				if (date == null) {
					date = "";
				} else {
					SimpleDateFormat format = new SimpleDateFormat(
							"yyyy/MM/dd HH:mm");
					date = format.format(new Date(Long.valueOf(date)));
				}

				type = cursor.getString(cursor.getColumnIndex(SMSField.TYPE));
				if (type == null) {
					type = "0";
				}

				body = cursor.getString(cursor.getColumnIndex(SMSField.BODY));
				if (body == null) {
					body = "";
				}

				SAXReader reader = new SAXReader();
				Document document = reader.read(message);
				Element root = document.getRootElement();

				root.attribute("number").setValue(
						String.valueOf(cursor.getCount()));// number

				Element sms = root.addElement("sms");

				Element idAttr = sms.addElement("id");
				Element personAttr = sms.addElement("person");
				Element addressAttr = sms.addElement("address");
				Element ProcotolAttr = sms.addElement("procotol");
				Element dateAttr = sms.addElement("date");
				Element typeAttr = sms.addElement("type");
				Element bodyAttr = sms.addElement("body");

				idAttr.setText(String.valueOf(cursor.getPosition() + 1));
				personAttr.setText(person);
				addressAttr.setText(address);
				dateAttr.setText(date);
				typeAttr.setText(typeName[Integer.valueOf(type)]);
				bodyAttr.setText(body);
				ProcotolAttr.setText(protocolName[Integer.valueOf(protocol)]);
				// document.setRootElement(root);
				/** ��ʽ�����������IE���һ�� */
				OutputFormat format = OutputFormat.createPrettyPrint();
				/** ָ��XML���� */
				format.setEncoding("UTF-8");
				XMLWriter writer = new XMLWriter(new FileOutputStream(message),
						format);
				writer.write(document);
				writer.close();
				dialog.setProgress(++progress);
			}

			cursor.close();
			dialog.dismiss();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void exportEvernote(Cursor cursor, ProgressDialog dialog) {

	}

	public void exportSMSExcel(Cursor cursor, ProgressDialog dialog)
			throws Exception {

		String path = Environment.getExternalStorageDirectory().getPath();
		Calendar c = Calendar.getInstance();
		String fileName = "���ű���" + c.get(Calendar.YEAR) + "��"
				+ (c.get(Calendar.MONTH) + 1) + "��" + c.get(Calendar.DATE)
				+ "��" + c.get(Calendar.HOUR_OF_DAY) + "-"
				+ c.get(Calendar.MINUTE) + "-" + c.get(Calendar.SECOND)
				+ ".xls";
		File file = new File(path + "/" + fileName);
		WritableWorkbook book = Workbook.createWorkbook(file);
		WritableSheet sheet = book.createSheet("��һҳ ", 0);
		String[] cont = { "����", "����", "����", "���", "����" }; // ��ͷ
		for (int i = 0; i < 5; i++) {
			sheet.addCell(new Label(i, 0, cont[i]));
		}
		try {

			int progress = 0;
			while (cursor.moveToNext()) {
				protocol = cursor.getString(cursor
						.getColumnIndex(SMSField.PROTOCOL));
				if (protocol == null) {
					protocol = "0";
				}
				address = cursor.getString(cursor
						.getColumnIndex(SMSField.ADDRESS));
				if (address == null) {
					address = "";
				}
				person = cursor.getString(cursor
						.getColumnIndex(SMSField.PERSON));
				if (person == null || "".equals(person)) {
					person = new PhoneUtils(context)
							.findNameByPhoneNum(address);
				}
				date = cursor.getString(cursor.getColumnIndex(SMSField.DATE));
				if (date == null) {
					date = "";
				} else {
					SimpleDateFormat format = new SimpleDateFormat(
							"yyyy/MM/dd HH:mm");
					date = format.format(new Date(Long.valueOf(date)));
				}

				type = cursor.getString(cursor.getColumnIndex(SMSField.TYPE));
				if (type == null) {
					type = "0";
				}

				body = cursor.getString(cursor.getColumnIndex(SMSField.BODY));
				if (body == null) {
					body = "";
				}
				sheet.addCell(new Label(col++, row, person));
				sheet.addCell(new Label(col++, row, address));
				sheet.addCell(new Label(col++, row, date));
				sheet.addCell(new Label(col++, row, typeName[Integer
						.valueOf(type)]));
				sheet.addCell(new Label(col++, row, body));

				dialog.setProgress(++progress);

				row++;
				col = 0;
			}

			cursor.close();
			dialog.dismiss();

			book.write();
			book.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void importSMSExcel(String filepath, ProgressDialog dialog) {
		/**
		 * ��һ������xml�ļ���ģ��
		 */
		File file = new File(filepath);
		Workbook workbook = null;
		try {
			workbook = Workbook.getWorkbook(file);
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Sheet sheet = workbook.getSheet(0);
		ContentResolver conResolver = context.getContentResolver();
		for (int i = 1; i < sheet.getRows(); i++) {
			Cell[] cell = sheet.getRow(i);
			ContentValues values = new ContentValues();
			if (cell.length > 0) {
				values.put(SMSField.PERSON, cell[0].getContents());
			}
			if (cell.length > 1) {
				values.put(SMSField.ADDRESS, cell[1].getContents());
			}
			if (cell.length > 2) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyy/MM/dd HH:mm");
					Date d = sdf.parse(cell[2].getContents());
					values.put(SMSField.DATE, String.valueOf(d.getTime()));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			if (cell.length > 3) {
				values.put(SMSField.TYPE, type2int(cell[3].getContents()));
			}
			if (cell.length > 4) {
				values.put(SMSField.BODY, cell[4].getContents());
			}
			values.put(SMSField.STATUS, "1");
			values.put(SMSField.REPLY_PATH_PRESENT, "");
			values.put(SMSField.LOCKED, "0");
			values.put(SMSField.ERROR_CODE, "1");
			values.put(SMSField.SEEN, "1");
			values.put(SMSField.PROTOCOL, "0");
			conResolver.insert(Uri.parse("content://sms"), values);

			dialog.setProgress(i);
		}
		dialog.dismiss();
	}

	public void importSMSXML(String filepath, ProgressDialog dialog) {
		/**
		 * ��һ������xml�ļ���ģ��
		 */
		try {
			File file = new File(filepath);
			SAXReader reader = new SAXReader();
			Document document = reader.read(file);
			Element root = document.getRootElement();
			@SuppressWarnings("unchecked")
			List<Element> list = root.elements();
			int i = 0;
			for (Element e : list) {

				ContentResolver conResolver = context.getContentResolver();
				// �ж϶������ݿ����Ƿ��Ѱ����������ţ�����У�����Ҫ�ָ�
				Cursor cursor = conResolver
						.query(Uri.parse("content://sms"),
								new String[] { SMSField.DATE },
								SMSField.DATE + "=?",
								new String[] { date2long(e.elementText("date")) },
								null);

				ContentValues values = new ContentValues();
				values.put(SMSField.ADDRESS, e.elementText("address"));
				// ����ǿ��ַ���˵��ԭ����ֵ��null���������ﻹԭΪnull�������ݿ�
				values.put(SMSField.PERSON, e.elementText("person"));
				values.put(SMSField.DATE, date2long(e.elementText("date")));
				values.put(SMSField.PROTOCOL,
						protocol2int(e.elementText("protocol")));
				values.put(SMSField.STATUS, "1");
				values.put(SMSField.TYPE, type2int(e.elementText("type")));
				values.put(SMSField.REPLY_PATH_PRESENT, "");
				values.put(SMSField.BODY, e.elementText("body"));
				values.put(SMSField.LOCKED, "0");
				values.put(SMSField.ERROR_CODE, "1");
				values.put(SMSField.SEEN, "1");
				conResolver.insert(Uri.parse("content://sms"), values);

				dialog.setProgress(++i);
				cursor.close();
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}

		dialog.dismiss();
	}

	private String date2long(String date) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			Date d = sdf.parse(date);
			return String.valueOf(d.getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "";
	}

	private String type2int(String type) {
		if ("ȫ��".equals(type)) {
			return "0";
		}
		if ("�ռ���".equals(type)) {
			return "1";
		}
		if ("�ѷ���".equals(type)) {
			return "2";
		}
		if ("�ݸ�".equals(type)) {
			return "3";
		}
		if ("������".equals(type)) {
			return "4";
		}
		if ("����ʧ��".equals(type)) {
			return "5";
		}
		if ("������".equals(type)) {
			return "6";
		}
		return "1";
	}

	private String protocol2int(String protocol) {
		if ("����".equals(protocol)) {
			return "0";
		} else {
			return "1";
		}
	}

}