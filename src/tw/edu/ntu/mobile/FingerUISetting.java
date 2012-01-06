package tw.edu.ntu.mobile;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class FingerUISetting extends Activity {
	private EditText editTextName;
	private ImageButton saveButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//消除標題列
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//消除狀態列
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.setting);

		initialize();
	}

	private void initialize() {
		editTextName = (EditText) findViewById(R.id.editTextName);
		saveButton = (ImageButton) findViewById(R.id.save_button);

		SharedPreferences settings = getSharedPreferences("FingerRunner", 0);
		String userName = settings.getString("name", "");
		editTextName.setText(userName);

		saveButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				SharedPreferences settings = getSharedPreferences(
						"FingerRunner", 0);
				SharedPreferences.Editor PE = settings.edit();
				PE.putString("name", editTextName.getText().toString());
				PE.commit();
				finish();
			}
		});
	}
}
