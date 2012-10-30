package ui;

import java.io.File;
import java.io.IOException;

import com.example.testclient.R;

import engine.DirDel;
import engine.Logger;
import engine.MyIO;
import engine.ZipUtility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Report extends Activity {
    Button save;
    ProgressDialog progressdialog;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report);
    	init();
        
        

    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    
    public void init(){
    	
    	
    	
    	final EditText name=(EditText) findViewById(R.id.name);
    	final EditText address=(EditText) findViewById(R.id.address);
    	final EditText comments=(EditText) findViewById(R.id.comments);

    	save=(Button)findViewById(R.id.save);
    	save.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				progressdialog = new ProgressDialog(Report.this);  
				progressdialog.setMessage("«Î…‘∫Ú°≠°≠"); 
				progressdialog.show();
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Logger.getInstance().stopLogger();
						String str=engine.PhoneInfo.getPhoneInfo(getApplicationContext()).toString();
						MyIO myio=new MyIO("/informations.txt");
						myio.write(str);
						StringBuilder builder=new StringBuilder();
						builder.append("name:").append(name.getText()).append("\n");
						builder.append("address:").append(address.getText()).append("\n");
						builder.append("comments:").append(comments.getText()).append("\n");
						myio.write(builder.toString());
						try{
						if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
						{
						File zipfile=new File(Environment.getExternalStorageDirectory().getCanonicalPath()+"/wlantest/report/"+System.currentTimeMillis()+".zip");
						File directory=new File(Environment.getExternalStorageDirectory().getCanonicalPath()+"/wlantest/current/");
						Log.v("CanonicalPath", Environment.getExternalStorageDirectory().getCanonicalPath());
						Log.v("getAbsolutePath", Environment.getExternalStorageDirectory().getAbsolutePath());
						Log.v("zipfile.getAbsolutePath", zipfile.getAbsolutePath());
						Log.v("zipfile.getCanonicalPath()", zipfile.getCanonicalPath());
						Log.v("zipfile.getCanonicalPath()", Environment.getExternalStorageDirectory()
								+ "/wlantest/" + "/report/");
						ZipUtility.zipDirectory(directory, zipfile);
						DirDel.delAllFile(Environment.getExternalStorageDirectory().getCanonicalPath()+"/wlantest/current/");
						}
						}
						catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
						
						Intent intent=new Intent();
						intent.setClass(Report.this, MainActivity.class);
						startActivity(intent);
					}
				}).start();


			}
		});
    	
    	
    	
    	
    }
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			
		}
		return true;
		
	}
    
	
	
	protected void onStart() { 
		 
		super.onStart(); 
		 
		MyApplication mApp = (MyApplication)getApplication(); 
		 
		if (mApp.isExit()) { 
		 
		finish(); 
		 
		} 
		 
		}
   
    
    
}
