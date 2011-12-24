package tw.edu.ntu.mobile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class Timer extends Thread {
	private Handler mainHandler;
	private float subLength;
	
	Timer(Handler mHandler, float subLength) {
		mainHandler = mHandler;
		this.subLength = subLength;
	}
	
	public void run() {
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Message m = new Message();
		Bundle data = new Bundle();
		data.putFloat("subLength",  subLength);
		m.setData(data);
        mainHandler.sendMessage(m);  
	}
}
