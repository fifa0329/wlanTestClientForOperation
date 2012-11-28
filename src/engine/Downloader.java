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
		progress.setTitle("������ݿ����");
		progress.setMessage("���ڼ�飬��ȴ�...");
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progress.show();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
		        try {
//		        	�����жϵ�ǰ�汾��Ϣ��
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
//        ͨ�����index�ļ����ж��Ƿ��ѹ�ɹ����������ݿ�
		return new File(Environment.getExternalStorageDirectory() + DB_DIR + INDEX_FILE).exists();
	}
	
	
//	Ѱ��zip�ڲ���name�ļ�������ָ���Ľ�ѹ��
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
	
//	ѡ���ԵĽ�ѹ�������ļ����е��ļ����������Զ����������Խ�ѹ��Ҳ����һ��ʼ�Ѿ�֪����Ҫ��ѹ������
//	����������Ϊ��������ô��
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
	
//	����ģ�飬�м���Խ����ж���APK������DB
//	��Ҫ���ڶ�̬��ʾ���ؽ���
	private void startDownloadProcess(String title, final String url, final String fileName, final boolean isApk) {
		progress = new ProgressDialog(context);
		progress.setTitle(title);
		progress.setMessage("�������أ���ȴ�...");
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
	
	
	
//	�������ݿ����㣬��ͷ���ع���
	public void downloadDatabase() {
		startDownloadProcess("�������ݿ�", DATABASE_URL, Environment.getExternalStorageDirectory() + DB_DIR + ZIP_FILE, false);
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
//        	�����ǽ������������ʧ�ܣ��κ�ʧ�ܶ����ߵ�����·
        	case MESSAGE_FAILED:
        		Toast.makeText(context, "�������ʧ��", Toast.LENGTH_LONG).show();
        		progress.dismiss();
        		break;
        	case MESSAGE_FINISH:
        		Toast.makeText(context, "�����", Toast.LENGTH_LONG).show();
        		progress.dismiss();
        		break;
        	case MESSAGE_LENGTH:
        		progress.setMax(msg.arg1);
        		break;
        	case MESSAGE_PROGRESS:
        		progress.setProgress(msg.arg1);
        		break;
        	case MESSAGE_UNZIP:
        		progress.setMessage("���ڽ�ѹ����ȴ�...");
        		break;
        	case MESSAGE_DB_UPDATE:
        		progress.dismiss();
    			Dialog dialog = new AlertDialog.Builder(context)
					.setTitle("���ݿ����")
					.setMessage("�������ݿ�����Ƿ���£�")
					.setPositiveButton("����", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							downloadDatabase();
						}})
					.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}})
					.create();
				dialog.show();
        		break;
        	case MESSAGE_NO_UPDATE:
        		Toast.makeText(context, "�Ѿ����������ݿ�汾�����ݿ�汾��Ϊ"+preferences.getInt("db_version", 100), Toast.LENGTH_LONG).show();
        		progress.dismiss();
        		break;
        	}
		}
	}
	

}
