package ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.example.testclient.R;

public class LoginProcess extends Activity{
	TextView show;
	Button report;
	Button logout;
	Button browser;
	StringBuilder builder=new StringBuilder();
	String FLAG;
	
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_process);
		init();
	}

	
	
	
	
	
	public void init(){
	show=(TextView) findViewById(R.id.show);
	report=(Button) findViewById(R.id.report);
	browser=(Button) findViewById(R.id.browser);
	logout=(Button) findViewById(R.id.logout);
	FLAG = getIntent().getStringExtra("FLAG");
	int a=Integer.parseInt(FLAG);
	switch(a){
	case 1:
		builder.append("���ڵ�¼�С�����"+"\n");
		builder.append("��¼�ɹ���"+"\n");
		logout.setVisibility(View.VISIBLE);
		show.setText(builder);
		break;
	case 2:
		builder.append("���ڵ�¼�С�����"+"\n");
		builder.append("��¼ʧ�ܣ�"+"\n");
		builder.append("�������������ԣ�"+"\n");
		browser.setVisibility(View.VISIBLE);
		show.setText(builder);
		break;
	case 0:
		builder.append("��"+"\n");
		builder.append("���Һ���"+"\n");
		browser.setVisibility(View.VISIBLE);
		logout.setVisibility(View.VISIBLE);
		report.setVisibility(View.VISIBLE);
		show.setText(builder);
		break;
	}

	
	
	report.setOnClickListener(new OnClickListener() {
		
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent=new Intent();
			intent.setClass(LoginProcess.this, Report.class);
			startActivity(intent);
		}
	});
	
	
	
	browser.setOnClickListener(new OnClickListener() {
		
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent=new Intent();
			intent.setClass(LoginProcess.this, Browser.class);
			startActivity(intent);
		}
	});
	
	
	
	
	logout.setOnClickListener(new OnClickListener() {
		
		public void onClick(View v) {
			// TODO Auto-generated method stub
			builder.append("�������ߡ�����"+"\n");
			builder.append("���߳ɹ�");
			show.setText(builder);
			report.setVisibility(View.VISIBLE);
			logout.setVisibility(View.INVISIBLE);
			
		}
	});
	
	
	
	
	
	
	
	
	}
}
