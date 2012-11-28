package ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import com.example.testclient.R;
import com.nullwire.trace.ExceptionHandler;

import engine.Downloader;
import engine.Logger;
import engine.WifiAdmin;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	protected static final String UPLOAD_LOG_URL = "http://wuxiantao.sinaapp.com/devapi/report_submit.php";
	private static final int MENU_UPDATE_DB = 0;
	private static String LOGIN_TEST_URL = "http://www.baidu.com";
	private static String LOGIN_TEST_SIGNATURE = "news.baidu.com";
	public static final String REPORT_DIR = "/wlantest/report/";
	public static final String CURRENT_DIR = "/wlantest/current/";
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
	ArrayList<HashMap<String, Object>> text_opens;
	ImageView view_cmcc;
	ImageView view_cmccedu;
	ImageView view_chinanet;
	ImageView to_starbucks;
	TableRow[] tablerow=new TableRow[5];
	private ImageView view_starbucks;
	private Downloader downloader;
	protected int db_version;
	protected SharedPreferences preferences;



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main1);
		init();
    	ExceptionHandler.register(this); 
	}



	public void init() {


		Logger.getInstance().startLogger();
		mWifiAdmin = new WifiAdmin(MainActivity.this);
		mWifiAdmin.openNetCard();
		tablerow[0]=(TableRow) findViewById(R.id.tableRow0);
		tablerow[1]=(TableRow) findViewById(R.id.tableRow1);
		tablerow[2]=(TableRow) findViewById(R.id.tableRow2);
		tablerow[3]=(TableRow) findViewById(R.id.tableRow3);
		tablerow[4]=(TableRow) findViewById(R.id.tableRow4);
		

		

		mApp=(MyApplication) getApplication();
		upload=(ImageView) findViewById(R.id.upload);
		
		to_cmcc = (ImageView) findViewById(R.id.to_cmcc);
		to_cmcc.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				if((Boolean) arg0.getTag())
				{
					mWifiAdmin.openNetCard();
					mApp.setCarrier(MyApplication.CMCC);
					mWifiAdmin.addApProfile("\"CMCC\"");
					Intent intent = new Intent();
					intent.setClass(MainActivity.this, Login.class);
					startActivity(intent);
				}
			}
		});
		to_chinanet = (ImageView) findViewById(R.id.to_chinanet);
		to_chinanet.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				if((Boolean) v.getTag())
				{
					mWifiAdmin.openNetCard();
					mApp.setCarrier(MyApplication.CHINANET);
					mWifiAdmin.addApProfile("\"ChinaNet\"");
					Intent intent = new Intent();
					intent.setClass(MainActivity.this, Login.class);
					startActivity(intent);
				}


			}
		});
		to_cmccedu = (ImageView) findViewById(R.id.to_cmccedu);
		to_cmccedu.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				if((Boolean) arg0.getTag())
				{
					mWifiAdmin.openNetCard();
					mApp.setCarrier(MyApplication.CMCC);
					mWifiAdmin.addApProfile("\"CMCC-EDU\"");
					Intent intent = new Intent();
					intent.setClass(MainActivity.this, Login.class);
					startActivity(intent);
				}

			}
		});


		to_starbucks=(ImageView) findViewById(R.id.to_starbucks);

		to_starbucks.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) 
			{
				if((Boolean) arg0.getTag())
				{
					// TODO Auto-generated method stub
					mApp.setCarrier(MyApplication.STARBUCKS);
					Intent intent=new Intent();
					intent.setClass(MainActivity.this, Login.class);
					startActivity(intent);
				}

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
		view_starbucks=(ImageView) findViewById(R.id.view_starbucks);
		




		

		
		
		
		
		try {
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			{
				new File(Environment.getExternalStorageDirectory() + REPORT_DIR).mkdirs();
				new File(Environment.getExternalStorageDirectory() + CURRENT_DIR).mkdirs();

				File filetotal = new File(Environment.getExternalStorageDirectory() + REPORT_DIR);

				if (filetotal.listFiles().length != 0) 
				{
					report_total.setText(""+filetotal.listFiles().length);
					upload.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) 
						{
							// TODO Auto-generated method stub
							if((Boolean) arg0.getTag())
							{
								final Builder builder = new AlertDialog.Builder(
										MainActivity.this);
								builder.setTitle("是否进行上传？");
								builder.setMessage("你可在“设置”选择一个可联网的WIFI进行上传"+"\n"+"或者在“设置”中关闭wifi，选择数据流量上传");
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
								builder.setNeutralButton("设置", new DialogInterface.OnClickListener() {
									

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										startActivity(new Intent(
												android.provider.Settings.ACTION_WIFI_SETTINGS));
									}
								});
								builder.create().show();
								; // 创建对话框
							}
							
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
			
		
		
//		动态刷新公共AP
		final Handler myhandler=new Handler(){

			public void handleMessage(Message msg) {
				if (msg.what == 0){
					WifiAdmin wifiadmin=new WifiAdmin(MainActivity.this);
					wifiadmin.scan();
					List<ScanResult> listResult;
					listResult = wifiadmin.getWifiManager().getScanResults();
					view_cmcc.setImageResource(R.drawable.wifi_none);
					view_cmccedu.setImageResource(R.drawable.wifi_none);
					view_chinanet.setImageResource(R.drawable.wifi_none);
					view_starbucks.setImageResource(R.drawable.wifi_none);
					to_cmcc.setImageResource(R.drawable.start_test1);
					to_cmccedu.setImageResource(R.drawable.start_test1);
					to_chinanet.setImageResource(R.drawable.start_test1);
					to_starbucks.setImageResource(R.drawable.start_test1);
					tablerow[0].setVisibility(View.INVISIBLE);
					tablerow[1].setVisibility(View.INVISIBLE);
					tablerow[2].setVisibility(View.INVISIBLE);
					tablerow[3].setVisibility(View.INVISIBLE);
					tablerow[4].setVisibility(View.INVISIBLE);
//					设置tag判断可否点击
					to_cmcc.setTag(false);
					to_cmccedu.setTag(false);
					to_chinanet.setTag(false);
					to_starbucks.setTag(false);
					
					
					text_opens=new ArrayList<HashMap<String, Object>>();
					if (listResult != null) {
						for (int i = 0; i < listResult.size(); i++) 
						{
							Log.v("scanresult", ""+listResult.get(i).toString());
//扫描不为公共运营商的AP
							if( (listResult.get(i).capabilities.equals("[ESS]")  || listResult.get(i).capabilities.equals("")) && !listResult.get(i).SSID.equals((String)"CMCC") && !listResult.get(i).SSID.equals((String)"CMCC-EDU") && !listResult.get(i).SSID.equals((String)"ChinaNet") && !listResult.get(i).SSID.equals((String)"ChinaUnicom"))

							{
								HashMap<String, Object> result=new HashMap<String, Object>();
								result.put("SSID", listResult.get(i).SSID);
								result.put("level", listResult.get(i).level);
								text_opens.add(result);
							}
//							几大公共运营商
							if( listResult.get(i).SSID.equals("CMCC"))
							{
								
								to_cmcc.setImageResource(R.drawable.start_test2);
								to_cmcc.setTag(true);
								if(listResult.get(i).level>-80)
								{
									view_cmcc.setImageResource(R.drawable.wifi_strong);
								}
								else 
								{
									view_cmcc.setImageResource(R.drawable.wifi_weak);
								}
								

							}
							if( listResult.get(i).SSID.equals("CMCC-EDU"))
							{
								to_cmccedu.setTag(true);
								to_cmccedu.setImageResource(R.drawable.start_test2);
								if(listResult.get(i).level>-80)
								{
									view_cmcc.setImageResource(R.drawable.wifi_strong);
								}
								else 
								{
									view_cmcc.setImageResource(R.drawable.wifi_weak);
								}
							}
							if( listResult.get(i).SSID.equals("ChinaNet"))
							{
								to_chinanet.setTag(true);
								to_chinanet.setImageResource(R.drawable.start_test2);
								if(listResult.get(i).level>-80)
								{
									view_cmcc.setImageResource(R.drawable.wifi_strong);
								}
								else 
								{
									view_cmcc.setImageResource(R.drawable.wifi_weak);
								}
							}
							if( listResult.get(i).SSID.equals("ChinaNet-Starbucks"))
							{
								
								to_starbucks.setTag(true);
								to_starbucks.setImageResource(R.drawable.start_test2);
								if(listResult.get(i).level>-80)
								{
									view_cmcc.setImageResource(R.drawable.wifi_strong);
								}
								else 
								{
									view_cmcc.setImageResource(R.drawable.wifi_weak);
								}
							}

						}
//						以下是用来处理开放热点的情况的
						if(text_opens.size()!=0)
						{
	
							for(int text_num=0,text_opens_size=0;text_num< text_opens.size();text_num++,text_opens_size++){
								if(text_opens_size<5)
								{

									tablerow[text_opens_size].setVisibility(View.VISIBLE);

									
									text_open[text_num].setText((String)text_opens.get(text_num).get("SSID"));
									
									
									if((Integer)text_opens.get(text_num).get("level")>-80)
									{
										view_open[text_num].setImageResource(R.drawable.wifi_strong);
									}
									else 
									{
										view_open[text_num].setImageResource(R.drawable.wifi_weak);
									}
									
									
									
									
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
													mWifiAdmin.openNetCard();
													mWifiAdmin.addApProfile("\""+text_opens.get(text_num).get("SSID")+"\"");
													Log.v("addapprofile", "\""+text_opens.get(text_num).get("SSID")+"\"");
												    boolean isConnected=((WifiManager) MainActivity.this.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getSupplicantState().equals(SupplicantState.COMPLETED);
													while(isConnected==false)
													{
														try {
															Thread.sleep(1000);
															isConnected=((WifiManager) MainActivity.this.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getSupplicantState().equals(SupplicantState.COMPLETED);
														} catch (InterruptedException e) {
															// TODO Auto-generated catch block
															e.printStackTrace();
														}
													}
												    

														DefaultHttpClient client = new DefaultHttpClient();
														HttpResponse response;
														try {
															response = client.execute(new HttpGet(LOGIN_TEST_URL));
															String output = EntityUtils.toString(response.getEntity(), "GBK");
															if (output.contains(LOGIN_TEST_SIGNATURE))
															{
																MainActivity.this.runOnUiThread(new Runnable() 
																{
																	@Override
																	public void run() 
																	{
																		progressdialog.dismiss();
																		Toast.makeText(MainActivity.this, "可以直接接入网络的热点就不需要测试啦！", Toast.LENGTH_LONG).show();
																	}
																});
															}
															else
															{
																Intent intent = new Intent();
																progressdialog.dismiss();
																intent.putExtra("step", "1");
																intent.setClass(MainActivity.this, Browser.class);
																startActivity(intent);
															}

														} catch (ClientProtocolException e1) {
															// TODO Auto-generated catch block
															e1.printStackTrace();
														} catch (IOException e1) {
															// TODO Auto-generated catch block
															e1.printStackTrace();
														}
												};
											}).start();

										}
									});
								}


							}
						}
//						if(text_opens.size()!=0)
						else{
							tablerow[0].setVisibility(View.INVISIBLE);
							tablerow[1].setVisibility(View.INVISIBLE);
							tablerow[2].setVisibility(View.INVISIBLE);
							tablerow[3].setVisibility(View.INVISIBLE);
							tablerow[4].setVisibility(View.INVISIBLE);

						}

					
					}
//					if (listResult != null)
					else{
						view_cmcc.setImageResource(R.drawable.wifi_none);
						view_cmccedu.setImageResource(R.drawable.wifi_none);
						view_chinanet.setImageResource(R.drawable.wifi_none);
						view_starbucks.setImageResource(R.drawable.wifi_none);
						to_cmcc.setImageResource(R.drawable.start_test1);
						to_cmccedu.setImageResource(R.drawable.start_test1);
						to_chinanet.setImageResource(R.drawable.start_test1);
						to_starbucks.setImageResource(R.drawable.start_test1);
						tablerow[0].setVisibility(View.INVISIBLE);
						tablerow[1].setVisibility(View.INVISIBLE);
						tablerow[2].setVisibility(View.INVISIBLE);
						tablerow[3].setVisibility(View.INVISIBLE);
						tablerow[4].setVisibility(View.INVISIBLE);

					}
				}
				
				
				
				
				
				
				
			}
		};
		
		
		
		
		
		
		
		
