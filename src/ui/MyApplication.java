package ui;

import android.app.Application;

public class MyApplication extends Application { 

// 程序退出标记 ,用于一键退出used to one-key exit

private static int reportTotal = 0; 

public void addTotal() { 

	reportTotal = reportTotal+1; 

} 

public void clearTotal() { 

	reportTotal = 0; 

}


public int getTotal() { 

	return reportTotal; 

}
}