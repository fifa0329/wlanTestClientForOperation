package ui;


import com.example.testclient.R;
import com.nullwire.trace.ExceptionHandler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.Toast;

public class Login extends Activity{
	EditText account;
	EditText password;
	ImageView login;
	TableRow click1;
	TableRow click2;
	TableRow click3;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login1);
		init();
	}
	
	
	
	
	
	 
	public void init(){
		account=(EditText)findViewById(R.id.account);
		password=(EditText)findViewById(R.id.password);
		login=(ImageView)findViewById(R.id.login);
		
		click1=(TableRow) findViewById(R.id.click1);
		click2=(TableRow) findViewById(R.id.click2);
		click3=(TableRow) findViewById(R.id.click3);
		click1.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub

				account.setText("13951832086");
				password.setText("719930");
			}
		});
		
		
		click2.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub

				account.setText("15996428873");
				password.setText("174648");
			}
		});
		
		
		
		click3.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub

				account.setText("15996424686");
				password.setText("164283");
			}
		});
		
		
		
//		具体多账号还可以再使用其他的东西
		
		
		
		/***
		id2.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
		
				int currentapiVersion = android.os.Build.VERSION.SDK_INT;
				 if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB){
				      android.content.ClipboardManager clipboard =  (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
				         ClipData clip = ClipData.newPlainText("label", "2");
				         clipboard.setPrimaryClip(clip); 
				 } else{
				     android.text.ClipboardManager clipboard = (android.text.ClipboardManager)getSystemService(CLIPBOARD_SERVICE); 
				     clipboard.setText("2");
			
				account.setText("13951681329");
				password.setText("900329");
			}
		});
		***/

		
		
		login.setOnClickListener(new OnClickListener() {
			
			private ProgressDialog progressdialog;

			public void onClick(View v) {
				// TODO Auto-generated method stub
				progressdialog = new ProgressDialog(Login.this);  
				progressdialog.setMessage("请稍候……"); 
				progressdialog.show();
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						WifiManager mWifiManager = (WifiManager)Login.this.getSystemService(Context.WIFI_SERVICE);
						WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
					    boolean isConnected=mWifiInfo.getSupplicantState().equals(SupplicantState.COMPLETED);
						if(isConnected)
						{
							MyApplication mApp = (MyApplication)getApplication();
							mApp.setUser(account.getText().toString());
							mApp.setPassword(password.getText().toString());
							Intent intent=new Intent();
							intent.setClass(Login.this, LoginProcess.class);
							startActivity(intent);
						}
						else{
							Login.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									progressdialog.dismiss();
									Toast.makeText(Login.this, "连接失败，请重试", Toast.LENGTH_LONG).show();
								}
							});
						}
						
					};
				}).start();
					

				
			}
		});

		
		
		
		
		
	}
	
	
	

	
	
	



}
