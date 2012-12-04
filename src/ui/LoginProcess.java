package ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testclient.R;
import engine.AuthPortalCMCC;
import engine.AuthPortalCMCC1;
import engine.AuthPortalCT;
import engine.AuthPortalStar;

public class LoginProcess extends Activity {
	TextView show;
	ImageView report;
	ImageView logout;
	ImageView browser;
	ImageView back;
	StringBuilder builder = new StringBuilder();
	private ImageView login_process_step;
	private String stepstring;
	private int stepint;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_process);
		init();
	}

	public void init() {
		back = (ImageView) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				LoginProcess.this.finish();
			}
		});
		login_process_step = (ImageView) findViewById(R.id.login_process_step);
		stepstring = getIntent().getStringExtra("step");
		stepint = Integer.parseInt(stepstring);
		switch (stepint) {
		case 1:
			login_process_step.setImageResource(R.drawable.firststep);
			break;
		case 2:
			login_process_step.setImageResource(R.drawable.secondstep);
			break;
		case 3:
			login_process_step.setImageResource(R.drawable.thirdstep);
			break;
		case 4:
			login_process_step.setImageResource(R.drawable.fourthstep);
			break;

		default:
			break;
		}

		stepint = stepint + 1;
		show = (TextView) findViewById(R.id.show);
		report = (ImageView) findViewById(R.id.report);
		browser = (ImageView) findViewById(R.id.browser);
		logout = (ImageView) findViewById(R.id.logout);
		builder.append("正在登录中。。。" + "\n");
		show.setText(builder.toString());
//下面一句新起了一个线程专门用来进行登录工作
		new Thread(login_runnable).start();

		report.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(LoginProcess.this, Report.class);
				intent.putExtra("step", "" + stepint);
				startActivity(intent);
			}
		});

		browser.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.putExtra("step", "" + stepint);
				intent.setClass(LoginProcess.this, Browser.class);
				startActivity(intent);
			}
		});

		logout.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				builder.append("正在下线。。。" + "\n");
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
			MyApplication mApp = (MyApplication) getApplication();
			int carrier = mApp.getCarrier();
			String user = mApp.getUser();
			String password = mApp.getPassword();
			if (carrier == MyApplication.CMCC) {
				code = AuthPortalCMCC1.getInstance().login(user, password,
						LoginProcess.this);
				if (code == -3) {
					LoginProcess.this.runOnUiThread(new Runnable() {
						public void run() {
							Builder alertDialog = new AlertDialog.Builder(
									LoginProcess.this);
							alertDialog.setTitle("注意！！！账号已经在线");
							alertDialog.setMessage("您的账号在线中，请先注销上次登录，然后再次进行测试");
							alertDialog.setPositiveButton("继续",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											LoginProcess.this.finish();
										}
									});
							alertDialog.create().show();
						}
					});

				}

				result = (code == 0);
				description = AuthPortalCMCC.getInstance().getDescription(code);
			} else if (carrier == MyApplication.CHINANET) {
				code = AuthPortalCT.getInstance().login(user, password);
				result = (code == 50);
				description = AuthPortalCT.getInstance().getDescription(code);
			} else if (carrier == MyApplication.STARBUCKS) {
				code = AuthPortalStar.getInstance().login(user, password,
						LoginProcess.this);
				result = (code == -1);
				description = AuthPortalStar.getInstance().getDescription(code);
			}
			final String desc = description;
			final boolean connectionReady = AuthPortalCT.getInstance()
					.testConnection();
			if (result) {
				LoginProcess.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(LoginProcess.this, "登陆成功",
								Toast.LENGTH_LONG).show();
						builder.append("登录成功！" + "\n");
						builder.append(desc + "\n");
						if (connectionReady) {
							builder.append("测试百度页面打开成功\n");
						} else {
							builder.append("测试百度页面打开失败\n");
						}
						builder.append("请进行下线测试！" + "\n");
						logout.setVisibility(View.VISIBLE);
						show.setText(builder);
					}
				});
			} else {
				LoginProcess.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(LoginProcess.this, "登录失败",
								Toast.LENGTH_LONG).show();
						builder.append("登录失败！" + "\n");
						builder.append(desc + "\n");
						if (connectionReady) {
							builder.append("测试百度页面打开成功\n");
						} else {
							builder.append("测试百度页面打开失败\n");
						}
						builder.append("请进行浏览器测试！" + "\n");
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
			MyApplication mApp = (MyApplication) getApplication();
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
						Toast.makeText(LoginProcess.this, "登出成功",
								Toast.LENGTH_LONG).show();
						builder.append("登出成功！" + "\n");
						builder.append(desc + "\n");
						builder.append("请生成报告！" + "\n");
						show.setText(builder.toString());
						report.setVisibility(View.VISIBLE);
						logout.setVisibility(View.INVISIBLE);
					}
				});
			} else {
				LoginProcess.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(LoginProcess.this, "登出失败，直接下线",
								Toast.LENGTH_LONG).show();
						builder.append("登出失败！" + "\n");
						builder.append(desc + "\n");
						builder.append("请生成报告！" + "\n");
						show.setText(builder.toString());
						report.setVisibility(View.VISIBLE);
						logout.setVisibility(View.INVISIBLE);
					}
				});
			}
		}
	};

	private void showTips() {
		Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle("退出测试");
		alertDialog.setMessage("确定退出本次测试？");
		alertDialog.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent();
						intent.setClass(LoginProcess.this, MainActivity.class);
						startActivity(intent);
					}
				});
		alertDialog.setNegativeButton("取消",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
				});

		alertDialog.create().show();
		; // 创建对话框
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			this.showTips();
			return false;
		}
		return false;
	}

}