//		定时任务的点火器，每60s执行任务
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



	
	
	
	
	
	// 用来监听wifi的变化，改变标题栏的报告数量，用来判断是否存在初始数据库
	public void onResume() {
		super.onResume();
		try {
			new File(Environment.getExternalStorageDirectory() + REPORT_DIR).mkdirs();
			new File(Environment.getExternalStorageDirectory() + CURRENT_DIR).mkdirs();
			File filetotal = new File(Environment.getExternalStorageDirectory() + REPORT_DIR);
			if (filetotal.listFiles().length != 0) {
				report_total.setText(""+filetotal.listFiles().length);
				upload.setImageResource(R.drawable.upload1);
				upload.setTag(true);
			}
			else
			{
				report_total.setText(""+filetotal.listFiles().length);
				upload.setImageResource(R.drawable.uploaded);
			}
			downloader = new Downloader(MainActivity.this);
			if (downloader.isDatabaseExisted()==false) {
				Dialog dialog = new AlertDialog.Builder(MainActivity.this)
					.setTitle("数据下载")
					.setMessage("未找到数据库，是否下载？")
					.setPositiveButton("下载", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							downloader.downloadDatabase();
							db_version=1;
							preferences =getSharedPreferences("version", MODE_PRIVATE);
							Editor edit=preferences.edit();
							edit.putInt("db_version", db_version);
							edit.commit();
						}})
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}})
					.create();
				dialog.show();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	
	
	
