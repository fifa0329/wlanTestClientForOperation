package ui;




import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import android.net.NetworkInfo;

import com.example.testclient.R;


import engine.AuthPortalStar;

import engine.Logger;
import engine.WifiAdmin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class Login extends Activity{
	EditText account;
	EditText password;
	ImageView login;
	ImageView refresh;
	TableRow click1;
	TableRow click2;
	TableRow click3;
	TextView id1;
	TextView id2;
	TextView id3;
	TextView password1;
	TextView password2;
	TextView password3;
	Button get_password;
	private ProgressDialog progressdialog;
	private byte[] body;
	private MyApplication mApp;
	SharedPreferences preferences;

	protected static final String GET_ID_URL="http://wuxiantao.sinaapp.com/devapi/getaccount.php";
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login1);
		init();
	}
	
	
	
	
	
	 
	public void init(){
		Logger.getInstance().startLogger();

		get_password=(Button) findViewById(R.id.get_password);
		get_password.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				AuthPortalStar.getInstance().getDynamicPassword(account.getText().toString());
			}
		});

		id1=(TextView) findViewById(R.id.id1);
		id2=(TextView) findViewById(R.id.id2);
		id3=(TextView) findViewById(R.id.id3);
		password1=(TextView) findViewById(R.id.password1);
		password2=(TextView) findViewById(R.id.password2);
		password3=(TextView) findViewById(R.id.password3);
		account=(EditText)findViewById(R.id.account);
		password=(EditText)findViewById(R.id.password);
		login=(ImageView)findViewById(R.id.login);
		refresh=(ImageView)findViewById(R.id.refresh);
		mApp=(MyApplication) getApplication();
		preferences =getPreferences(MODE_PRIVATE);
		
		
		
		
		if(mApp.getCarrier()==MyApplication.CMCC)
		{
			id1.setText(preferences.getString("cmcc_account0", ""));
			id2.setText(preferences.getString("cmcc_account1", ""));
			id3.setText(preferences.getString("cmcc_account2", ""));
			password1.setText(preferences.getString("cmcc_passwd0", ""));
			password2.setText(preferences.getString("cmcc_passwd1", ""));
			password3.setText(preferences.getString("cmcc_passwd2", ""));
			account.setText(preferences.getString("cmcc_account", ""));
			password.setText(preferences.getString("cmcc_passwd", ""));
		}
		if(mApp.getCarrier()==MyApplication.CHINANET)
		{
			id1.setText(preferences.getString("chinanet_account0", ""));
			id2.setText(preferences.getString("chinanet_account1", ""));
			id3.setText(preferences.getString("chinanet_account2", ""));
			password1.setText(preferences.getString("chinanet_passwd0", ""));
			password2.setText(preferences.getString("chinanet_passwd1", ""));
			password3.setText(preferences.getString("chinanet_passwd2", ""));
			account.setText(preferences.getString("chinanet_account", ""));
			password.setText(preferences.getString("chinanet_passwd", ""));
		}
		
		
		
		
		
		refresh.setOnClickListener(new OnClickListener() 
		{
			
		

			@Override
			public void onClick(View v) 
			{
				
				progressdialog = new ProgressDialog(Login.this);  
				progressdialog.setMessage("正在获取账号密码，请稍候……"); 
				progressdialog.show();
				// TODO Auto-generated method stub
				
				new Thread(new Runnable() 
				{


					

					public void run() 
					{
							try {
								WifiAdmin wifiadmin=new WifiAdmin(Login.this);
								wifiadmin.closeNetCard();
								if(isNetworkConnected(Login.this)==false)
								{
									Login.this.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											Toast.makeText(Login.this, "当前网络不可用，请检查GPRS开启与否", Toast.LENGTH_SHORT).show();
											progressdialog.dismiss();
										}
									});
								}
								else
								{
									HttpClient client = new DefaultHttpClient();
									HttpGet get=new HttpGet(GET_ID_URL);
									HttpResponse response =client.execute(get);
									if (response.getStatusLine().getStatusCode() == 200) 
									{

										HttpEntity entity=response.getEntity();
										if(entity!=null)
										{
											InputStream istream = response.getEntity().getContent();
											body = toByteArray(istream);
											parseID(body);
											
										}
									}
									wifiadmin.openNetCard();
									
									mApp=(MyApplication) getApplication();
									
									if(mApp.getCarrier()==MyApplication.CHINANET)
									{
										wifiadmin.addApProfile("\"ChinaNet\"");
									}
									if(mApp.getCarrier()==MyApplication.CMCC)
									{
										wifiadmin.addApProfile("\"CMCC\"");
									}
								}

								

									
								
							} catch (MalformedURLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}catch (NoHttpResponseException e) {
								e.printStackTrace();

							} catch (ClientProtocolException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						
					    		
					}
					
				}).start();
			}
		});
		
		
		
		

		
		
		
		
		
		
		
		click1=(TableRow) findViewById(R.id.click1);
		click2=(TableRow) findViewById(R.id.click2);
		click3=(TableRow) findViewById(R.id.click3);
		click1.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub

				account.setText(id1.getText());
				password.setText(password1.getText());
			}
		});
		
		
		click2.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub

				account.setText(id2.getText());
				password.setText(password2.getText());
			}
		});
		
		
		
		click3.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub

				account.setText(id3.getText());
				password.setText(password3.getText());
			}
		});
		
		
		
