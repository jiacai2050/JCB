package com.ytu.miracle.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.evernote.client.android.InvalidAuthenticationException;
import com.ytu.evernote.ParentActivity;
import com.ytu.miracle.util.ContactUtils;
import com.ytu.miracle.util.PhoneUtils;
import com.ytu.miracle.util.SMSField;
import com.ytu.miracle.util.SMSUtils;

public class MainActivity extends ParentActivity {

	private ProgressDialog dialog;

	private String smsType = "excel"; // Ĭ�ϵ�����ʽΪexcel
	private String contactType = "excel";
	
	private Button login;
	private Button exit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!Environment.getExternalStorageDirectory().exists()) {
			Intent intent = new Intent(MainActivity.this, SDError.class);
			startActivity(intent);
			finish();
		}
		String tip = getSharedPreferences("tip", MODE_PRIVATE).getString("tip",
				"true");
		if ("true".equals(tip)) {
			Intent intent = new Intent(MainActivity.this, Tip.class);
			startActivity(intent);
		}
		setContentView(R.layout.main);
		ListView lv = (ListView) findViewById(R.id.function);
		login = (Button) findViewById(R.id.login);
		exit = (Button) findViewById(R.id.logout);

		lv.setAdapter(new TextImageAdapter(this));

		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				switch (arg2) {
				case 0:
					exportSMS(1); // ����ʱ��˳�򵼳�
					break;
				case 1:
					exportSMS(2); // ������ϵ��˳�򵼳�
					break;
				case 2:
					importSMS();
					break;
				case 3:
					exportContact();
					break;
				case 4:
					importContact();
					break;
				case 5:
					if (mEvernoteSession.isLoggedIn()) {
						exportContactEvernote();
					} else {
						Toast.makeText(MainActivity.this, "���ȵ�¼Evernote��",
								Toast.LENGTH_LONG).show();
					}
					break;
				case 6:
					if (mEvernoteSession.isLoggedIn()) {
						exportSMSEvernote(1);
					} else {
						Toast.makeText(MainActivity.this, "���ȵ�¼Evernote��",
								Toast.LENGTH_LONG).show();
					}
					break;
				case 7:
					if (mEvernoteSession.isLoggedIn()) {
						exportSMSEvernote(2);
					} else {
						Toast.makeText(MainActivity.this, "���ȵ�¼Evernote��",
								Toast.LENGTH_LONG).show();
					}
					break;

				}

			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		updateAuthUi();
	}

	private void updateAuthUi() {
		login.setEnabled(!mEvernoteSession.isLoggedIn());
		exit.setEnabled(mEvernoteSession.isLoggedIn());
	}

	public void login(View view) {
		mEvernoteSession.authenticate(this);
	}

	public void logout(View view) {
		try {
			mEvernoteSession.logOut(this);
		} catch (InvalidAuthenticationException e) {
		}
		updateAuthUi();
	}

	private void exportSMS(int type) {// type=1����ʱ��˳�򵼳� type=2������ϵ�˵���
		HandlerThread handlerThread = new HandlerThread("SMSThread");
		// ��ʹ��HandlerThread��getLooper()����֮ǰ�������ȵ��ø����start();
		handlerThread.start();
		SMSHandler smsHandler = new SMSHandler(handlerThread.getLooper());
		Message msg = smsHandler.obtainMessage();
		msg.arg1 = 1;

		String[] projection = new String[] { SMSField.ADDRESS,
				SMSField.PROTOCOL, SMSField.PERSON, SMSField.DATE,
				SMSField.TYPE, SMSField.BODY }; // type=1���ռ��䣬==2�Ƿ�����
		Uri uri = Uri.parse("content://sms/");
		String sortOrder = SMSField.DATE;
		if (type == 2) {
			sortOrder = SMSField.ADDRESS;
		}
		Cursor cursor = getContentResolver().query(uri, projection, null, null,
				sortOrder);

		msg.obj = cursor;
		// ��msg���͵�Ŀ�������ν��Ŀ����󣬾������ɸ�msg�����handler����
		dialog = new ProgressDialog(MainActivity.this);
		dialog.setTitle("��ʾ��Ϣ");
		dialog.setMessage("���ŵ����У����Ժ�...");
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMax(cursor.getCount());
		dialog.show();
		msg.sendToTarget();
	}

	private void exportSMSEvernote(int type) {// type=1����ʱ��˳�򵼳� type=2������ϵ�˵���
		HandlerThread handlerThread = new HandlerThread("EvernoteThread");
		// ��ʹ��HandlerThread��getLooper()����֮ǰ�������ȵ��ø����start();
		handlerThread.start();
		EvernoteHandler evernoteHandler = new EvernoteHandler(handlerThread.getLooper());
		Message msg = evernoteHandler.obtainMessage();
		msg.arg1 = 1;
		String[] projection = new String[] { SMSField.ADDRESS,
				SMSField.PROTOCOL, SMSField.PERSON, SMSField.DATE,
				SMSField.TYPE, SMSField.BODY }; // type=1���ռ��䣬==2�Ƿ�����
		Uri uri = Uri.parse("content://sms/");
		String sortOrder = SMSField.DATE;
		if (type == 2) {
			sortOrder = SMSField.ADDRESS;
		}
		Cursor cursor = getContentResolver().query(uri, projection, null, null,
				sortOrder);
		msg.arg1 = 1;
		msg.obj = cursor;
		// ��msg���͵�Ŀ�������ν��Ŀ����󣬾������ɸ�msg�����handler����
		dialog = new ProgressDialog(MainActivity.this);
		dialog.setTitle("��ʾ��Ϣ");
		dialog.setMessage("���ŵ����У����Ժ�...");
		//dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMax(cursor.getCount());
		dialog.show();
		msg.sendToTarget();
	}
	private void importSMS() {
		Intent intent = new Intent(MainActivity.this, FileChooseDialog.class);
		intent.putExtra("type", "sms");
		startActivityForResult(intent, 2013);
	}

	private void exportContact() { // type=1ʱ����Ϊxml��type=2ʱ����csv
		HandlerThread handlerThread = new HandlerThread("ContactThread");
		// ��ʹ��HandlerThread��getLooper()����֮ǰ�������ȵ��ø����start();
		handlerThread.start();
		ContactHandler contactHandler = new ContactHandler(
				handlerThread.getLooper());
		Message msg = contactHandler.obtainMessage();
		msg.arg1 = 1;
		dialog = new ProgressDialog(MainActivity.this);
		dialog.setTitle("��ʾ��Ϣ");
		dialog.setMessage("ͨ��¼�����У����Ժ�...");
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.show();
		Cursor cur = getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI, null, null, null,
				ContactsContract.Contacts.DISPLAY_NAME);
		dialog.setMax(cur.getCount());
		msg.obj = cur;
		msg.sendToTarget();
	}

	private void importContact() {
		Intent intent = new Intent(MainActivity.this, FileChooseDialog.class);
		intent.putExtra("type", "contact");
		startActivityForResult(intent, 2012);
	}

	private void exportContactEvernote() {
		HandlerThread handlerThread = new HandlerThread("EvernoteThread");
		// ��ʹ��HandlerThread��getLooper()����֮ǰ�������ȵ��ø����start();
		handlerThread.start();
		EvernoteHandler evernoteHandler = new EvernoteHandler(
				handlerThread.getLooper());
		Message msg = evernoteHandler.obtainMessage();
		dialog = new ProgressDialog(MainActivity.this);
		dialog.setTitle("��ʾ��Ϣ");
		dialog.setMessage("ͨ��¼�����У����Ժ�...");
		// dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.show();
		Cursor cur = getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI, null, null, null,
				ContactsContract.Contacts.DISPLAY_NAME);
		dialog.setMax(cur.getCount());
		msg.obj = cur;
		msg.arg1 = 2;
		msg.sendToTarget();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {

			if (requestCode == 2013) {
				HandlerThread handlerThread = new HandlerThread("SMSThread");
				// ��ʹ��HandlerThread��getLooper()����֮ǰ�������ȵ��ø����start();
				handlerThread.start();
				SMSHandler smsHandler = new SMSHandler(
						handlerThread.getLooper());
				Message msg = smsHandler.obtainMessage();
				msg.arg1 = 2;
				msg.obj = data.getStringExtra("file");
				if (!"blank".equals(msg.obj.toString())) {
					if (msg.obj.toString().endsWith("xml")
							|| msg.obj.toString().endsWith("xls")) {
						dialog = new ProgressDialog(MainActivity.this);
						dialog.setTitle("��ʾ��Ϣ");
						dialog.setMessage("���ŵ����У����Ժ�...");
						dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						dialog.setMax(PhoneUtils.getFileCount(msg.obj
								.toString()));
						dialog.show();
						msg.sendToTarget();
					} else {
						Toast.makeText(MainActivity.this, "��ѡ���ļ���ʽ��֧�֣�������ѡ��",
								Toast.LENGTH_LONG).show();
					}
				}
			} else if (requestCode == 2012) {
				HandlerThread handlerThread = new HandlerThread("ContactThread");
				// ��ʹ��HandlerThread��getLooper()����֮ǰ�������ȵ��ø����start();
				handlerThread.start();
				ContactHandler contactHandler = new ContactHandler(
						handlerThread.getLooper());
				Message msg = contactHandler.obtainMessage();
				msg.arg1 = 2;
				msg.obj = data.getStringExtra("file");

				if (!"blank".equals(msg.obj.toString())) {
					if (msg.obj.toString().endsWith("xml")
							|| msg.obj.toString().endsWith("xls")
							|| msg.obj.toString().endsWith("csv")) {
						dialog = new ProgressDialog(MainActivity.this);
						dialog.setTitle("��ʾ��Ϣ");
						dialog.setMessage("ͨ��¼�����У����Ժ�...");
						dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						dialog.setMax(PhoneUtils.getFileCount(msg.obj
								.toString()));
						dialog.show();
						msg.sendToTarget();
					} else {
						Toast.makeText(MainActivity.this, "��ѡ���ļ���ʽ��֧�֣�������ѡ��",
								Toast.LENGTH_LONG).show();
					}
				}
			} else if (requestCode == 2011) {// �����ļ�������
				smsType = data.getStringExtra("smsType");
				contactType = data.getStringExtra("contactType");
				// Toast.makeText(this,
				// "���ţ�" + smsType + "  ͨ��¼��" + contactType,
				// Toast.LENGTH_SHORT).show();
			}
		}
	}

	class EvernoteHandler extends Handler {
		public EvernoteHandler() {

		}

		public EvernoteHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// ��������ȥevernote
			if (msg.arg1==1) { //��������
				SMSUtils utils = new SMSUtils(MainActivity.this);
				Cursor cursor = (Cursor) msg.obj;
					try {
						utils.exportSMSEvernote(cursor, dialog, mEvernoteSession);
					} catch (Exception e) {
						e.printStackTrace();
					}
			} else { //����ͨ��¼
				ContactUtils utils = new ContactUtils(MainActivity.this, dialog);
				Cursor cursor = (Cursor) msg.obj;
				utils.exportContactEvernote(cursor, mEvernoteSession);	
			}
			
		}
	}

	class ContactHandler extends Handler {
		public ContactHandler() {
		}

		public ContactHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int type = msg.arg1;
			if (type == 1) {
				ContactUtils utils = new ContactUtils(MainActivity.this, dialog);
				Cursor cursor = (Cursor) msg.obj;
				if ("xml".equals(contactType)) {
					utils.exportContactXML(cursor);
				} else if ("csv".equals(contactType)) {
					utils.exportContactCSV(cursor);
				} else {
					try {
						utils.exportContactExcel(cursor);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				Toast.makeText(MainActivity.this, "ͨ��¼�����ɹ�", Toast.LENGTH_SHORT)
						.show();
			} else {

				String file = msg.obj.toString();
				ContactUtils utils = new ContactUtils(MainActivity.this, dialog);
				if (file.endsWith(".csv")) {
					utils.importContactCSV(msg.obj.toString(), dialog);
				} else if (file.endsWith(".xls")) {
					utils.importContactExcel(msg.obj.toString(), dialog);
				} else {
					utils.importContactXML(msg.obj.toString(), dialog);
				}

				Toast.makeText(MainActivity.this, "ͨ��¼����ɹ�", Toast.LENGTH_SHORT)
						.show();
			}

		}
	}

	class SMSHandler extends Handler {
		public SMSHandler() {
		}

		public SMSHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int type = msg.arg1;
			if (type == 1) {
				SMSUtils utils = new SMSUtils(MainActivity.this);
				Cursor cursor = (Cursor) msg.obj;
				if ("xml".equals(smsType)) {
					utils.exportSMSXML(cursor, dialog);
				} else {
					try {
						utils.exportSMSExcel(cursor, dialog);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				Toast.makeText(MainActivity.this, "���ŵ����ɹ�", Toast.LENGTH_SHORT)
						.show();
			} else {
				SMSUtils utils = new SMSUtils(MainActivity.this);
				if (msg.obj.toString().endsWith("xml")) {
					utils.importSMSXML(msg.obj.toString(), dialog);
				} else {
					utils.importSMSExcel(msg.obj.toString(), dialog);
				}
				Toast.makeText(MainActivity.this, "���ŵ���ɹ�", Toast.LENGTH_SHORT)
						.show();
			}

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, "�������");
		menu.add(0, 2, 0, "�û�ָ��");
		menu.add(0, 3, 0, "���߽���");
		menu.add(0, 4, 0, "�˳�");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case 1:
			intent = new Intent(MainActivity.this, Setting.class);
			intent.putExtra("smsType", smsType);
			intent.putExtra("contactType", contactType);
			startActivityForResult(intent, 2011);
			break;
		case 2:
			intent = new Intent(MainActivity.this, Instruction.class);
			startActivity(intent);
			break;
		case 3:
			intent = new Intent(MainActivity.this, AboutUS.class);
			startActivity(intent);
			break;
		case 4:
			this.finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
