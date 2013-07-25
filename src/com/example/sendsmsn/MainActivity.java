package com.example.sendsmsn;

import java.util.ArrayList;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	/** 发送与接收的广播 **/
	String SENT_SMS_ACTION = "SENT_SMS_ACTION";
	String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
	Button send;
	EditText smsText;
	EditText smsNumber;
	EditText smsTimes;
	Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		smsNumber = (EditText) findViewById(R.id.editText1);
		smsText = (EditText) findViewById(R.id.editText2);
		smsTimes = (EditText) findViewById(R.id.editText3);
		mContext = this;
		send = (Button) findViewById(R.id.button1);

		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				String number = smsNumber.getText().toString();
				String text = smsText.getText().toString();
				int n = Integer.parseInt(smsTimes.getText().toString());

				for (int i = 0; i < n; i++) {
					if (!TextUtils.isEmpty(number) && !TextUtils.isEmpty(text)) {
						sendSMS(number, text);

						/** 将发送的短信插入数据库 **/
						ContentValues values = new ContentValues();
						// 发送时间
						values.put("date", System.currentTimeMillis());
						// 阅读状态
						values.put("read", 0);
						// 1为收 2为发
						values.put("type", 2);
						// 送达号码
						values.put("address", number);
						// 送达内容
						values.put("body", text);
						// 插入短信库
						getContentResolver().insert(Uri.parse("content://sms"),
								values);
					}
				}

			}
		});

		// 注册广播 发送消息
		registerReceiver(sendMessage, new IntentFilter(SENT_SMS_ACTION));
		registerReceiver(receiver, new IntentFilter(DELIVERED_SMS_ACTION));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private BroadcastReceiver sendMessage = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 判断短信是否发送成功
			switch (getResultCode()) {
			case Activity.RESULT_OK:
				Toast.makeText(context, "短信发送成功", Toast.LENGTH_SHORT).show();
				break;
			default:
				Toast.makeText(mContext, "发送失败", Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 表示对方成功收到短信
			Toast.makeText(mContext, "对方接收成功", Toast.LENGTH_LONG).show();
		}
	};

	private void sendSMS(String phoneNumber, String message) {
		// ---sends an SMS message to another device---
		SmsManager sms = SmsManager.getDefault();

		// create the sentIntent parameter
		Intent sentIntent = new Intent(SENT_SMS_ACTION);
		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, sentIntent,
				0);

		// create the deilverIntent parameter
		Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
		PendingIntent deliverPI = PendingIntent.getBroadcast(this, 0,
				deliverIntent, 0);

		// 如果短信内容超过70个字符 将这条短信拆成多条短信发送出去
		if (message.length() > 70) {
			ArrayList<String> msgs = sms.divideMessage(message);
			for (String msg : msgs) {
				sms.sendTextMessage(phoneNumber, null, msg, sentPI, deliverPI);
			}
		} else {
			sms.sendTextMessage(phoneNumber, null, message, sentPI, deliverPI);
		}
	}

}