//	上传当前手机内部已有报告的模块
	public void uploadLog() {

		new Thread(new Runnable() {
			private int i;
			File file=new File(Environment.getExternalStorageDirectory()
					+ "/wlantest/" + "/report/");
			File[] files=file.listFiles();
			@Override
			public void run() 
			{
				try {
					if (isNetworkConnected(MainActivity.this)==true)
					{
						URL url;
						url = new URL(UPLOAD_LOG_URL);
//???上传模块需要进行一下调试 具体理解过程
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
//				    		对于单个文件的上传过程如下：
				    		if(HttpURLConnection.HTTP_OK == responseCode){
//				    			用于一个文件上传成功后修改UI实现动画显示
				    			MainActivity.this.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										try {
											files[i].delete();
											File filetotal = new File(Environment.getExternalStorageDirectory() + REPORT_DIR);
											if (filetotal.listFiles().length != 0) 
											{
												report_total.setText(""+filetotal.listFiles().length);
												Log.v("test", "" + filetotal.listFiles().length);	
											}
//											关键一步，上传完所有文件的显示
											else {
												report_total.setText(""+"0");
												progress.dismiss();
												upload.setImageResource(R.drawable.uploaded);
												Toast toast=Toast.makeText(MainActivity.this, "上传成功", Toast.LENGTH_SHORT);
												toast.show();
												upload.setTag(false);
											}
										} catch (ArrayIndexOutOfBoundsException e) {
											// TODO: handle exception
										}


									}
								});
				    		}
				    		else 
				    		{
								Logger.getInstance().writeLog(""+responseCode);
				    			MainActivity.this.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										Toast.makeText(MainActivity.this, "该文件上传失败，与服务器失去连接", Toast.LENGTH_SHORT).show();
										progress.dismiss();
									}
								});
				    			break;
//				    			用于第一个文件上传失败后退出循环体，且给予用户提示，是服务器端的问题
							}
							
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}//for循环体
					}
