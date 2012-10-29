package engine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;

import android.os.Environment;

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
		File outputFile = new File(outputFileName);
    	try {
			outputFile.createNewFile();
			oStream = new FileOutputStream(outputFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void writeLog(String log) {
		try {
			oStream.write(log.getBytes());
			oStream.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
