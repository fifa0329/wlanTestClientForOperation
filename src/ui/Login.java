package ui;


import com.example.testclient.R;
import com.nullwire.trace.ExceptionHandler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Login extends Activity{
	Button id1;
	Button id2;
	Button id3;
	EditText account;
	EditText password;
	Button login;
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		init();
	}
	
	
	
	
	
	 
	public void init(){
		id1=(Button)findViewById(R.id.id1);
		id2=(Button)findViewById(R.id.id2);
		id3=(Button)findViewById(R.id.id3);
		account=(EditText)findViewById(R.id.account);
		password=(EditText)findViewById(R.id.password);
		login=(Button)findViewById(R.id.login);
		
		
		id1.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub

				account.setText("13951832086");
				password.setText("719930");
			}
		});
//		具体多账号还可以再使用其他的东西
		
		
		id2.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				/** 
				int currentapiVersion = android.os.Build.VERSION.SDK_INT;
				 if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB){
				      android.content.ClipboardManager clipboard =  (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
				         ClipData clip = ClipData.newPlainText("label", "2");
				         clipboard.setPrimaryClip(clip); 
				 } else{
				     android.text.ClipboardManager clipboard = (android.text.ClipboardManager)getSystemService(CLIPBOARD_SERVICE); 
				     clipboard.setText("2");
				 }
				 **/
				account.setText("13951681329");
				password.setText("900329");
			}
		});
		
		
		login.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MyApplication mApp = (MyApplication)getApplication();
				mApp.setUser(account.getText().toString());
				mApp.setPassword(password.getText().toString());
				Intent intent=new Intent();
				intent.setClass(Login.this, LoginProcess.class);
				startActivity(intent);
				
			}
		});

		
		
		
		
		
	}
	
	
	
	protected void onStart() { 
		 
		super.onStart(); 
		 
		MyApplication mApp = (MyApplication)getApplication(); 
		 
		if (mApp.isExit()) { 
		 
		finish(); 
		 
		} 
		 
		}
	


}
