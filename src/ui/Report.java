package ui;

import com.example.testclient.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

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
    	
    	save=(Button)findViewById(R.id.save);
    	save.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent=new Intent();
				intent.setClass(Report.this, MainActivity.class);
				startActivity(intent);
				MyApplication mApp = (MyApplication)getApplication(); 
				mApp.addTotal();
			}
		});
    	
    	
    	
    	
    }
   
    
    
}
