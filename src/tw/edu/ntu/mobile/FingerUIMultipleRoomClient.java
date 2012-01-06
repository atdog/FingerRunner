package tw.edu.ntu.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class FingerUIMultipleRoomClient extends Activity {
	private Button startButton;
	private TextView raceNameTextView;
	private TextView player1_textview;
	private TextView player2_textview;
	public static LanConnectionServer server;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.multiple_room_client);

		initialize();
		
	}

	private void initialize() {
		String raceName = getIntent().getExtras().getString("raceName");
		String name= getIntent().getExtras().getString("name");
		
		startButton = (Button) findViewById(R.id.start_button);
		raceNameTextView = (TextView) findViewById(R.id.raceNameTextView);
		player1_textview = (TextView) findViewById(R.id.player1_textview);
		player2_textview = (TextView) findViewById(R.id.player2_textview);
		
		SharedPreferences settings = getSharedPreferences("FingerRunner", 0);
		String userName = settings.getString("name", "");
		
		raceNameTextView.append(raceName);
		player2_textview.append(userName);
		player1_textview.append(name);
		
	}
}
