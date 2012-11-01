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
	builder.append("正在登录中。。。"+"\n");
	show.setText(builder.toString());
	/*
	FLAG = getIntent().getStringExtra("FLAG");
	int a=Integer.parseInt(FLAG);
	switch(a){
	case 1:
		builder.append("正在登录中。。。"+"\n");
		builder.append("登录成功！"+"\n");
		logout.setVisibility(View.VISIBLE);
		show.setText(builder);
		break;
	case 2:
		builder.append("正在登录中。。。"+"\n");
		builder.append("登录失败！"+"\n");
		builder.append("请进行浏览器测试！"+"\n");
		browser.setVisibility(View.VISIBLE);
		show.setText(builder);
		break;
	case 0:
		builder.append("亲"+"\n");
		builder.append("别捣乱好吗"+"\n");
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
			builder.append("正在下线。。。"+"\n");
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
						Toast.makeText(LoginProcess.this, "登陆成功", Toast.LENGTH_LONG).show();
						builder.append("登录成功！"+"\n");
						builder.append(desc + "\n");
						if (connectionReady) {
							builder.append("测试百度页面打开成功\n");
						} else {
							builder.append("测试百度页面打开失败\n");
						}
						builder.append("请进行下线测试！"+"\n");
						logout.setVisibility(View.VISIBLE);
						show.setText(builder);
					}
				});
			} else {
				LoginProcess.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(LoginProcess.this, "登录失败", Toast.LENGTH_LONG).show();
						builder.append("登录失败！"+"\n");
						builder.append(desc + "\n");
						if (connectionReady) {
							builder.append("测试百度页面打开成功\n");
						} else {
							builder.append("测试百度页面打开失败\n");
						}
						builder.append("请进行浏览器测试！"+"\n");
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
						Toast.makeText(LoginProcess.this, "登出成功", Toast.LENGTH_LONG).show();
						builder.append("登出成功！"+"\n");
						builder.append(desc + "\n");
						builder.append("请生成报告！"+"\n");
						show.setText(builder.toString());
						report.setVisibility(View.VISIBLE);
						logout.setVisibility(View.INVISIBLE);
					}
				});
			} else {
				LoginProcess.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(LoginProcess.this, "登出失败，直接下线", Toast.LENGTH_LONG).show();
						builder.append("登出失败！"+"\n");
						builder.append(desc + "\n");
						builder.append("请生成报告！"+"\n");
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
