package tw.edu.ntu.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ShowRunTimeActivity extends Activity {

	private TextView timeTextView;
	private Button okButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		//消除標題列
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//消除狀態列
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    
	    
	    setContentView(R.layout.show_run_time);
	    
	    
	    
	    //找出所有UI
		findAllView();
		
		String me="Archer";
		String other="Dog";
		Boolean meIsFirst=false;
		long other_minius=0;
		long other_seconds=59;
		long other_milliseconds=0;
		
		if(getIntent().getExtras().getBoolean("hasOther")){
			if(getIntent().getExtras().getBoolean("first")){
				timeTextView.setText("第一名 "+me+"\n第二名 "+other);
			}else{
				timeTextView.setText("第一名 "+other+"\n第二名 "+me);
			}
			
	    }else{
	    	timeTextView.setText("從「"+getIntent().getExtras().getString("start")+"」\n到「"+getIntent().getExtras().getString("end")+"」\n"+"你花了\n"+getIntent().getExtras().getLong("minius")+"分"+getIntent().getExtras().getLong("seconds")+"秒"+getIntent().getExtras().getLong("milliseconds"));
	    }
		okButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
	}
	
	
	
	public void findAllView(){
		timeTextView = (TextView) findViewById(R.id.timer);
		okButton = (Button) findViewById(R.id.Okbutton);
	}
	
}
