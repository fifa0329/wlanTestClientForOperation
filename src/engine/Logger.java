package engine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

public class Logger {
	private static Logger instance = null;
	private String outputFileName = Environment.getExternalStorageDirectory() + "/wlantest/current/log.txt";
	private FileOutputStream oStream  = null;
	
	public static Logger getInstance() {
		if (instance == null) {
			instance = new Logger();
		}
		return instance;
	}
	
	private Logger() {
	}
	
	public void startLogger() {
		File outputFile = new File(outputFileName);
    	try {
    		outputFile.delete();
			outputFile.createNewFile();
			oStream = new FileOutputStream(outputFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void writeLog(String log) {
		try {
			Log.i("WLANEngine", log);
	        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS\n");
	        Date curDate = new Date(System.currentTimeMillis());
	        String dateStr = formatter.format(curDate);
	        oStream.write(dateStr.getBytes());
			oStream.write(log.getBytes());
			oStream.write("\n".getBytes());
			oStream.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void stopLogger() {
    	try {
			oStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
