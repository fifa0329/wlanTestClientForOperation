package ui;

import java.io.File;
import com.example.testclient.R;
import engine.DirDel;
import engine.Logger;
import engine.MyIO;
import engine.ZipUtility;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;

public class Report extends Activity {
	ImageView save;
	ProgressDialog progressdialog;
	private String stepstring;
	private int stepint;
	private ImageView report_step;
	private ImageView back;
	SharedPreferences preferences;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report);
		init();

	}

	public void init() {
		preferences = getPreferences(MODE_PRIVATE);

		back = (ImageView) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Report.this.finish();
			}
		});
		report_step = (ImageView) findViewById(R.id.report_step);

		stepstring = getIntent().getStringExtra("step");
		stepint = Integer.parseInt(stepstring);
		switch (stepint) {
		case 1:
			report_step.setImageResource(R.drawable.firststep);
			break;
		case 2:
			report_step.setImageResource(R.drawable.secondstep);
			break;
		case 3:
			report_step.setImageResource(R.drawable.thirdstep);
			break;
		case 4:
			report_step.setImageResource(R.drawable.fourthstep);
			break;

		default:
			break;
		}

		stepint = stepint + 1;

		final EditText name = (EditText) findViewById(R.id.name);
		final EditText address = (EditText) findViewById(R.id.address);
		final EditText comments = (EditText) findViewById(R.id.comments);
		name.setText(preferences.getString("name", ""));
		address.setText(preferences.getString("address", ""));

		save = (ImageView) findViewById(R.id.save);
		save.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				progressdialog = new ProgressDialog(Report.this);
				progressdialog.setMessage("请稍候……");
				progressdialog.show();
				new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						Logger.getInstance().stopLogger();
						String str = engine.PhoneInfo.getPhoneInfo(
								getApplicationContext()).toString();
						MyIO myio = new MyIO("/informations.txt");
						myio.write(str);
						StringBuilder builder = new StringBuilder();
						builder.append("name:").append(name.getText())
								.append("\n");
						builder.append("address:").append(address.getText())
								.append("\n");
						builder.append("comments:").append(comments.getText())
								.append("\n");
						myio.write(builder.toString());
						Editor editor = preferences.edit();
						editor.putString("name", name.getText().toString());
						editor.putString("address", address.getText()
								.toString());
						editor.commit();
						try {
							if (Environment.getExternalStorageState().equals(
									Environment.MEDIA_MOUNTED)) {
								File zipfile = new File(Environment
										.getExternalStorageDirectory()
										.getCanonicalPath()
										+ "/wlantest/report/"
										+ System.currentTimeMillis() + ".zip");
								File directory = new File(Environment
										.getExternalStorageDirectory()
										.getCanonicalPath()
										+ "/wlantest/current/");
								Log.v("CanonicalPath", Environment
										.getExternalStorageDirectory()
										.getCanonicalPath());
								Log.v("getAbsolutePath", Environment
										.getExternalStorageDirectory()
										.getAbsolutePath());
								Log.v("zipfile.getAbsolutePath",
										zipfile.getAbsolutePath());
								Log.v("zipfile.getCanonicalPath()",
										zipfile.getCanonicalPath());
								Log.v("zipfile.getCanonicalPath()",
										Environment
												.getExternalStorageDirectory()
												+ "/wlantest/" + "/report/");
								ZipUtility.zipDirectory(directory, zipfile);
								DirDel.delAllFile(Environment
										.getExternalStorageDirectory()
										.getCanonicalPath()
										+ "/wlantest/current/");
							}
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}

						Intent intent = new Intent();
						intent.setClass(Report.this, MainActivity.class);
						startActivity(intent);
					}
				}).start();

			}
		});

	}

	private void showTips() {
		Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle("退出测试");
		alertDialog.setMessage("确定退出本次测试？");
		alertDialog.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent();
						intent.setClass(Report.this, MainActivity.class);
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
