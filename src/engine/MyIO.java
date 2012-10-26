package engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

import android.os.Environment;

public class MyIO {
	String FILE_NAME = "/crazyit.txt";
	
	
	public MyIO(String name) {
		// TODO Auto-generated constructor stub
		this.FILE_NAME=name;
	}
	
	
	
	public String read() {
		try {
			// ����ֻ�������SD��������Ӧ�ó�����з���SD��Ȩ��
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				// ��ȡSD����Ӧ�Ĵ洢Ŀ¼
				File sdCardDir = Environment.getExternalStorageDirectory();
				// ��ȡָ���ļ���Ӧ��������
				FileInputStream fis = new FileInputStream(sdCardDir
						.getCanonicalPath()+"/wlantest/"+"/current/"
						+ FILE_NAME);
				// ��ָ����������װ��BufferedReader
				BufferedReader br = new BufferedReader(new InputStreamReader(
						fis));
				StringBuilder sb = new StringBuilder("");
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				return sb.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void write(String content) {
		try {
			// ����ֻ�������SD��������Ӧ�ó�����з���SD��Ȩ��
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				// ��ȡSD����Ŀ¼
				File sdCardDir = Environment.getExternalStorageDirectory();
				File targetFile = new File(sdCardDir.getCanonicalPath()+"/wlantest/"+"/current/"
						+ FILE_NAME);
				// ��ָ���ļ����� RandomAccessFile����
				RandomAccessFile raf = new RandomAccessFile(targetFile, "rw");
				// ���ļ���¼ָ���ƶ������
				raf.seek(targetFile.length());
				// ����ļ�����
				raf.write(content.getBytes());
				raf.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
