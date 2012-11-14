package ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.example.testclient.R;

import engine.WifiAdmin;

import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
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
	protected static final String UPLOAD_LOG_URL = "http://wuxiantao.sinaapp.com/devapi/report_submit.php";
	ImageView to_cmcc;
	ImageView to_chinanet;
	ImageView to_cmccedu;
	ImageView wlansetting;
	ConnectivityManager mConnectivityManager;
	TextView report_total;
	MyApplication mApp;
	private ProgressDialog progress;
	private long mExitTime;
	private ImageView upload;
	private WifiAdmin mWifiAdmin;
	TextView[] text_open=new TextView[5];
	ImageView[] to_open=new ImageView[5];
	ImageView[] view_open=new ImageView[5];
	ArrayList<String> text_opens;
	ImageView view_cmcc;
	ImageView view_cmccedu;
	ImageView view_chinanet;
	ImageView to_starbucks;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main1);
		init();

//    	ExceptionHandler.register(this); 
	}



	public void init() {

		to_starbucks=(ImageView) findViewById(R.id.to_starbucks);
		to_starbucks.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mApp.setCarrier(MyApplication.STARBUCKS);
				Intent intent=new Intent();
				intent.setClass(MainActivity.this, Login.class);
				startActivity(intent);
			}
		});
		
		mWifiAdmin = new WifiAdmin(MainActivity.this);


		mApp=(MyApplication) getApplication();
		upload=(ImageView) findViewById(R.id.upload);
		
		to_cmcc = (ImageView) findViewById(R.id.to_cmcc);
		to_cmcc.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				mApp.setCarrier(MyApplication.CMCC);
