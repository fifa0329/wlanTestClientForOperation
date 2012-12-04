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
		builder.append("���ڵ�¼�С�����" + "\n");
		show.setText(builder.toString());
//����һ��������һ���߳�ר���������е�¼����
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
				builder.append("�������ߡ�����" + "\n");
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
							alertDialog.setTitle("ע�⣡�����˺��Ѿ�����");
							alertDialog.setMessage("�����˺������У�����ע���ϴε�¼��Ȼ���ٴν��в���");
							alertDialog.setPositiveButton("����",
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
						Toast.makeText(LoginProcess.this, "��½�ɹ�",
								Toast.LENGTH_LONG).show();
						builder.append("��¼�ɹ���" + "\n");
						builder.append(desc + "\n");
						if (connectionReady) {
							builder.append("���԰ٶ�ҳ��򿪳ɹ�\n");
						} else {
							builder.append("���԰ٶ�ҳ���ʧ��\n");
						}
						builder.append("��������߲��ԣ�" + "\n");
						logout.setVisibility(View.VISIBLE);
						show.setText(builder);
					}
				});
			} else {
				LoginProcess.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(LoginProcess.this, "��¼ʧ��",
								Toast.LENGTH_LONG).show();
						builder.append("��¼ʧ�ܣ�" + "\n");
						builder.append(desc + "\n");
						if (connectionReady) {
							builder.append("���԰ٶ�ҳ��򿪳ɹ�\n");
						} else {
							builder.append("���԰ٶ�ҳ���ʧ��\n");
						}
						builder.append("�������������ԣ�" + "\n");
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
						Toast.makeText(LoginProcess.this, "�ǳ��ɹ�",
								Toast.LENGTH_LONG).show();
						builder.append("�ǳ��ɹ���" + "\n");
						builder.append(desc + "\n");
						builder.append("�����ɱ��棡" + "\n");
						show.setText(builder.toString());
						report.setVisibility(View.VISIBLE);
						logout.setVisibility(View.INVISIBLE);
					}
				});
			} else {
				LoginProcess.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(LoginProcess.this, "�ǳ�ʧ�ܣ�ֱ������",
								Toast.LENGTH_LONG).show();
						builder.append("�ǳ�ʧ�ܣ�" + "\n");
						builder.append(desc + "\n");
						builder.append("�����ɱ��棡" + "\n");
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
		alertDialog.setTitle("�˳�����");
		alertDialog.setMessage("ȷ���˳����β��ԣ�");
		alertDialog.setPositiveButton("ȷ��",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent();
						intent.setClass(LoginProcess.this, MainActivity.class);
						startActivity(intent);
					}
				});
		alertDialog.setNegativeButton("ȡ��",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
				});

		alertDialog.create().show();
		; // �����Ի���
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			this.showTips();
			return false;
		}
		return false;
	}

}
