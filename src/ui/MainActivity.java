package ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.example.testclient.R;
import com.nullwire.trace.ExceptionHandler;

import engine.WifiAdmin;

import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	protected static final String UPLOAD_LOG_URL = "http://wuxiantao.sinaapp.com/wlanlog.php";
	ImageView start_cmcc;
	ImageView start_chinanet;
	ImageView start_cmccedu;
	ImageView wlansetting;
	ImageView start_open1;
	ConnectivityManager mConnectivityManager;
	TextView report_total;
	MyApplication mApp;
	private ProgressDialog progress;
	private long mExitTime;
	private ImageView upload;
	private WifiAdmin mWifiAdmin;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main1);
		init();
		ExceptionHandler.register(this);
//    	ExceptionHandler.register(this, "http://192.168.6.60/loginbsp/devexception.php"); 
	}



	public void init() {
		mWifiAdmin = new WifiAdmin(MainActivity.this);


		mApp=(MyApplication) getApplication();
		upload=(ImageView) findViewById(R.id.upload);
		
		start_cmcc = (ImageView) findViewById(R.id.start_cmcc);
		start_cmcc.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				mApp.setCarrier(MyApplication.CMCC);
				mWifiAdmin.addApProfile("\"CMCC\"");
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, Login.class);
				startActivity(intent);

			}
		});
		start_chinanet = (ImageView) findViewById(R.id.start_chinanet);
		start_chinanet.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				mApp.setCarrier(MyApplication.CHINANET);
				mWifiAdmin.addApProfile("\"ChinaNet\"");
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, Login.class);
				startActivity(intent);

			}
		});
		start_cmccedu = (ImageView) findViewById(R.id.start_cmccedu);
		start_cmccedu.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				mApp.setCarrier(MyApplication.CMCC);
				mWifiAdmin.addApProfile("\"CMCC-EDU\"");
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, Login.class);
				startActivity(intent);

			}
		});
		
		
		
		
		
		
		
		/*
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
		
		*/
		report_total = (TextView) findViewById(R.id.report_total);


		
		start_open1=(ImageView) findViewById(R.id.open1);
		start_open1.setOnClickListener(new OnClickListener() {
			
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
					report_total.setText(""+filetotal.listFiles().length);
					upload.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							final Builder builder = new AlertDialog.Builder(
									MainActivity.this);
							builder.setTitle("上传报告");
							builder.setMessage("是否进行上传？");
							builder.setPositiveButton("确定",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,
												int which) {
											// 进度对话框的秒显用法
											progress = new ProgressDialog(MainActivity.this);  
											progress.setMessage("请稍候……"); 
											progress.show();
											uploadLog();
										}

									});
							builder.setNegativeButton("取消",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,
												int which) {
											return;
										}
									});
							builder.create().show();
							; // 创建对话框
						}
					});
				}
			}
			else
			{
				Toast.makeText(getApplicationContext(), "请挂载SD卡，否则程序无法运行",
						Toast.LENGTH_LONG).show();
				this.finish();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
			

		

		


	}



	
	
	
	
	
	// 用来监听wifi的变化，改变标题栏的报告数量
	public void onResume() {
		
		super.onResume();


		try {
			new File(Environment.getExternalStorageDirectory() + "/wlantest/report/").mkdirs();
			new File(Environment.getExternalStorageDirectory() + "/wlantest/current/").mkdirs();
			File filetotal = new File(Environment.getExternalStorageDirectory() + "/wlantest/report/");
			if (filetotal.listFiles().length != 0) {
				report_total.setText(""+filetotal.listFiles().length);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}


	}

	public void uploadLog() {
		/*
		 * progress = new ProgressDialog(context); progress.setTitle("上传日志");
		 * progress.setMessage("正在上传，请等待...");
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

			    		//建立输出流，并写入数据
			    		OutputStream outputStream = httpConn.getOutputStream();
			    		outputStream.write(b);
			    		outputStream.close();
			    		//获得响应状态
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
										report_total.setText(""+filetotal.listFiles().length);
										Log.v("test", "" + filetotal.listFiles().length);	
									}
									
									else {
										report_total.setText(""+"0");
										progress.dismiss();
									}

								}
							});
			    		}
			    		else {
			    			Toast.makeText(MainActivity.this, "网络质量不佳", Toast.LENGTH_SHORT).show();
						}
						
//						Toast.makeText(MainActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
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
	                                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
	                                mExitTime = System.currentTimeMillis();

	                        } else {
	                        	
	                        	Intent intent=new Intent(Intent.ACTION_MAIN);
	                        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//android提示 如果是服务里调用，必须用new task标示;
	                        	intent.addCategory(Intent.CATEGORY_HOME);//新建一个主屏幕的Intent就可以;
	                        	startActivity(intent);

	

	                        }
	                        return true;
	                }
	                return super.onKeyDown(keyCode, event);
	        }
	


}
