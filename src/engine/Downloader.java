package engine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class Downloader {
	public static final String DB_VERSION_URL = "http://218.94.107.4:8000/central/version";
	public static final String DATABASE_URL = "http://218.94.107.4:8000/central/WLAN.zip";
	public static final String DB_DIR = "/wlantest/databases/";
	public static final String ZIP_FILE = "WLANTEST.zip";
	public static final String INDEX_FILE = "index.dat";
	private static final int MESSAGE_FAILED		= 0;
	private static final int MESSAGE_FINISH		= 1;
	private static final int MESSAGE_LENGTH		= 2;
	private static final int MESSAGE_PROGRESS	= 3;
	private static final int MESSAGE_UNZIP		= 4;
	private static final int MESSAGE_DB_UPDATE	= 5;
	private static final int MESSAGE_NO_UPDATE	= 6;
	private Context context;
	private ProgressDialog progress;
	private Handler progressHandler;
	protected int latest_version;
	private SharedPreferences preferences;
	
	public Downloader(Context context) {
		this.context = context;
		this.progressHandler = new ProgressHandler();
		preferences=this.context.getSharedPreferences("version", Context.MODE_PRIVATE);
	}

	public void checkDBVersion(final int current_version) {
		progress = new ProgressDialog(context);
		progress.setTitle("检查数据库更新");
		progress.setMessage("正在检查，请等待...");
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progress.show();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
		        try {
//		        	用来判断当前版本信息的
		    		HttpClient client = new DefaultHttpClient();
		            HttpGet get = new HttpGet(DB_VERSION_URL);
		            HttpResponse response = client.execute(get);
		            int statusCode = response.getStatusLine().getStatusCode();
		            if (statusCode == 200) {
			            HttpEntity entity = response.getEntity();
			            InputStream is = entity.getContent();
			            byte[] buf = new byte[1024];
			            StringBuffer textBuffer = new StringBuffer();
			            while ((is.read(buf)) > 0) {
			            	textBuffer.append(new String(buf));
			            }
			            latest_version = Integer.parseInt(textBuffer.toString().trim());
			            if (latest_version > current_version) {
			            	sendMesssage(MESSAGE_DB_UPDATE, 0);
			            } else {
			            	sendMesssage(MESSAGE_NO_UPDATE, 0);
			            }
		            } else {
		            	sendMesssage(MESSAGE_FAILED, 0);
		            }
		        } catch (Exception e) {
		            e.printStackTrace();
		            sendMesssage(MESSAGE_FAILED, 0);
		        }
			}
		}).start();
	}
	
	
	
	public boolean isDatabaseExisted() {
        File dir = new File(Environment.getExternalStorageDirectory() + DB_DIR);
        if (!dir.exists()) {
        	dir.mkdirs();
        }
//        通过这个index文件来判断是否解压成功，存在数据库
		return new File(Environment.getExternalStorageDirectory() + DB_DIR + INDEX_FILE).exists();
	}
	
	
//	寻找zip内部的name文件，进行指定的解压缩
	private void extractFile(ZipFile zipFile, String name) throws IOException {
		ZipEntry entry = zipFile.getEntry(name);
		if (entry == null) {
			return;
		}
		
        InputStream istream = zipFile.getInputStream(entry);        
        FileOutputStream oStream = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + DB_DIR + name));
        int ch = -1;
        byte[] buf = new byte[1024];
        while ((ch = istream.read(buf)) > 0) {
        	oStream.write(buf, 0, ch);
        }
        oStream.flush();
        oStream.close();
	}
	
//	选择性的解压了所有文件夹中的文件，而非是自动的批量无脑解压，也就是一开始已经知道需要解压的内容
//	？？？我认为这样不怎么好
	private void unzipDatabase() throws IOException {
        ZipFile zipFile = new ZipFile(Environment.getExternalStorageDirectory() + DB_DIR + ZIP_FILE);
        extractFile(zipFile, INDEX_FILE);
        extractFile(zipFile, "0.dat");
        extractFile(zipFile, "1.dat");
        extractFile(zipFile, "2.dat");
        extractFile(zipFile, "1.db");
        extractFile(zipFile, "2.db");
        if (!new File(Environment.getExternalStorageDirectory() + DB_DIR + "USER.db").exists()) {
        	extractFile(zipFile, "USER.db");
        }
        zipFile.close();
        new File(Environment.getExternalStorageDirectory() + DB_DIR + ZIP_FILE).delete();
        Editor edit=preferences.edit();
		edit.putInt("db_version", latest_version);
		edit.commit();
	}
	
