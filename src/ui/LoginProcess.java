package ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testclient.R;

import engine.AuthPortalCMCC;
import engine.AuthPortalCT;
import engine.Logger;

public class LoginProcess extends Activity{
	TextView show;
	Button report;
	Button logout;
	Button browser;
	StringBuilder builder=new StringBuilder();
	
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_process);
		init();
	}

	
	
	
	
	
	public void init(){
	show=(TextView) findViewById(R.id.show);
	report=(Button) findViewById(R.id.report);
	browser=(Button) findViewById(R.id.browser);
	logout=(Button) findViewById(R.id.logout);
	builder.append("���ڵ�¼�С�����"+"\n");
	show.setText(builder.toString());
	/*
	FLAG = getIntent().getStringExtra("FLAG");
	int a=Integer.parseInt(FLAG);
	switch(a){
	case 1:
		builder.append("���ڵ�¼�С�����"+"\n");
		builder.append("��¼�ɹ���"+"\n");
		logout.setVisibility(View.VISIBLE);
		show.setText(builder);
		break;
	case 2:
		builder.append("���ڵ�¼�С�����"+"\n");
		builder.append("��¼ʧ�ܣ�"+"\n");
		builder.append("�������������ԣ�"+"\n");
		browser.setVisibility(View.VISIBLE);
		show.setText(builder);
		break;
	case 0:
		builder.append("��"+"\n");
		builder.append("���Һ���"+"\n");
		browser.setVisibility(View.VISIBLE);
		logout.setVisibility(View.VISIBLE);
		report.setVisibility(View.VISIBLE);
		show.setText(builder);
		break;
	}
	*/
	Logger.getInstance().startLogger();
	new Thread(login_runnable).start();
	
	
	report.setOnClickListener(new OnClickListener() {
		
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent=new Intent();
			intent.setClass(LoginProcess.this, Report.class);
			startActivity(intent);
		}
	});
	
	
	
	browser.setOnClickListener(new OnClickListener() {
		
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent=new Intent();
			intent.setClass(LoginProcess.this, Browser.class);
			startActivity(intent);
		}
	});
	
	
	
	
	logout.setOnClickListener(new OnClickListener() {
		
		public void onClick(View v) {
			// TODO Auto-generated method stub
			builder.append("�������ߡ�����"+"\n");
			show.setText(builder);
			new Thread(logout_runnable).start();
		}
	});
	
	
	
	
	
	
	
	
	}
	
	private Runnable login_runnable = new Runnable() {
		@Override
		public void run() {
			boolean result = false;
			int code = -2;
			String description = null;
			MyApplication mApp = (MyApplication)getApplication();
			int carrier = mApp.getCarrier();
			String user = mApp.getUser();
			String password = mApp.getPassword();
			if (carrier == MyApplication.CMCC) {
				code = AuthPortalCMCC.getInstance().login(user, password);
				result = (code == 0);
				description = AuthPortalCMCC.getInstance().getDescription(code);
			} else if (carrier == MyApplication.CHINANET) {
				code = AuthPortalCT.getInstance().login(user, password);
				result = (code == 50);
				description = AuthPortalCT.getInstance().getDescription(code);
			}
			final String desc = description;
			final boolean connectionReady = AuthPortalCT.getInstance().testConnection();
			if (result) {
				LoginProcess.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(LoginProcess.this, "��½�ɹ�", Toast.LENGTH_LONG).show();
						builder.append("��¼�ɹ���"+"\n");
						builder.append(desc + "\n");
						if (connectionReady) {
							builder.append("���԰ٶ�ҳ��򿪳ɹ�\n");
						} else {
							builder.append("���԰ٶ�ҳ���ʧ��\n");
						}
						builder.append("��������߲��ԣ�"+"\n");
						logout.setVisibility(View.VISIBLE);
						show.setText(builder);
					}
				});
			} else {
				LoginProcess.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(LoginProcess.this, "��¼ʧ��", Toast.LENGTH_LONG).show();
						builder.append("��¼ʧ�ܣ�"+"\n");
						builder.append(desc + "\n");
						if (connectionReady) {
							builder.append("���԰ٶ�ҳ��򿪳ɹ�\n");
						} else {
							builder.append("���԰ٶ�ҳ���ʧ��\n");
						}
						builder.append("�������������ԣ�"+"\n");
						browser.setVisibility(View.VISIBLE);
						show.setText(builder);
					}
				});
			}
		}
	};
	
	private Runnable logout_runnable = new Runnable() {
		@Override
		public void run() {
			boolean result = false;
			int code = -2;
			String description = null;
			MyApplication mApp = (MyApplication)getApplication();
			int carrier = mApp.getCarrier();
			if (carrier == MyApplication.CMCC) {
				code = AuthPortalCMCC.getInstance().logout();
				result = (code == 0);
				description = AuthPortalCMCC.getInstance().getDescription(code);
			} else if (carrier == MyApplication.CHINANET) {
				code = AuthPortalCT.getInstance().logout();
				result = (code == 150);
				description = AuthPortalCT.getInstance().getDescription(code);
			}
			final String desc = description;
			if (result) {
				LoginProcess.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(LoginProcess.this, "�ǳ��ɹ�", Toast.LENGTH_LONG).show();
						builder.append("�ǳ��ɹ���"+"\n");
						builder.append(desc + "\n");
						builder.append("�����ɱ��棡"+"\n");
						show.setText(builder.toString());
						report.setVisibility(View.VISIBLE);
						logout.setVisibility(View.INVISIBLE);
					}
				});
			} else {
				LoginProcess.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(LoginProcess.this, "�ǳ�ʧ�ܣ�ֱ������", Toast.LENGTH_LONG).show();
						builder.append("�ǳ�ʧ�ܣ�"+"\n");
						builder.append(desc + "\n");
						builder.append("�����ɱ��棡"+"\n");
						show.setText(builder.toString());
						report.setVisibility(View.VISIBLE);
						logout.setVisibility(View.INVISIBLE);
					}
				});
			}
		}
	};
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			
		}
		return true;
		
	}
	
	
	protected void onStart() { 
		 
		super.onStart(); 
		 
		MyApplication mApp = (MyApplication)getApplication(); 
		 
		if (mApp.isExit()) { 
		 
		finish(); 
		 
		} 
		 
		}
}
