package tw.edu.ntu.mobile;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FingerUIFlowActivity extends Activity {
	/** Called when the activity is first created. */
	private Button singleButton;
	private Button multipleButton;
	private Button settingButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.uimain);

		initialize();

		checkAndSetting();
	}

	private void initialize() {
		/**
		 * initialize UI component
		 */
		singleButton = (Button) findViewById(R.id.single_button);
		multipleButton = (Button) findViewById(R.id.multiple_button);
		settingButton = (Button) findViewById(R.id.setting_button);
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