package ui;

import android.app.Application;

public class MyApplication extends Application { 

// �����˳���� ,����һ���˳�used to one-key exit

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