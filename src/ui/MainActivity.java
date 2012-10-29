package ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.example.testclient.R;

import android.R.string;
import android.net.ConnectivityManager;
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
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	protected static final String UPLOAD_LOG_URL = "http://218.94.107.4:7000/WLANServer/LogCollector";
	Button to_cmcc;
	Button to_chinanet;
	Button wlansetting;
	TextView ssid;
	ConnectivityManager mConnectivityManager;
	TextView report_total;
	MyApplication mApp;
	private static final int MESSAGE_FAILED		= 0;
	private static final int MESSAGE_FINISH		= 1;
	private static final int MESSAGE_LENGTH		= 2;
	private static final int MESSAGE_PROGRESS	= 3;
	private static final int MESSAGE_UNZIP		= 4;
	private static final int MESSAGE_UPDATE		= 5;
	private static final int MESSAGE_NO_UPDATE	= 6;
	private Handler progressHandler;
	private ProgressDialog progress;
	private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    	init();
        

    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    
    public void init(){
    	mApp = (MyApplication)getApplication();
    	to_cmcc=(Button)findViewById(R.id.to_cmcc);
        to_cmcc.setOnClickListener(new OnClickListener() {
		
		public void onClick(View arg0) {
			mApp.setCarrier(MyApplication.CMCC);
			Intent intent=new Intent();
			intent.setClass(MainActivity.this, Login.class);
			startActivity(intent);
			
		}
	});
    to_chinanet=(Button)findViewById(R.id.to_chinanet);
    to_chinanet.setOnClickListener(new OnClickListener() {
		
		public void onClick(View v) {
			// TODO Auto-generated method stub
			mApp.setCarrier(MyApplication.CHINANET);
			Intent intent=new Intent();
			intent.setClass(MainActivity.this, Login.class);
			startActivity(intent);
			
		}
	});
    wlansetting=(Button)findViewById(R.id.wlan_setting);
    wlansetting.setOnClickListener(new OnClickListener() {
		
		public void onClick(View v) {
			// TODO Auto-generated method stub
//    		mWifiAdmin.connect();
//    		startActivityForResult(new Intent(
//    				android.provider.Settings.ACTION_WIFI_SETTINGS), 0);
    		startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
//			gprsEnable(false);
		}
	});
    ssid=(TextView)findViewById(R.id.ssid);
    report_total=(TextView)findViewById(R.id.report_total);
    
    
    
    
	File filetotal=new File("mnt/sdcard/wlantest/report/");
	
	if(filetotal.listFiles().length!=0)
	{
		report_total.setText("当前你已经保存"+filetotal.listFiles().length+"条报告"+"\n"+"上传请点我");
		report_total.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				final Builder builder=new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("上传报告");
				builder.setMessage("是否进行上传？");
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which)
					{
						//进度对话框的秒显用法
						uploadLog();
					}
	
				});
				builder.setNegativeButton("取消",new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which)
					{
						return;
					}
				});
				builder.create().show();;  //创建对话框
				}
			});
		}
    

    
    

    }

    
    
    
    

    
    
    
    
    
    
    
    
//    以下模块用于管理gprs的自动开启
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
	
	
//	GPRS自动开启结束
	
	
	
//    用来监听wifi的变化，改变标题栏的报告数量	
    public void onResume()
    {
    	Log.v("test", "这里有resume");
    	super.onResume();
        WifiManager mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE); 
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
        ssid=(TextView)findViewById(R.id.ssid);
    	ssid.setText(mWifiInfo.getSSID());
    	//setMobileNetEnable();
    	new File(Environment.getExternalStorageDirectory() + "/wlantest/"+"/report/").mkdirs();
    	new File(Environment.getExternalStorageDirectory() + "/wlantest/"+"/current/").mkdirs();
        
    	
//    	下述代码用于根据条件判断按钮的可选择性
//    	这里要注意，有个BUG，没有开启WIFI的话，是会出错的,因此增加了try，catch版块
    	try{
    	if (mWifiInfo.getSSID().equals((String)"ENICE-1B3F-AP1"))
        {
            to_cmcc.setVisibility(View.VISIBLE); 
        }
    	}
    	catch (Exception e) {
			// TODO: handle exception
		}//SHOW the button
    	
    	File filetotal=new File("mnt/sdcard/wlantest/report/");
    	if(filetotal.listFiles().length!=0)
    	{
    		report_total.setText("当前你已经保存"+filetotal.listFiles().length+"条报告"+"\n"+"上传请点我");
    	}
    	

    	}
    
    
    
    
    
	public void uploadLog() {
	/*
		progress = new ProgressDialog(context);
		progress.setTitle("上传日志");
		progress.setMessage("正在上传，请等待...");
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progress.show();
		*/
		new Thread(new Runnable() {
			@Override
			public void run() {
				
					HttpClient client = new DefaultHttpClient();
					HttpPost post = new HttpPost(UPLOAD_LOG_URL);
					InputStreamEntity reqEntity;
					try {
						reqEntity = new InputStreamEntity(
								new FileInputStream(Environment.getExternalStorageDirectory()
										+ "/wlantest/" +"/report/"+ "1351490719879" + ".zip"), -1);
						reqEntity.setContentType("application/x-zip-compressed");
						reqEntity.setChunked(true);
						post.setEntity(reqEntity);
						//post.setHeader("Content-Length", ""+new File(Environment.getExternalStorageDirectory()
						//		+ "/wlantest/" +"/report/"+ "1351490719879" + ".zip").length());
						HttpResponse response = client.execute(post);
						Log.v("test", ""+response.getStatusLine().toString());
						Log.v("test", ""+response.getStatusLine().getStatusCode());
						if (response.getStatusLine().getStatusCode() == 200) {
							
						}
						
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClientProtocolException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

						/*
						sendMesssage(MESSAGE_FINISH, 0);
					} else {
						sendMesssage(MESSAGE_FAILED, 0);
					}
				} catch (IOException e) {
					e.printStackTrace();
					sendMesssage(MESSAGE_FAILED, 0);
				}
				*/
			
		}
	}).start();
	}
	
	public void sendMesssage(int what, int value) {
        Message message = new Message();
        message.what = what;
        message.arg1 = value;
        progressHandler.sendMessage(message);
    }
}
