package tw.edu.ntu.mobile;

import android.R.string;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class FingerUIMultipleRoomServer extends Activity {
	private ImageButton startButton;
	private TextView raceNameTextView;
	private TextView player1_textview;
	private TextView player2_textview;
	public static LanConnectionServer server = null;
	private String raceName;
	private String userName;
	private Handler mainHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			player2_textview.append(msg.getData().getString("name"));
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.multiple_room_server);

		initialize();

		/**
		 * start broadcast
		 */
		server = new LanConnectionServer(mainHandler, userName);
		server.startHandleBroadcastPacket(raceName);

	}

	private void initialize() {
		raceName = getIntent().getExtras().getString("raceName");

		startButton = (ImageButton) findViewById(R.id.start_button);
		raceNameTextView = (TextView) findViewById(R.id.raceNameTextView);
		player1_textview = (TextView) findViewById(R.id.player1_textview);
		player2_textview = (TextView) findViewById(R.id.player2_textview);

		SharedPreferences settings = getSharedPreferences("FingerRunner", 0);
		userName = settings.getString("name", "");

		raceNameTextView.append(raceName);
		player1_textview.append(userName);

		startButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (server.sizeOfClient() == 0) {
					return;
				}
				Bundle bundle = getIntent().getExtras();
				bundle.putBoolean("hasOther", true);
				server.sendData("start:" + bundle.getString("start") + ",end:"
						+ bundle.getString("end"));
				Intent intent = new Intent();
				intent.setClass(FingerUIMultipleRoomServer.this,
						FingerRunnerMapActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}
}