//					一旦发现当前没有网络连接的处理方法
					else
					{
						MainActivity.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(MainActivity.this, "当前网络不可用，请检查GPRS后再试", Toast.LENGTH_SHORT).show();
								progress.dismiss();
							}
						});
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



//连按两下返回键退出程序
	        public boolean onKeyDown(int keyCode, KeyEvent event) {
	                if (keyCode == KeyEvent.KEYCODE_BACK) 
	                {
	                        if ((System.currentTimeMillis() - mExitTime) > 2000) 
	                        {
	                                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
	                                mExitTime = System.currentTimeMillis();
	                        }
//	                        相当于点击了“HOME”
	                        else
	                        {
	                        	Intent intent=new Intent(Intent.ACTION_MAIN);
	                        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//android提示 如果是服务里调用，必须用new task标示;
	                        	intent.addCategory(Intent.CATEGORY_HOME);//新建一个主屏幕的Intent就可以;
	                        	startActivity(intent);
	                        }
	                        return true;
	                }
	                return super.onKeyDown(keyCode, event);
	        }
	        
	        
//	       判断此时是否可以连入网络
//	        连入网络，是否代表可以正常进行上网？？连入CMCC返回的是什么？
	        public boolean isNetworkConnected(Context context) {  
	            if (context != null) {  
	                ConnectivityManager mConnectivityManager = (ConnectivityManager) context  
	                        .getSystemService(Context.CONNECTIVITY_SERVICE);  
	                NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();  
	                if (mNetworkInfo != null) {  
	                    return mNetworkInfo.isAvailable();  
	                }  
	            }  
	            return false;  
	        }
	        
	      
	
	        
	        
//	        左软键的目录弹出
	    	public boolean onCreateOptionsMenu(Menu menu) {
	    		menu.add(Menu.NONE, MENU_UPDATE_DB, Menu.NONE, "检查更新");
	    		return super.onCreateOptionsMenu(menu);
	    	}
	    	
	        public boolean onOptionsItemSelected(MenuItem item) {
	    		switch (item.getItemId()) {
	        	case MENU_UPDATE_DB:
	        		downloader.checkDBVersion(preferences.getInt("db_version", 1));
	        		break;
	        	}
	        	return false;
	        }


}
