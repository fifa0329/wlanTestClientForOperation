package ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


import com.example.testclient.R;
import com.nullwire.trace.ExceptionHandler;

import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	protected static final String UPLOAD_LOG_URL = "http://wuxiantao.sinaapp.com/wlanlog.php";
	Button to_cmcc;
	Button to_chinanet;
	Button wlansetting;
	Button exit;
	Button to_else;
	TextView ssid;
	ConnectivityManager mConnectivityManager;
	Button report_total;
	MyApplication mApp;
	private ProgressDialog progress;
	private long mExitTime;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
//    	ExceptionHandler.register(this, "http://192.168.6.60/loginbsp/devexception.php"); 
	}



	public void init() {
		mApp = (MyApplication) getApplication();
		mApp.setExit(false);
		to_cmcc = (Button) findViewById(R.id.to_cmcc);
		to_cmcc.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				mApp.setCarrier(MyApplication.CMCC);
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, Login.class);
				startActivity(intent);

			}
		});
		to_chinanet = (Button) findViewById(R.id.to_chinanet);
		to_chinanet.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				mApp.setCarrier(MyApplication.CHINANET);
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, Login.class);
				startActivity(intent);

			}
		});
		wlansetting = (Button) findViewById(R.id.wlan_setting);
		wlansetting.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				// mWifiAdmin.connect();
				// startActivityForResult(new Intent(
				// android.provider.Settings.ACTION_WIFI_SETTINGS), 0);
				startActivity(new Intent(
						android.provider.Settings.ACTION_WIFI_SETTINGS));
				// gprsEnable(false);
			}
		});
		ssid = (TextView) findViewById(R.id.ssid);
		report_total = (Button) findViewById(R.id.report_total);

		Button exit = (Button) findViewById(R.id.exit);
		exit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				MyApplication mApp = (MyApplication) getApplication();

				mApp.setExit(true);

				finish();
			}
		});
		
		to_else=(Button) findViewById(R.id.to_else);
		to_else.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, Browser.class);
				startActivity(intent);
				
			}
		});
		
		try {
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			{
				new File(Environment.getExternalStorageDirectory() + "/wlantest/report/").mkdirs();
				new File(Environment.getExternalStorageDirectory() + "/wlantest/current/").mkdirs();

				File filetotal = new File(Environment.getExternalStorageDirectory() + "/wlantest/"
						+ "/report/");

				if (filetotal.listFiles().length != 0) {
					report_total.setText("��ǰ���Ѿ�����" + filetotal.listFiles().length
							+ "������" + "\n" + "�ϴ������");
					report_total.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							final Builder builder = new AlertDialog.Builder(
									MainActivity.this);
							builder.setTitle("�ϴ�����");
							builder.setMessage("�Ƿ�����ϴ���");
							builder.setPositiveButton("ȷ��",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,
												int which) {
											// ���ȶԻ���������÷�
											progress = new ProgressDialog(MainActivity.this);  
											progress.setMessage("���Ժ򡭡�"); 
											progress.show();
											uploadLog();
										}

									});
							builder.setNegativeButton("ȡ��",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,
												int which) {
											return;
										}
									});
							builder.create().show();
							; // �����Ի���
						}
					});
				}
			}
			else
			{
				Toast.makeText(getApplicationContext(), "�����SD������������޷�����",
						Toast.LENGTH_LONG).show();
				this.finish();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
			

		

		


	}

	// ����ģ�����ڹ���gprs���Զ�����
	public final void setMobileNetEnable() {

		mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		Object[] arg = null;
		try {
			boolean isMobileDataEnable = invokeMethod("getMobileDataEnabled",
					arg);
			if (!isMobileDataEnable) {
				invokeBooleanArgMethod("setMobileDataEnabled", true);

			}
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public final void setMobileNetUnable() {
		mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		Object[] arg = null;
		try {
			boolean isMobileDataEnable = invokeMethod("getMobileDataEnabled",
					arg);
			if (isMobileDataEnable) {
				invokeBooleanArgMethod("setMobileDataEnabled", false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean invokeMethod(String methodName, Object[] arg)
			throws Exception {

		ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		Class ownerClass = mConnectivityManager.getClass();

		Class[] argsClass = null;
		if (arg != null) {
			argsClass = new Class[1];
			argsClass[0] = arg.getClass();
		}

		Method method = ownerClass.getMethod(methodName, argsClass);

		Boolean isOpen = (Boolean) method.invoke(mConnectivityManager, arg);

		return isOpen;
	}

	public Object invokeBooleanArgMethod(String methodName, boolean value)
			throws Exception {

		ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		Class ownerClass = mConnectivityManager.getClass();

		Class[] argsClass = new Class[1];
		argsClass[0] = boolean.class;

		Method method = ownerClass.getMethod(methodName, argsClass);

		return method.invoke(mConnectivityManager, value);
	}

	// GPRS�Զ���������

	
	
	
	
	
	// ��������wifi�ı仯���ı�������ı�������
	public void onResume() {
		super.onResume();
		ssid.setText("��");
		to_cmcc.setVisibility(View.INVISIBLE);
		to_chinanet.setVisibility(View.INVISIBLE);
		WifiManager mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
		if(mWifiManager.isWifiEnabled())
		{
			if(mWifiInfo.getSSID().equals((String)""))
			{
				ssid.setText("��");
			}
			else
			{
			ssid.setText(mWifiInfo.getSSID());
			}
		}
		// setMobileNetEnable();


		// �����������ڸ��������жϰ�ť�Ŀ�ѡ����
		try {
			if (mWifiInfo.getSSID().equals((String) "CMCC")) {
				to_cmcc.setVisibility(View.VISIBLE);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}// SHOW the button
		
		
		try {
			if (mWifiInfo.getSSID().equals((String) "ChinaNet")) {
				to_chinanet.setVisibility(View.VISIBLE);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}// SHOW the button
		
		
		try {
			if (mWifiInfo.getSSID().equals((String) "CMCC-EDU")) {
				to_cmcc.setVisibility(View.VISIBLE);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}// SHOW the button
		
		

		try {
			new File(Environment.getExternalStorageDirectory() + "/wlantest/report/").mkdirs();
			new File(Environment.getExternalStorageDirectory() + "/wlantest/current/").mkdirs();
			File filetotal = new File(Environment.getExternalStorageDirectory() + "/wlantest/report/");
			if (filetotal.listFiles().length != 0) {
				report_total.setText("��ǰ��������" + filetotal.listFiles().length
						+ "" + "\n" + "�����ϴ�");
			}
		} catch (Exception e) {
			// TODO: handle exception
		}


	}

	public void uploadLog() {
		/*
		 * progress = new ProgressDialog(context); progress.setTitle("�ϴ���־");
		 * progress.setMessage("�����ϴ�����ȴ�...");
		 * progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		 * progress.show();
		 */
		new Thread(new Runnable() {
			private int i;
			File file=new File(Environment.getExternalStorageDirectory()
					+ "/wlantest/" + "/report/");
			File[] files=file.listFiles();

			@Override
			public void run() {
				try {
					URL url;
					url = new URL(UPLOAD_LOG_URL);
					
					

					for(i=0;i<files.length;i++){
		    			Log.v("test", "" + files.length);

						HttpURLConnection httpConn;
						httpConn = (HttpURLConnection)url.openConnection();
						httpConn.setDoOutput(true);
						httpConn.setDoInput(true);
						httpConn.setUseCaches(false);
						httpConn.setRequestMethod("POST");
						
						FileInputStream inputStream = new FileInputStream(files[i].getCanonicalFile());  
			            byte[] b = new byte[inputStream.available()];  
			            inputStream.read(b);

			    		httpConn.setRequestProperty("Content-length", Integer.toString(b.length));
			    		httpConn.setRequestProperty("Content-Type", "application/x-zip-compressed");
			    		httpConn.setRequestProperty("Connection", "Keep-Alive");

			    		//�������������д������
			    		OutputStream outputStream = httpConn.getOutputStream();
			    		outputStream.write(b);
			    		outputStream.close();
			    		//�����Ӧ״̬
			    		int responseCode = httpConn.getResponseCode();
			    		if(HttpURLConnection.HTTP_OK == responseCode){
			    			Log.v("test", "" + responseCode);
							MainActivity.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									files[i].delete();
									File filetotal = new File(Environment.getExternalStorageDirectory() + "/wlantest/report/");
									if (filetotal.listFiles().length != 0) 
									
									{
										report_total.setText("��ǰ��������" + filetotal.listFiles().length
												+ "" + "\n" + "�����ϴ�");
										Log.v("test", "" + filetotal.listFiles().length);	
									}
									
									else {
										report_total.setText("��ǰ��������0\n�����ϴ�");
										progress.dismiss();
									}

								}
							});
			    		}
			    		else {
			    			Toast.makeText(MainActivity.this, "������������", Toast.LENGTH_SHORT).show();
						}
						
//						Toast.makeText(MainActivity.this, "�ϴ��ɹ�", Toast.LENGTH_SHORT).show();
						try {
							Thread.sleep(2000);
							
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}

				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}).start();
	}




	        public boolean onKeyDown(int keyCode, KeyEvent event) {
	                if (keyCode == KeyEvent.KEYCODE_BACK) {
	                        if ((System.currentTimeMillis() - mExitTime) > 2000) {
	                                Toast.makeText(this, "�ٰ�һ���˳�����", Toast.LENGTH_SHORT).show();
	                                mExitTime = System.currentTimeMillis();

	                        } else {
	                        	MyApplication mApp = (MyApplication) getApplication();

	                			mApp.setExit(true);

	                			finish();    
	                        	finish();
	                        }
	                        return true;
	                }
	                return super.onKeyDown(keyCode, event);
	        }
	

	protected void onStart() {

		super.onStart();

		MyApplication mApp = (MyApplication) getApplication();

		if (mApp.isExit()) {

			finish();

		}

	}
}