//		具体多账号还可以再使用其他的东西
		
		
				
		login.setOnClickListener(new OnClickListener() {
			

			private ProgressDialog progressdialog;
			private String flag;


			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(account.getText().toString().equals((String)""))
				{
					Toast toast=Toast.makeText(Login.this, "账号不能为空", Toast.LENGTH_SHORT);
					toast.show();
				}
				else if(password.getText().toString().equals((String)"") )
				{
					Toast toast=Toast.makeText(Login.this, "密码不能为空", Toast.LENGTH_SHORT);
					toast.show();
				}
				else
				{
					if(mApp.getCarrier()==MyApplication.CMCC)
					{
						Editor editor =preferences.edit();
						editor.putString("cmcc_account",account.getText().toString());
						editor.putString("cmcc_passwd",password.getText().toString());
						editor.commit();
						flag="CMCC";
					}
					if(mApp.getCarrier()==MyApplication.CHINANET)
					{
						Editor editor =preferences.edit();
						editor.putString("chinanet_account",account.getText().toString());
						editor.putString("chinanet_passwd",password.getText().toString());
						editor.commit();
						flag="ChinaNet";
					}

					progressdialog = new ProgressDialog(Login.this);  
					progressdialog.setMessage("正在登入，请稍候……"); 
					progressdialog.show();
					new Thread(new Runnable() {
						public void run() {
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							WifiManager mWifiManager = (WifiManager)Login.this.getSystemService(Context.WIFI_SERVICE);
							WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
							
							if(!mWifiInfo.getSupplicantState().equals(SupplicantState.COMPLETED) || mWifiInfo.getSSID()==null)
							{
								Login.this.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										progressdialog.dismiss();
										Toast.makeText(Login.this, "还未彻底连接好，请再试", Toast.LENGTH_SHORT).show();
									}
								});
							}
							else if(mWifiInfo.getSupplicantState().equals(SupplicantState.COMPLETED))
							{
								try{
									if(mWifiInfo.getSSID().equals((String)flag))  
									{
										progressdialog.dismiss();
										MyApplication mApp = (MyApplication)getApplication();
										mApp.setUser(account.getText().toString());
										mApp.setPassword(password.getText().toString());
										Intent intent=new Intent();
										intent.putExtra("step", "2");
										intent.setClass(Login.this, LoginProcess.class);
										startActivity(intent);
									}
									else if(!mWifiInfo.getSSID().equals((String)flag))
									{
										Login.this.runOnUiThread(new Runnable() {
											@Override
											public void run() {
												progressdialog.dismiss();
												Toast.makeText(Login.this, "连接失败,您当前连接的网络与要求测试的不一致", Toast.LENGTH_LONG).show();
											}
										});
									}
								}catch (NullPointerException e) {
									// TODO: handle exception
								}
								

							}
							
							
						};
					}).start();
				}

					

				
			}
		});

		
		

	}
	
	
	
	public static byte[] toByteArray(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int read = 0;
		byte[] buffer = new byte[1024];
		while (read != -1) {
			read = in.read(buffer);
			if (read != -1)
				out.write(buffer, 0, read);
		}
		out.close();
		return out.toByteArray();
	}
	
	
	public void parseID(byte[] body) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document dom = builder.parse(new ByteArrayInputStream(body));
			Element root = dom.getDocumentElement();

			if (root.getTagName().equals("accounts")) {
				NodeList accountList = root.getElementsByTagName("account");
				for (int i = 0, cmcc=0, chinanet=0; i < accountList.getLength(); i++) {

					Element e = (Element) accountList.item(i);
					
					
					
					if(e.getAttribute("type").equals((String)"CMCC") && cmcc<3)
					{
						Log.v("account", e.getAttribute("account"));
						Editor editor =preferences.edit();
						editor.putString("cmcc_account"+cmcc, e.getAttribute("account"));
						editor.putString("cmcc_passwd"+cmcc, e.getAttribute("passwd"));
						editor.commit();


						cmcc++;
					}
					
					
					
					
					
					if(e.getAttribute("type").equals((String)"ChinaNet") && chinanet<3)
					{
						Log.v("account", e.getAttribute("account"));
						Editor editor =preferences.edit();
						editor.putString("chinanet_account"+chinanet, e.getAttribute("account"));
						editor.putString("chinanet_passwd"+chinanet, e.getAttribute("passwd"));
						editor.commit();

						chinanet++;
					}
					
					
					
					
					Login.this.runOnUiThread(new Runnable() {
						

						@Override
						public void run() {
							
							
							
							mApp=(MyApplication) getApplication();
							if(mApp.getCarrier()==MyApplication.CMCC)
							{
								id1.setText(preferences.getString("cmcc_account0", ""));
								id2.setText(preferences.getString("cmcc_account1", ""));
								id3.setText(preferences.getString("cmcc_account2", ""));
								password1.setText(preferences.getString("cmcc_passwd0", ""));
								password2.setText(preferences.getString("cmcc_passwd1", ""));
								password3.setText(preferences.getString("cmcc_passwd2", ""));
							}
							if(mApp.getCarrier()==MyApplication.CHINANET)
							{
								id1.setText(preferences.getString("chinanet_account0", ""));
								id2.setText(preferences.getString("chinanet_account1", ""));
								id3.setText(preferences.getString("chinanet_account2", ""));
								password1.setText(preferences.getString("chinanet_passwd0", ""));
								password2.setText(preferences.getString("chinanet_passwd1", ""));
								password3.setText(preferences.getString("chinanet_passwd2", ""));
							}
							

						}
					});
					progressdialog.dismiss();

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
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



}
