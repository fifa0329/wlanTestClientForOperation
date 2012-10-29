package ui;

import java.io.File;
import java.lang.reflect.Method;

import com.example.testclient.R;
import android.R.string;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	Button to_cmcc;
	Button to_chinanet;
	Button wlansetting;
	TextView ssid;
	ConnectivityManager mConnectivityManager;
	TextView report_total;
	MyApplication mApp;

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
    	super.onResume();
        WifiManager mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE); 
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
        ssid=(TextView)findViewById(R.id.ssid);
    	ssid.setText(mWifiInfo.getSSID());
    	//setMobileNetEnable();
    	new File(Environment.getExternalStorageDirectory() + "/wlantest/"+"/report/").mkdirs();
    	new File(Environment.getExternalStorageDirectory() + "/wlantest/"+"/current/").mkdirs();
        
    	
//    	下述代码用于根据条件判断按钮的可选择性
    	if (mWifiInfo.getSSID().equals((String)"ENICE-1B3F-AP1"))
        {
            to_cmcc.setVisibility(View.VISIBLE); 
        }//SHOW the button
    	
    	
//    	最初的想法，用于更改报告个数
    	MyApplication mApp = (MyApplication)getApplication(); 
    	if(mApp.getTotal()!=0)
    	{
    		report_total.setText("当前你已经保存"+mApp.getTotal()+"条报告"+"\n"+"上传请点击");
    	}
    }
    
    

    
    
}
