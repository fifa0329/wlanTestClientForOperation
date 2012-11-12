package ui;

import android.app.Application;

public class MyApplication extends Application {

	public static int CMCC = 0;
	public static int CHINANET = 1;

	

	private int carrier;
	private String user;
	private String password;

	
	
	
	
	
	
	
	public int getCarrier() {
		return carrier;
	}

	public void setCarrier(int carrier) {
		this.carrier = carrier;
	}
	

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}



	
	
	

}