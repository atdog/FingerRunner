package tw.edu.ntu.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class ChooseVenueActivity extends Activity {
	
	private EditText startEditText;
	private EditText endEditText;
	private Button startButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		//消除標題列
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//消除狀態列
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    setContentView(R.layout.choose_venue);
	    
	    //找出所有UI
		findAllView();
		
		setListener();
	    
	}
	
	
	
	public void findAllView(){
		startButton = (Button) findViewById(R.id.startButton);
		startEditText = (EditText) findViewById(R.id.startEditText);
		endEditText = (EditText) findViewById(R.id.endEditText);
	}
	
	//按下Start 鈕後跳到下一個Activity去
	public void setListener(){
		startButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Bundle b = new Bundle();
				b.putString("start", startEditText.getText().toString());
				b.putString("end", endEditText.getText().toString());
				b.putBoolean("hasOther", getIntent().getExtras().getBoolean("hasOther"));
				Intent intent = new Intent();
				intent.putExtras(b);
				
				intent.setClass(ChooseVenueActivity.this, FingerRunnerMapActivity.class);
				
				startActivity(intent);
				finish();
			}
		});
	}


}
