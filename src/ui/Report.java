package ui;

import java.io.File;
import java.io.IOException;

import com.example.testclient.R;

import engine.DirDel;
import engine.MyIO;
import engine.ZipUtility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Report extends Activity {
    Button save;
	
	
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
				ProgressDialog progressdialog = new ProgressDialog(Report.this);  
				progressdialog.setMessage("«Î…‘∫Ú°≠°≠"); 
				progressdialog.show();
				String str=engine.PhoneInfo.getPhoneInfo(getApplicationContext()).toString();
				MyIO myio=new MyIO("/informations.txt");
				myio.write(str);
				StringBuilder builder=new StringBuilder();
				builder.append("name:").append(name.getText()).append("\n");
				builder.append("address:").append(address.getText()).append("\n");
				builder.append("comments:").append(comments.getText()).append("\n");
				myio.write(builder.toString());
				File zipfile=new File("mnt/sdcard/wlantest/report/"+System.currentTimeMillis()+".zip");
				File directory=new File("mnt/sdcard/wlantest/current/");
				try {
					ZipUtility.zipDirectory(directory, zipfile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				DirDel.delAllFile("mnt/sdcard/wlantest/current/");
				Intent intent=new Intent();
				intent.setClass(Report.this, MainActivity.class);
				startActivity(intent);

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
