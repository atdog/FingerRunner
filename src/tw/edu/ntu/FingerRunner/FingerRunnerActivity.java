package tw.edu.ntu.FingerRunner;




import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.TextView;


public class FingerRunnerActivity extends Activity {
    /** Called when the activity is first created. */
    private VelocityTracker m_tracker;
    private int steps=0;//用來計算走了幾步
    int point0_down_Y=0;
	int point0_up_Y=0;
	int point1_down_Y=0;
	int point1_up_Y=0;
	boolean sendMessageToHandler=false;
	
	private TextView speedTextView;
	private TextView commentTextView;
	private long touchtime = 0;//用來判斷使用者是否同時用兩隻手在玩
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        findAllView();
        
        
        
    }
	private void findAllView(){
		speedTextView = (TextView) findViewById(R.id.speedTextView);
		commentTextView = (TextView) findViewById(R.id.commentTextView);
	}
	// Load Data in another thread
	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			Log.d("Archer","現在速度為："+steps+" steps/s");
			speedTextView.setText("現在速度為："+steps+" steps/s");
			
			
			if(steps>10){
				commentTextView.setText("靠！！！你跟本是用手指賽跑之神");
			}else if(steps>8){
				commentTextView.setText("太強了！只比阿哲弱一點點了！！！");
			}else if(steps>=7){
				commentTextView.setText("猛喔，好快～");
			}else if(steps>5){
				commentTextView.setText("加油～ 你還蠻快的");
			}else if(steps>0&&steps<4){
				commentTextView.setText("你的手指有受傷麻？＠＠");
			}else if(steps==0){
				commentTextView.setText("不要站在原地拉屎！！！");
			}
			steps = 0;
			sendMessageToHandler = false;
		}
	};
	