//	下载模块，中间可以进行判断是APK，还是DB
//	主要用于动态显示下载进度
	private void startDownloadProcess(String title, final String url, final String fileName, final boolean isApk) {
		progress = new ProgressDialog(context);
		progress.setTitle(title);
		progress.setMessage("正在下载，请等待...");
		progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progress.show();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
		        try {
		    		HttpClient client = new DefaultHttpClient();
		            HttpGet get = new HttpGet(url);
		            HttpResponse response = client.execute(get);
		            int statusCode = response.getStatusLine().getStatusCode();
		            if (statusCode == 200) {
			            HttpEntity entity = response.getEntity();
			            int length = (int)entity.getContentLength();
			            InputStream istream = entity.getContent();
			            FileOutputStream oStream = new FileOutputStream(fileName); 
			            sendMesssage(MESSAGE_LENGTH, length);
			            int ch = -1;
			            int count = 0;
			            byte[] buf = new byte[1024];
			            while ((ch = istream.read(buf)) > 0) {
			            	oStream.write(buf, 0, ch);
			            	count += ch;
			            	sendMesssage(MESSAGE_PROGRESS, count);
			            }
			            oStream.flush();
			            oStream.close();
			            if (isApk) {
//			            	installNewApk();
			            } else {
				            sendMesssage(MESSAGE_UNZIP, 0);
				            unzipDatabase();
			            }
			            sendMesssage(MESSAGE_FINISH, 0);
		            } else {
		            	sendMesssage(MESSAGE_FAILED, 0);
		            }
		        } catch (Exception e) {
		            e.printStackTrace();
		            sendMesssage(MESSAGE_FAILED, 0);
		        }
			}
		}).start();
	}
	
	
	
//	下载数据库的起点，带头下载过程
	public void downloadDatabase() {
		startDownloadProcess("下载数据库", DATABASE_URL, Environment.getExternalStorageDirectory() + DB_DIR + ZIP_FILE, false);
	}
	
	
	
    public void sendMesssage(int what, int value) {
        Message message = new Message();
        message.what = what;
        message.arg1 = value;
        progressHandler.sendMessage(message);
    }
	
	class ProgressHandler extends Handler {
		@Override
        public void handleMessage(Message msg) {
        	switch (msg.what) {
//        	并非是仅仅的网络访问失败，任何失败都是走的这条路
        	case MESSAGE_FAILED:
        		Toast.makeText(context, "网络访问失败", Toast.LENGTH_LONG).show();
        		progress.dismiss();
        		break;
        	case MESSAGE_FINISH:
        		Toast.makeText(context, "已完成", Toast.LENGTH_LONG).show();
        		progress.dismiss();
        		break;
        	case MESSAGE_LENGTH:
        		progress.setMax(msg.arg1);
        		break;
        	case MESSAGE_PROGRESS:
        		progress.setProgress(msg.arg1);
        		break;
        	case MESSAGE_UNZIP:
        		progress.setMessage("正在解压，请等待...");
        		break;
        	case MESSAGE_DB_UPDATE:
        		progress.dismiss();
    			Dialog dialog = new AlertDialog.Builder(context)
					.setTitle("数据库更新")
					.setMessage("发现数据库落后，是否更新？")
					.setPositiveButton("更新", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							downloadDatabase();
						}})
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}})
					.create();
				dialog.show();
        		break;
        	case MESSAGE_NO_UPDATE:
        		Toast.makeText(context, "已经是最新数据库版本，数据库版本号为"+preferences.getInt("db_version", 100), Toast.LENGTH_LONG).show();
        		progress.dismiss();
        		break;
        	}
		}
	}
	

}
