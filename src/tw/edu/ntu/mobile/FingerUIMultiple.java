package tw.edu.ntu.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class FingerUIMultiple extends Activity {
	private Button createButton;
	private Button searchButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.multiple_play);

		initialize();
	}

	private void initialize() {
		createButton = (Button) findViewById(R.id.create_button);
		searchButton = (Button) findViewById(R.id.search_button);

		createButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				WindowManager manager = getWindowManager();
				Display display = manager.getDefaultDisplay();
				int width = display.getWidth();
				int height = display.getHeight();

				LayoutInflater inflater = getLayoutInflater();
				View view = inflater.inflate(R.layout.alert, null);

				Button createRaceButton = (Button) view
						.findViewById(R.id.create_race_button);
				Button cancelRaceButton = (Button) view
						.findViewById(R.id.cancel_race_button);
				final EditText startLocation = (EditText) view.findViewById(R.id.startLocation);
				final EditText endLocation = (EditText) view.findViewById(R.id.endLocation);
				
				final EditText editTextRaceName = (EditText) view.findViewById(R.id.editTextRaceName);

				final AlertDialog alert = new AlertDialog.Builder(
						FingerUIMultiple.this).setView(view).create();
				
				createRaceButton.setOnClickListener(new OnClickListener() {
					
					public void onClick(View v) {
						// TODO Auto-generated method stub
						String raceName = editTextRaceName.getText().toString();
						String startLocationString = startLocation.getText().toString();
						String endLocationString = endLocation.getText().toString();
						if(raceName.equals("") || startLocationString.equals("") || endLocationString.equals("")) {
							return;
						}
						Intent intent = new Intent();
						intent.setClass(FingerUIMultiple.this, FingerUIMultipleRoomServer.class);
						Bundle extras = new Bundle();
						extras.putString("raceName", raceName);
						extras.putString("startLocation", startLocationString);
						extras.putString("endLocation", endLocationString);
						intent.putExtras(extras);
						startActivity(intent);
					}
				});
				
				cancelRaceButton.setOnClickListener(new OnClickListener() {
					
					public void onClick(View v) {
						// TODO Auto-generated method stub
						alert.dismiss();
					}
				});
				
				alert.show();

				alert.getWindow().setLayout(width / 2, height / 2);
				alert.setTitle("建立賽道");
				
			}
		});

		searchButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});

	}
}