@Override
public boolean onTouchEvent(MotionEvent event) {
	int pointerCount = event.getPointerCount();
	int type = event.getActionMasked();//用getActionMasked可以抓每一次點擊下去的event，而且可以在用id去判斷是哪一個點擊event
									   //如果是用getAction，那用MotionEvent.ACTION_POINTER_UP會找不到東西
	
	
	
	
	switch (type) {
	case MotionEvent.ACTION_UP:
		Date date = new Date();
		
		//Log.d("Archer","ACTION_UPPointerId"+event.getPointerId((event.getActionIndex()))+"時間"+date.getTime());
		
		switch (event.getPointerId((event.getActionIndex()))) {
		case 0:
			point0_up_Y = (int) event.getY(event.getActionIndex());
			//Log.d("Archer","放開PointerId"+event.getPointerId((event.getActionIndex()))+"Ｙ坐標："+event.getY());
			if((point0_up_Y-point0_down_Y)>=65){
				
				//如果時間間隔沒有超過一定時間的話（這邊是25），代表他用兩隻手指頭一起完，就不加一步了
				if(date.getTime()-25 > touchtime){
					Log.d("Archer","走了一步");
					steps++;
				}
				
				
			}
			
			break;
		case 1:
			point1_up_Y = (int) event.getY(event.getActionIndex());
			//Log.d("Archer","放開PointerId"+event.getPointerId((event.getActionIndex()))+"Ｙ坐標："+point1_up_Y);
			if((point1_up_Y-point1_down_Y)>=65){
				
				//如果時間間隔沒有超過一定時間的話（這邊是25），代表他用兩隻手指頭一起完，就不加一步了
				if(date.getTime()-25 > touchtime){
					Log.d("Archer","走了一步");
					steps++;
				}
				
			}
			break;

		default:
			break;
		}
		
		break;
	case MotionEvent.ACTION_POINTER_UP:
		//把時間記下來和ACTION_UP去比較，如果太近的話，代表他用兩隻手指頭一起完
		Date date_ACTION_POINTER_UP = new Date();
		touchtime = date_ACTION_POINTER_UP.getTime();
		//Log.d("Archer","ACTION_POINTER_UPPointerId"+event.getPointerId((event.getActionIndex()))+"時間"+touchtime);
		
		switch (event.getPointerId((event.getActionIndex()))) {
		case 0:
			point0_up_Y = (int) event.getY(event.getActionIndex());
			//Log.d("Archer","放開PointerId"+event.getPointerId((event.getActionIndex()))+"Ｙ坐標："+event.getY());
			if((point0_up_Y-point0_down_Y)>=65){
				
				Log.d("Archer","走了一步");
				steps++;

				
			}
			break;
		case 1:
			point1_up_Y = (int) event.getY(event.getActionIndex());
			//Log.d("Archer","放開PointerId"+event.getPointerId((event.getActionIndex()))+"Ｙ坐標："+point1_up_Y);
			if((point1_up_Y-point1_down_Y)>=65){
				
				Log.d("Archer","走了一步");
				steps++;

			}
			break;

		default:
			break;
		}
		break;
	
	case MotionEvent.ACTION_DOWN:
		//Log.d("Archer","ACTION_DOWNPointerId"+event.getPointerId((event.getActionIndex())));
		
		//如果還在計算一秒內跑幾步的話，就不在觸發一次handler
		
			if(!sendMessageToHandler){
				Log.d("Archer","開始計算跑速\n");
				sendMessageToHandler = true;
				handler.sendEmptyMessageDelayed(0, 1000);
				
			}
			
			switch (event.getPointerId((event.getActionIndex()))) {
			case 0:
				point0_down_Y = (int) event.getY(event.getActionIndex());
				//Log.d("Archer","按下PointerId"+event.getPointerId((event.getActionIndex()))+"Ｙ坐標："+event.getY());
				break;
			case 1:
				point1_down_Y = (int) event.getY(event.getActionIndex());
				//Log.d("Archer","按下PointerId"+event.getPointerId((event.getActionIndex()))+"Ｙ坐標："+event.getY());
				break;

			default:
				break;
			}
		
		
		
		
		break;
		
	case MotionEvent.ACTION_POINTER_DOWN:
		//Log.d("Archer","按下PointerId"+event.getPointerId((event.getActionIndex())));
		if(!sendMessageToHandler){
			Log.d("Archer","開始計算跑速\n");
			sendMessageToHandler = true;
			handler.sendEmptyMessageDelayed(0, 1000);
			
		}
		//Log.d("Archer","按下PointerId"+event.getPointerId((event.getActionIndex())));
		switch (event.getPointerId((event.getActionIndex()))) {
		case 0:
			point0_down_Y = (int) event.getY(event.getActionIndex());
			
			//Log.d("Archer","按下PointerId"+event.getPointerId((event.getActionIndex()))+"Ｙ坐標："+event.getY());
			break;
		case 1:
			point1_down_Y = (int) event.getY(event.getActionIndex());
			//Log.d("Archer","按下PointerId"+event.getPointerId((event.getActionIndex()))+"Ｙ坐標："+event.getY());
			break;

		default:
			break;
		}
			
		
		break;

	default:
		break;
	}
	
	
//	//當手指離開螢幕時處發
//	if(type == MotionEvent.ACTION_UP){
////		String clickEvent = String.format("在(%1$.1f,%2$.1f)放開\n",event.getX(0),event.getY(0));
////		//Log.d("Archer",clickEvent);
////		
////		//將放開的這一點也加入
////		m_tracker.addMovement(event);
////		
////		//計算到目前的速度
////		m_tracker.computeCurrentVelocity(1);
////		
////		//水平或垂直哪一個分量大就顯示該方向的訊息
////		if(Math.abs(m_tracker.getXVelocity(0))>Math.abs(m_tracker.getYVelocity(0))){
////			Log.d("Archer","水平方向："+m_tracker.getXVelocity(0));
////		}else{
////			Log.d("Archer","垂直方向："+m_tracker.getYVelocity(0));
////		}
////		
////		m_tracker.recycle();
////		m_tracker = null;
//		
//		
//	}
//	if(type == MotionEvent.ACTION_MOVE){
////		//在移動的時候把點加進去速度偵測器
////		if(pointerCount==1){
////			Log.d("Archer","(1)("+event.getPressure(0)+")");
////		}else if(pointerCount==2){
////			Log.d("Archer","(1)("+event.getPressure(0)+")\n");
////			Log.d("Archer","(2)("+event.getPressure(1)+")\n");
////		}
////		m_tracker.addMovement(event);
//		
//	}
//	
//	//當手按下時觸發
//	if(type == MotionEvent.ACTION_DOWN){
//		
//		
//		
//		
//		//Log.d("Archer","X: "+event.getX());
//		Log.d("Archer","Index: "+event.getActionIndex()+"\nPointerId"+event.getPointerId((event.getActionIndex())));
//		
//		
//		
////		m_tracker = VelocityTracker.obtain();
//		
//		
//		
//		
//	}
//	
//	if(type == MotionEvent.ACTION_POINTER_1_DOWN){
//		Log.d("Archer","Index: "+event.getActionIndex()+"\nPointerId"+event.getPointerId((event.getActionIndex())));
//	}
	
	return super.onTouchEvent(event);
}
	
}