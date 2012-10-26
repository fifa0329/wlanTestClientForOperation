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
			// 如果手机插入了SD卡，而且应用程序具有访问SD的权限
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				// 获取SD卡对应的存储目录
				File sdCardDir = Environment.getExternalStorageDirectory();
				// 获取指定文件对应的输入流
				FileInputStream fis = new FileInputStream(sdCardDir
						.getCanonicalPath()+"/wlantest/"+"/current/"
						+ FILE_NAME);
				// 将指定输入流包装成BufferedReader
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
			// 如果手机插入了SD卡，而且应用程序具有访问SD的权限
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				// 获取SD卡的目录
				File sdCardDir = Environment.getExternalStorageDirectory();
				File targetFile = new File(sdCardDir.getCanonicalPath()+"/wlantest/"+"/current/"
						+ FILE_NAME);
				// 以指定文件创建 RandomAccessFile对象
				RandomAccessFile raf = new RandomAccessFile(targetFile, "rw");
				// 将文件记录指针移动到最后
				raf.seek(targetFile.length());
				// 输出文件内容
				raf.write(content.getBytes());
				raf.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
