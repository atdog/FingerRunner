package tw.edu.ntu.mobile;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class FingerUISetting extends Activity {
	private EditText editTextName;
	private Button saveButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);

		initialize();
	}

	private void initialize() {
		editTextName = (EditText) findViewById(R.id.editTextName);
		saveButton = (Button) findViewById(R.id.save_button);

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
