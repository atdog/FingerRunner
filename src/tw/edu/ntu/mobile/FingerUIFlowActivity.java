package tw.edu.ntu.mobile;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

public class FingerUIFlowActivity extends Activity {
	/** Called when the activity is first created. */
	private ImageButton singleButton;
	private ImageButton multipleButton;
	private ImageButton settingButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//消除標題列
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//消除狀態列
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.uimain);

		initialize();

		checkAndSetting();
	}

	
	
	@Override
	protected void onPause() {
		if(FingerUIMultiple.client != null) {
			FingerUIMultiple.client.stopClient();
		}
		if(FingerUIMultipleRoomServer.server != null) {
			FingerUIMultipleRoomServer.server.stopServer();
		}
		// TODO Auto-generated method stub
		super.onPause();
	}



	private void initialize() {
		/**
		 * initialize UI component
		 */
		singleButton = (ImageButton) findViewById(R.id.single_button);
		multipleButton = (ImageButton) findViewById(R.id.multiple_button);
		settingButton = (ImageButton) findViewById(R.id.setting_button);
		/**
		 * set OnClick event
		 */
		// 單人遊戲
		singleButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(FingerUIFlowActivity.this, ChooseVenueActivity.class);
				Bundle extras = new Bundle();
				extras.putBoolean("hasOther", false);
				intent.putExtras(extras);
				startActivity(intent);
			}
		});
		// 多人遊戲
		multipleButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(FingerUIFlowActivity.this, FingerUIMultiple.class);
				startActivity(intent);
			}
		});
		// 設置選項
		settingButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(FingerUIFlowActivity.this, FingerUISetting.class);
				startActivity(intent);
			}
		});
	}
	
	private void checkAndSetting() {
		SharedPreferences settings = getSharedPreferences("FingerRunner", 0);
		String userName = settings.getString("name", "");
		if(userName.equals("")) {
			Intent intent = new Intent();
			intent.setClass(FingerUIFlowActivity.this, FingerUISetting.class);
			startActivity(intent);
		}
	}
	
	
}