//				mWifiAdmin.addApProfile("\"CMCC\"");
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, Login.class);
				startActivity(intent);

			}
		});
		to_chinanet = (ImageView) findViewById(R.id.to_chinanet);
		to_chinanet.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				mApp.setCarrier(MyApplication.CHINANET);
				mWifiAdmin.addApProfile("\"ChinaNet\"");
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, Login.class);
				startActivity(intent);

			}
		});
		to_cmccedu = (ImageView) findViewById(R.id.to_cmccedu);
		to_cmccedu.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				mApp.setCarrier(MyApplication.CMCC);
				mWifiAdmin.addApProfile("\"CMCC-EDU\"");
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, Login.class);
				startActivity(intent);

			}
		});
		
		
		
		
		
		

		report_total = (TextView) findViewById(R.id.report_total);

		text_open[0] = (TextView) findViewById(R.id.text_open0);
		text_open[1] = (TextView) findViewById(R.id.text_open1);
		text_open[2] = (TextView) findViewById(R.id.text_open2);
		text_open[3] = (TextView) findViewById(R.id.text_open3);
		text_open[4] = (TextView) findViewById(R.id.text_open4);
		
		
		
		
		to_open[0] =(ImageView) findViewById(R.id.to_open0);
		to_open[1] =(ImageView) findViewById(R.id.to_open1);
		to_open[2] =(ImageView) findViewById(R.id.to_open2);
		to_open[3] =(ImageView) findViewById(R.id.to_open3);
		to_open[4] =(ImageView) findViewById(R.id.to_open4);
		
		
		view_open[0] =(ImageView) findViewById(R.id.view_open0);
		view_open[1] =(ImageView) findViewById(R.id.view_open1);
		view_open[2] =(ImageView) findViewById(R.id.view_open2);
		view_open[3] =(ImageView) findViewById(R.id.view_open3);
		view_open[4] =(ImageView) findViewById(R.id.view_open4);
		
		
		view_cmcc =(ImageView) findViewById(R.id.view_cmcc);
		view_cmccedu =(ImageView) findViewById(R.id.view_cmccedu);
		view_chinanet =(ImageView) findViewById(R.id.view_chinanet);

		




		

		
		
		
		
		try {
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			{
				new File(Environment.getExternalStorageDirectory() + "/wlantest/report/").mkdirs();
				new File(Environment.getExternalStorageDirectory() + "/wlantest/current/").mkdirs();

				File filetotal = new File(Environment.getExternalStorageDirectory() + "/wlantest/"
						+ "/report/");

				if (filetotal.listFiles().length != 0) 
				{
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
				else
				{
					report_total.setText(""+filetotal.listFiles().length);
					upload.setImageResource(R.drawable.uploaded);
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
			
		
		
		
		final Handler myhandler=new Handler(){
			public void handleMessage(Message msg) {
				if (msg.what == 0){
					WifiAdmin wifiadmin=new WifiAdmin(MainActivity.this);
					wifiadmin.scan();
					List<ScanResult> listResult;
					listResult = wifiadmin.getWifiManager().getScanResults();
					view_cmcc.setImageResource(R.drawable.wifi3);
					view_cmccedu.setImageResource(R.drawable.wifi3);
					view_chinanet.setImageResource(R.drawable.wifi3);
					to_cmcc.setImageResource(R.drawable.start_test1);
					to_cmccedu.setImageResource(R.drawable.start_test1);
					to_chinanet.setImageResource(R.drawable.start_test1);
					text_opens=new ArrayList<String>();
					if (listResult != null) {
						for (int i = 0; i < listResult.size(); i++) 
						{
							Log.v("scanresult", ""+listResult.get(i).toString());
							if( listResult.get(i).capabilities.equals("[ESS]") && listResult.get(i).SSID!="CMCC" && !listResult.get(i).SSID.equals((String)"CMCC-EDU") && !listResult.get(i).SSID.equals((String)"ChinaNet"))
							{
								{
									text_opens.add(listResult.get(i).SSID);
								}
							}
							if( listResult.get(i).SSID.equals("CMCC"))
							{
								view_cmcc.setImageResource(R.drawable.wifi1);
								to_cmcc.setImageResource(R.drawable.start_test2);
							}
							if( listResult.get(i).SSID.equals("CMCC-EDU"))
							{
								view_cmcc.setImageResource(R.drawable.wifi1);
								to_cmccedu.setImageResource(R.drawable.start_test2);
							}
							if( listResult.get(i).SSID.equals("ChinaNet"))
							{
								view_cmcc.setImageResource(R.drawable.wifi1);
								to_chinanet.setImageResource(R.drawable.start_test2);
							}

						}
						
						if(text_opens.size()!=0 && text_opens!=null)
						{
	
							for(int text_num=0,text_opens_size=0;text_num< text_opens.size();text_num++,text_opens_size++){
								if(text_opens_size<5)
								{
									text_open[0].setText("无");
									text_open[1].setText("无");
									text_open[2].setText("无");
									text_open[3].setText("无");
									text_open[4].setText("无");
									view_open[0].setImageResource(R.drawable.wifi3);
									view_open[1].setImageResource(R.drawable.wifi3);
									view_open[2].setImageResource(R.drawable.wifi3);
									view_open[3].setImageResource(R.drawable.wifi3);
									view_open[4].setImageResource(R.drawable.wifi3);
									to_open[0].setImageResource(R.drawable.start_test1);
									to_open[1].setImageResource(R.drawable.start_test1);
									to_open[2].setImageResource(R.drawable.start_test1);
									to_open[3].setImageResource(R.drawable.start_test1);
									to_open[4].setImageResource(R.drawable.start_test1);
									Log.v("text_opens.get(text_num)", text_opens.get(text_num));
									text_open[text_num].setText(text_opens.get(text_num));
//									wifi1代表信号最强3代表没有信号
									view_open[text_num].setImageResource(R.drawable.wifi1);
									to_open[text_num].setImageResource(R.drawable.start_test2);
									to_open[text_num].setTag(text_num);
									to_open[text_num].setOnClickListener(new OnClickListener() {
										private ProgressDialog progressdialog;

										@Override
										public void onClick(View arg0) {
											final int text_num = (Integer)arg0.getTag();
											// TODO Auto-generated method stub
											progressdialog = new ProgressDialog(MainActivity.this);  
											progressdialog.setMessage("请稍候……"); 
											progressdialog.show();
											new Thread(new Runnable() 
											{
												public void run() 
												{
													mWifiAdmin.addApProfile("\""+text_opens.get(text_num)+"\"");
													Log.v("addapprofile", "\""+text_opens.get(text_num)+"\"");
													try 
													{
														Thread.sleep(5000);
													} catch (InterruptedException e) 
													{
														// TODO Auto-generated catch block
														e.printStackTrace();
													}
													
													WifiManager mWifiManager = (WifiManager)MainActivity.this.getSystemService(Context.WIFI_SERVICE);
													WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
												    boolean isConnected=mWifiInfo.getSupplicantState().equals(SupplicantState.COMPLETED);
													if(isConnected)
													{
														Intent intent = new Intent();
														progressdialog.dismiss();
														intent.putExtra("step", "1");
														intent.setClass(MainActivity.this, Browser.class);
														startActivity(intent);
													}
													else
													{
														MainActivity.this.runOnUiThread(new Runnable() 
														{
															@Override
															public void run() 
															{
																progressdialog.dismiss();
																Toast.makeText(MainActivity.this, "连接失败，请重试", Toast.LENGTH_LONG).show();
															}
														});
													}
													
												};
											}).start();

										}
									});
								}


							}
						}
//						if(text_opens.size()!=0 && text_opens!=null)
						else{

							text_open[0].setText("无");
							text_open[1].setText("无");
							text_open[2].setText("无");
							text_open[3].setText("无");
							text_open[4].setText("无");
							view_open[0].setImageResource(R.drawable.wifi3);
							view_open[1].setImageResource(R.drawable.wifi3);
							view_open[2].setImageResource(R.drawable.wifi3);
							view_open[3].setImageResource(R.drawable.wifi3);
							view_open[4].setImageResource(R.drawable.wifi3);
							to_open[0].setImageResource(R.drawable.start_test1);
							to_open[1].setImageResource(R.drawable.start_test1);
							to_open[2].setImageResource(R.drawable.start_test1);
							to_open[3].setImageResource(R.drawable.start_test1);
							to_open[4].setImageResource(R.drawable.start_test1);
						}

					
					}
//					if (listResult != null)
					else{
						view_cmcc.setImageResource(R.drawable.wifi3);
						view_cmccedu.setImageResource(R.drawable.wifi3);
						view_chinanet.setImageResource(R.drawable.wifi3);
						to_cmcc.setImageResource(R.drawable.start_test1);
						to_cmccedu.setImageResource(R.drawable.start_test1);
						to_chinanet.setImageResource(R.drawable.start_test1);
						text_open[0].setText("无");
						text_open[1].setText("无");
						text_open[2].setText("无");
						text_open[3].setText("无");
						text_open[4].setText("无");
						view_open[0].setImageResource(R.drawable.wifi3);
						view_open[1].setImageResource(R.drawable.wifi3);
						view_open[2].setImageResource(R.drawable.wifi3);
						view_open[3].setImageResource(R.drawable.wifi3);
						view_open[4].setImageResource(R.drawable.wifi3);
						to_open[0].setImageResource(R.drawable.start_test1);
						to_open[1].setImageResource(R.drawable.start_test1);
						to_open[2].setImageResource(R.drawable.start_test1);
						to_open[3].setImageResource(R.drawable.start_test1);
						to_open[4].setImageResource(R.drawable.start_test1);
					}
//				if (msg.what == 0){
				}
				
				
				
				
				
				
				
			}
		};
		
		
		
		
		
		
		
		
		
		 new Timer().scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Message msg =new Message();
				msg.what=0;
				myhandler.sendMessage(msg);
			}
		}, 0, 60000);

		

		


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
				upload.setImageResource(R.drawable.upload1);
			}
			else
			{
				report_total.setText(""+filetotal.listFiles().length);
				upload.setImageResource(R.drawable.uploaded);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}


	}

	public void uploadLog() {

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
					
					

					for(i=0;i<files.length;i++)
					{
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
									try {
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
											upload.setImageResource(R.drawable.uploaded);
										}
									} catch (ArrayIndexOutOfBoundsException e) {
										// TODO: handle exception
										
										
									}


								}
							});
			    		}
			    		else {
			    			Toast.makeText(MainActivity.this, "网络质量不佳", Toast.LENGTH_SHORT).show();
						}
						
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
