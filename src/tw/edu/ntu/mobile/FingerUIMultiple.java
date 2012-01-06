package tw.edu.ntu.mobile;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
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

public class FingerUIMultiple extends Activity {
	private ImageButton createButton;
	public static LanConnectionClient client = null;
	private ImageButton searchButton;
	private String raceName;
	private Handler mainHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			
			Intent intent = new Intent();
			intent.setClass(FingerUIMultiple.this, FingerUIMultipleRoomClient.class);
			Bundle bundle = new Bundle();
			bundle.putString("raceName", raceName);
			bundle.putString("name",msg.getData().getString("name"));
			Log.d("test", raceName);
			intent.putExtras(bundle);
			startActivity(intent);
		}
		
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.multiple_play);

		initialize();
	}

	private void initialize() {
		createButton = (ImageButton) findViewById(R.id.create_button);
		searchButton = (ImageButton) findViewById(R.id.search_button);

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
				final EditText startLocation = (EditText) view
						.findViewById(R.id.startLocation);
				final EditText endLocation = (EditText) view
						.findViewById(R.id.endLocation);

				final EditText editTextRaceName = (EditText) view
						.findViewById(R.id.editTextRaceName);

				final AlertDialog alert = new AlertDialog.Builder(
						FingerUIMultiple.this).setView(view).create();

				createRaceButton.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						// TODO Auto-generated method stub
						String raceName = editTextRaceName.getText().toString();
						String startLocationString = startLocation.getText()
								.toString();
						String endLocationString = endLocation.getText()
								.toString();
						if (raceName.equals("")
								|| startLocationString.equals("")
								|| endLocationString.equals("")) {
							return;
						}
						Intent intent = new Intent();
						intent.setClass(FingerUIMultiple.this,
								FingerUIMultipleRoomServer.class);
						Bundle extras = new Bundle();
						extras.putString("raceName", raceName);
						extras.putString("start", startLocationString);
						extras.putString("end", endLocationString);
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

				// alert.getWindow().setLayout(width / 2, height / 2);
				alert.setTitle("建立賽道");

			}
		});

		searchButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				SharedPreferences settings = getSharedPreferences("FingerRunner", 0);
				String userName = settings.getString("name", "");
				client = new LanConnectionClient(FingerUIMultiple.this,userName,mainHandler);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				final host[] ListHost = client.showHosts();
				List<String> listStr = new ArrayList<String>();
				for (int i = 0; i < ListHost.length; ++i) {
					listStr.add(ListHost[i].raceName);
				}
				final String[] aryStr = listStr.toArray(new String[0]);
				// TODO Auto-generated method stub
				Builder MyAlertDialog = new AlertDialog.Builder(
						FingerUIMultiple.this);
				MyAlertDialog.setTitle("賽道選擇");
				// 建立選擇的事件
				DialogInterface.OnClickListener ListClick = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						try {
							raceName = aryStr[which];
							client.startTCPconnection(InetAddress.getByName((ListHost[which].ipAddress)));
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				};
				// 建立按下取消什麼事情都不做的事件
				DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				};
				MyAlertDialog.setItems(aryStr, ListClick);
				MyAlertDialog.setNeutralButton("取消", OkClick);
				MyAlertDialog.show();
			}
		});

	}
}
