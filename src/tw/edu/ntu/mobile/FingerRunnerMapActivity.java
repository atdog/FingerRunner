package tw.edu.ntu.mobile;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class FingerRunnerMapActivity extends MapActivity {
	private MapView mapView;
	private MapController mapController;
	private MyLocationOverlay mylayer;
	private List<GeoPoint> pathPoints;
	private Handler mHandler;
	private Handler centerHandler;
	private Context myContext;
	private Long startTime;
	private Handler timehandler = new Handler();

	
    private VelocityTracker m_tracker;
    private int steps=0;//用來計算走了幾步
    
    public static boolean centerchange=true;
    //用來計算往上往下的行為
    int point0_down_Y=0;
    int point0_up_Y=0;
    int point1_down_Y=0;
    int point1_up_Y=0;
    
    //用來計算左往右的行為
    int point0_down_X=0;
    int point0_up_X=0;
    int point1_down_X=0;
    int point1_up_X=0;
    
    boolean sendMessageToHandler=false;
    
    private long touchtime = 0;//用來判斷使用者是否同時用兩隻手在玩
    private float totalLength = 0;
    private int delayMillis = 500;
    
    //顯示主角的imageView
    private ImageView meImageView;
    
    //顯示跑步方向的imageView
    private ImageView directionImageView;
    
    //顯示路名以及提示資源的TextView
    private TextView routeNameTextView;
    private TextView hintTextView;
    
    private JSONArray conerList;//用來紀錄轉角資訊的JSONArray
    private int coner_num;//用來紀錄到第幾個轉角的變數
    private Boolean inLine;//用來判斷是否要到下一段路了
    private Double preLat_y;//計算位移量 
    private Double prelng_x;//計算位移量 
    private Boolean showRouteName;//判斷這時候是否要顯示路名
    private Boolean first=true;//判斷是否第一次載入畫面
    
    //記錄現在要往那邊走才是有效方向
    private int direction;//0 1 2 3 就是上下左右
    
    //紀錄時間
    Long minius;
    Long seconds;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//消除標題列
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//消除狀態列
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main);
		
		//找出所有UI
		findAllView();
		
		mHandler = new mainHandler();
		centerHandler = new centerHandler();
		// map 羅盤, built in zoom in/out init
		mapInit();
		// draw the first route path
		new RoutePath().execute(getIntent().getExtras().getString("start"), getIntent().getExtras().getString("end"));
		
		
		//初始化記錄轉角資訊的JSONArray
		conerList = new JSONArray();
		coner_num = 0;
		inLine = false;
		showRouteName = false;
		preLat_y = 0.0;
		prelng_x = 0.0;
		

		//取得目前時間
		startTime = System.currentTimeMillis();
		//設定定時要執行的方法
		timehandler.removeCallbacks(updateTimer);
		//設定Delay的時間
		timehandler.postDelayed(updateTimer, 1000);
		
		
		Log.d("Archer","現在位於「"+RouteInfo.LatLngToAddressName(25.015508, 121.542471)+"」上");
	}

	//固定要執行的方法
	private Runnable updateTimer = new Runnable() {
	public void run() {
		
		Long spentTime = System.currentTimeMillis() - startTime;
		//計算目前已過分鐘數
		minius = (spentTime/1000)/60;
		//計算目前已過秒數
		seconds = (spentTime/1000) % 60;
		
		handler.postDelayed(this, 1000);
	}
	};
	
	public void findAllView(){
		routeNameTextView = (TextView) findViewById(R.id.routeNameTextView);
		hintTextView = (TextView) findViewById(R.id.hintTextView);
		meImageView = (ImageView) findViewById(R.id.meImageView);
		directionImageView = (ImageView) findViewById(R.id.directionImageView);
	}
	

	
	public void mapInit() {
		mapView = (MapView) findViewById(R.id.mapView);
		// mapView.setBuiltInZoomControls(true);

		mapController = mapView.getController();
		mapController.setZoom(19);

		mylayer = new MyLocationOverlay(this, mapView);
		mylayer.enableCompass();

		List<Overlay> overlays = mapView.getOverlays();
		overlays.add(mylayer);
		
		myContext = this;
	}

	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			Log.d("Archer", "現在速度為：" + steps + " steps/s");
			totalLength += steps*5;
			centerchange =true;
			drawRoutePath(pathPoints, totalLength);
			
			steps = 0;
			sendMessageToHandler = false;
		}
	};

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	private class RoutePath extends AsyncTask<String, Integer, List<GeoPoint>> {
		private final String mapAPI = "http://maps.google.com/maps/api/directions/json?"
				+ "origin={0}&destination={1}&language=zh-TW&sensor=true&mode=walking";
		private String _from, _to;
		private List<GeoPoint> _points = new ArrayList<GeoPoint>();

		@Override
		protected List<GeoPoint> doInBackground(String... params) {
			if (params.length < 0)
				return null;

			_from = params[0];
			_to = params[1];

			String url = MessageFormat.format(mapAPI, _from, _to);
			Log.i("map", url);
			HttpGet get = new HttpGet(url);
			String strResult = "";
			try {
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
				HttpClient httpClient = new DefaultHttpClient(httpParameters);

				HttpResponse httpResponse = null;
				httpResponse = httpClient.execute(get);

				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					strResult = EntityUtils.toString(httpResponse.getEntity());

					JSONObject jsonObject = new JSONObject(strResult);
					JSONArray routeObject = jsonObject.getJSONArray("routes");
					String polyline = routeObject.getJSONObject(0)
							.getJSONObject("overview_polyline")
							.getString("points");
					
					
					for(int i=0;i<routeObject.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps").length();i++){
//						Log.d("Archer","方向提示>>"+routeObject.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps").getJSONObject(i).getString("html_instructions"));
//						Log.d("Archer","上述那一段路起點緯度(Y)"+routeObject.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps").getJSONObject(i).getJSONObject("start_location").getDouble("lat"));
//						Log.d("Archer","上述那一段路起點經度(X)"+routeObject.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps").getJSONObject(i).getJSONObject("start_location").getDouble("lng"));
//						Log.d("Archer","上述那一段路終點緯度(Y)"+routeObject.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps").getJSONObject(i).getJSONObject("end_location").getDouble("lat"));
//						Log.d("Archer","上述那一段路終點經度(X)"+routeObject.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps").getJSONObject(i).getJSONObject("end_location").getDouble("lng")+"\n\n");
						
						conerList = routeObject.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
						//Log.d("Archer","check lng>>"+conerList.getJSONObject(conerList.length()-1).getJSONObject("end_location").getDouble("lng"));
						
						
					}
					
					
					
					if (polyline.length() > 0) {
						decodePolylines(polyline);

					}

				}
			} catch (Exception e) {
				Log.e("map", e.toString());
			}
			pathPoints = _points;
			return _points;
		}

		private void decodePolylines(String poly) {
			int len = poly.length();
			int index = 0;
			int lat = 0;
			int lng = 0;

			while (index < len) {
				int b, shift = 0, result = 0;
				do {
					b = poly.charAt(index++) - 63;
					result |= (b & 0x1f) << shift;
					shift += 5;
				} while (b >= 0x20);
				int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
				lat += dlat;

				shift = 0;
				result = 0;
				do {
					b = poly.charAt(index++) - 63;
					result |= (b & 0x1f) << shift;
					shift += 5;
				} while (b >= 0x20);
				int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
				lng += dlng;

				GeoPoint p = new GeoPoint((int) (((double) lat / 1E5) * 1E6),
						(int) (((double) lng / 1E5) * 1E6));
				_points.add(p);

			}
		}

		protected void onPostExecute(List<GeoPoint> points) {
			drawRoutePath(points, 0);

			//new Timer(mHandler, 1).start();

		}

	}

	private void drawRoutePath(List<GeoPoint> points, float subLength) {
		if (points.size() > 0) {
			LineItemizedOverlay mOverlay = new LineItemizedOverlay(points,
					subLength, centerHandler);
			List<Overlay> overlays = mapView.getOverlays();
			overlays.clear();
			overlays.add(mylayer);
			overlays.add(0, mOverlay);
			// mapController.animateTo(points.get(0));
		}
	}

	private class mainHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			float subLength = msg.getData().getFloat("subLength");
			drawRoutePath(pathPoints, subLength);
			new Timer(mHandler, ++subLength).start();
			super.handleMessage(msg);
		}
	}

	private class centerHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			GeoPoint startPoint = (GeoPoint) msg.obj;
			mapView.getController().setCenter((GeoPoint) msg.obj);
			Double nowLat_y = startPoint.getLatitudeE6()/1000000.0;
			Double nowlng_x = startPoint.getLongitudeE6()/1000000.0;
 
			//下列四個變數為，用來幫助判斷是否在某段路上的變數
			Double startLat_y;
			Double endLat_y=0.0;
			Double startLng_x;
			Double endLng_x=0.0;
			
			
			//Log.d("Archer",String.valueOf(startPoint.getLongitudeE6()/1000000.0));
			//Log.d("Archer",String.valueOf(startPoint.getLatitudeE6()/1000000.0));
			
			
			
			if((Math.abs(nowLat_y-preLat_y)>(0.000025*6)||Math.abs(nowlng_x-prelng_x)>(0.000025*6))&&!showRouteName){
				Log.d("Archer"," 現在位於「"+RouteInfo.LatLngToAddressName(nowLat_y, nowlng_x)+"」上");
				hintTextView.setText("現在位於");
				routeNameTextView.setText("「"+RouteInfo.LatLngToAddressName(nowLat_y, nowlng_x)+"」");
				showRouteName = true;
				preLat_y = nowLat_y;
				prelng_x = nowlng_x;
			}
			
			
			
			//判斷現在是在那一段路上（用轉角跟轉角之間來判斷）
			if(conerList!=null&&coner_num<conerList.length()-1){
					
					try {
						
						startLat_y = conerList.getJSONObject(coner_num).getJSONObject("start_location").getDouble("lat");
						startLng_x = conerList.getJSONObject(coner_num).getJSONObject("start_location").getDouble("lng");
						endLat_y = conerList.getJSONObject(coner_num).getJSONObject("end_location").getDouble("lat");
						endLng_x = conerList.getJSONObject(coner_num).getJSONObject("end_location").getDouble("lng");
						
						
						if(Math.abs(startLat_y-endLat_y)>Math.abs(startLng_x-endLng_x)){
							//上下的路
							if(startLat_y-endLat_y>0){
								//向下的路，並查看現在是否在那條路上，如果第一次判斷方向就把對應的圖畫上去
								if(first){
									first = false;
									direction = 1;
									meImageView.setImageResource(R.drawable.down);
									directionImageView.setImageResource(R.drawable.arrow_down);
								}
			
								if(nowLat_y<startLat_y&&nowLat_y>endLat_y&&!inLine){
									Log.d("Archer","現在位於「"+RouteInfo.LatLngToAddressName(nowLat_y, nowlng_x)+"」上，前面轉角處>>"+conerList.getJSONObject(coner_num+1).getString("html_instructions"));
									inLine = true;
									direction = 1;
									meImageView.setImageResource(R.drawable.down);
									directionImageView.setImageResource(R.drawable.arrow_down);
									
								}
								if((coner_num!=conerList.length()-1)&&inLine&&nowLat_y-0.000050<=endLat_y){
									//走到下一段路了
									inLine = false;
									coner_num++;
									
									Log.d("Archer","check lng>>"+conerList.getJSONObject(coner_num).getJSONObject("end_location").getDouble("lng"));
									Log.d("Archer",conerList.length()+"  "+String.valueOf(coner_num) );
									
								}
								if((coner_num!=conerList.length()-1)&&inLine&&(Math.abs(nowLat_y-preLat_y)>(0.000025*6)||Math.abs(nowlng_x-prelng_x)>(0.000025*6))&&showRouteName){
									hintTextView.setText("前方轉角處");
									routeNameTextView.setText(Html.fromHtml(conerList.getJSONObject(coner_num+1).getString("html_instructions")));
									showRouteName = false;
									preLat_y = nowLat_y;
									prelng_x = nowlng_x;
									
								}
							}else{
								//向上的路
								
								//向上的路，並查看現在是否在那條路上，如果第一次判斷方向就把對應的圖畫上去
								if(first){
									first = false;
									direction = 0;
									meImageView.setImageResource(R.drawable.up);
									directionImageView.setImageResource(R.drawable.arrow_up);
								}
								
								if(nowLat_y>startLat_y&&nowLat_y<endLat_y&&!inLine){
									Log.d("Archer","現在位於「"+RouteInfo.LatLngToAddressName(nowLat_y, nowlng_x)+"」上，前面轉角處>>"+conerList.getJSONObject(coner_num+1).getString("html_instructions"));
									inLine = true;
									direction = 0;
									meImageView.setImageResource(R.drawable.up);
									directionImageView.setImageResource(R.drawable.arrow_up);
								}
								if((coner_num!=conerList.length()-1)&&inLine&&nowLat_y>=endLat_y-0.000050){
									//走到下一段路了
									inLine = false;
									coner_num++;
									
									Log.d("Archer","check lng>>"+conerList.getJSONObject(coner_num).getJSONObject("end_location").getDouble("lng"));
									Log.d("Archer",conerList.length()+"  "+String.valueOf(coner_num) );
								}
								if((coner_num!=conerList.length()-1)&&inLine&&(Math.abs(nowLat_y-preLat_y)>(0.000025*6)||Math.abs(nowlng_x-prelng_x)>(0.000025*6))&&showRouteName){
									hintTextView.setText("前方轉角處");
									routeNameTextView.setText(Html.fromHtml(conerList.getJSONObject(coner_num+1).getString("html_instructions")));
									showRouteName = false;
									preLat_y = nowLat_y;
									prelng_x = nowlng_x;
									
								}
							}
						}else{
							//左右的路
							if(startLng_x-endLng_x>0){
								//向左的路，並查看現在是否在那條路上
								
								//向左的路，並查看現在是否在那條路上，如果第一次判斷方向就把對應的圖畫上去
								if(first){
									first = false;
									direction = 2;
									meImageView.setImageResource(R.drawable.left);
									directionImageView.setImageResource(R.drawable.arrow_left);
								}
								
								if(nowlng_x<startLng_x&&nowlng_x>endLng_x&&!inLine){
									//RouteInfo.LatLngToAddressName(nowLat_y, nowlng_x);
									Log.d("Archer","現在位於「"+RouteInfo.LatLngToAddressName(nowLat_y, nowlng_x)+"」上，前面轉角處>>"+conerList.getJSONObject(coner_num+1).getString("html_instructions"));
									inLine = true;
									direction = 2;
									meImageView.setImageResource(R.drawable.left);
									directionImageView.setImageResource(R.drawable.arrow_left);
								}
								if((coner_num!=conerList.length()-1)&&inLine&&nowlng_x-0.000050<=endLng_x){
									//走到下一段路了
									inLine = false;
									coner_num++;
									
									Log.d("Archer","check lng>>"+conerList.getJSONObject(coner_num).getJSONObject("end_location").getDouble("lng"));
									Log.d("Archer",conerList.length()+"  "+String.valueOf(coner_num) );
								}
								if((coner_num!=conerList.length()-1)&&inLine&&(Math.abs(nowLat_y-preLat_y)>(0.000025*6)||Math.abs(nowlng_x-prelng_x)>(0.000025*6))&&showRouteName){
									hintTextView.setText("前方轉角處");
									routeNameTextView.setText(Html.fromHtml(conerList.getJSONObject(coner_num+1).getString("html_instructions")));
									showRouteName = false;
									preLat_y = nowLat_y;
									prelng_x = nowlng_x;
									
								}
							}else{
								//向右的路
								
								//向右的路，並查看現在是否在那條路上，如果第一次判斷方向就把對應的圖畫上去
								if(first){
									first = false;
									direction = 3;
									meImageView.setImageResource(R.drawable.right);
									directionImageView.setImageResource(R.drawable.arrow_right);
								}
								
								
								if(nowlng_x>startLng_x&&nowlng_x<endLng_x&&!inLine){
									Log.d("Archer","現在位於「"+RouteInfo.LatLngToAddressName(nowLat_y, nowlng_x)+"」上，前面轉角處>>"+conerList.getJSONObject(coner_num+1).getString("html_instructions"));
									inLine = true;
									direction = 3;
									meImageView.setImageResource(R.drawable.right);
									directionImageView.setImageResource(R.drawable.arrow_right);
								}
								if((coner_num!=conerList.length()-1)&&inLine&&nowlng_x>=endLng_x-0.000050){
									inLine = false;
									coner_num++;
									
									Log.d("Archer","check lng>>"+conerList.getJSONObject(coner_num).getJSONObject("end_location").getDouble("lng"));
									Log.d("Archer",conerList.length()+"  "+String.valueOf(coner_num) );
								}
								if((coner_num!=conerList.length()-1)&&inLine&&(Math.abs(nowLat_y-preLat_y)>(0.000025*6)||Math.abs(nowlng_x-prelng_x)>(0.000025*6))&&showRouteName){
									hintTextView.setText("前方轉角處");
									routeNameTextView.setText(Html.fromHtml(conerList.getJSONObject(coner_num+1).getString("html_instructions")));
									showRouteName = false;
									preLat_y = nowLat_y;
									prelng_x = nowlng_x;
									
								}
							}
						
						
						}
						
						
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
					
					
				}else if(coner_num==conerList.length()-1){
					showRouteName = false;
					try {
						startLat_y = conerList.getJSONObject(coner_num).getJSONObject("start_location").getDouble("lat");
						startLng_x = conerList.getJSONObject(coner_num).getJSONObject("start_location").getDouble("lng");
						endLat_y = conerList.getJSONObject(coner_num).getJSONObject("end_location").getDouble("lat");
						endLng_x = conerList.getJSONObject(coner_num).getJSONObject("end_location").getDouble("lng");
						if(Math.abs(startLat_y-endLat_y)>Math.abs(startLng_x-endLng_x)){
							//上下的路

								if(startLat_y-endLat_y>0){
									direction = 1;
									meImageView.setImageResource(R.drawable.down);
									directionImageView.setImageResource(R.drawable.arrow_down);
								}else{
									direction = 0;
									meImageView.setImageResource(R.drawable.up);
									directionImageView.setImageResource(R.drawable.arrow_up);
								}
						}else{
								if(startLng_x-endLng_x>0){
									direction = 2;
									meImageView.setImageResource(R.drawable.left);
									directionImageView.setImageResource(R.drawable.arrow_left);
								}else{
									direction = 3;
									meImageView.setImageResource(R.drawable.right);
									directionImageView.setImageResource(R.drawable.arrow_right);
								}
							}
							
						
						
						Log.d("Archer","end>>"+endLat_y+","+endLng_x);
						Log.d("Archer","me>>"+nowLat_y+","+nowlng_x);
						switch (direction) {
						case 0:
							if(nowLat_y>=endLat_y-0.000100){
								Log.d("Archer","跑完了0 "+coner_num+" "+conerList.getJSONObject(coner_num).getJSONObject("end_location").getDouble("lat"));
								Bundle b = new Bundle();
								b.putString("start", getIntent().getExtras().getString("start"));
								b.putString("end", getIntent().getExtras().getString("end"));
								b.putLong("minius", minius);
								b.putLong("seconds", seconds);
								Intent intent = new Intent();
								intent.putExtras(b);
						
								intent.setClass(FingerRunnerMapActivity.this, ShowRunTimeActivity.class);
								
								startActivity(intent);
								finish();
							}
							break;
						case 1:
							if(nowLat_y-0.000100<=endLat_y){
								Log.d("Archer","跑完了1 "+coner_num+" "+conerList.getJSONObject(coner_num).getJSONObject("end_location").getDouble("lat"));
								Bundle b = new Bundle();
								b.putString("start", getIntent().getExtras().getString("start"));
								b.putString("end", getIntent().getExtras().getString("end"));
								b.putLong("minius", minius);
								b.putLong("seconds", seconds);
								Intent intent = new Intent();
								intent.putExtras(b);
						
								intent.setClass(FingerRunnerMapActivity.this, ShowRunTimeActivity.class);
								
								startActivity(intent);
								finish();
							}
							
							break;
						case 2:
							if(nowlng_x-0.000100<=endLng_x){
								Log.d("Archer","跑完了2 "+coner_num+" "+conerList.getJSONObject(coner_num).getJSONObject("end_location").getDouble("lng"));
								Bundle b = new Bundle();
								b.putString("start", getIntent().getExtras().getString("start"));
								b.putString("end", getIntent().getExtras().getString("end"));
								b.putLong("minius", minius);
								b.putLong("seconds", seconds);
								Intent intent = new Intent();
								intent.putExtras(b);
						
								intent.setClass(FingerRunnerMapActivity.this, ShowRunTimeActivity.class);
								
								startActivity(intent);
								finish();
							}
							break;
						case 3:
							if(nowlng_x>=endLng_x-0.000100){
								Log.d("Archer","跑完了3 "+coner_num+" "+conerList.getJSONObject(coner_num).getJSONObject("end_location").getDouble("lng"));
								Bundle b = new Bundle();
								b.putString("start", getIntent().getExtras().getString("start"));
								b.putString("end", getIntent().getExtras().getString("end"));
								b.putLong("minius", minius);
								b.putLong("seconds", seconds);
								Intent intent = new Intent();
								intent.putExtras(b);
						
								intent.setClass(FingerRunnerMapActivity.this, ShowRunTimeActivity.class);
								
								startActivity(intent);
								finish();
							}
							break;

						default:
							break;
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}

		
		
		}
		
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int pointerCount = event.getPointerCount();
		int type = event.getActionMasked();// 用getActionMasked可以抓每一次點擊下去的event，而且可以在用id去判斷是哪一個點擊event
		// 如果是用getAction，那用MotionEvent.ACTION_POINTER_UP會找不到東西

		switch (type) {
		case MotionEvent.ACTION_UP:
			Date date = new Date();

			// Log.d("Archer","ACTION_UPPointerId"+event.getPointerId((event.getActionIndex()))+"時間"+date.getTime());

			switch (event.getPointerId((event.getActionIndex()))) {
			case 0:
				point0_up_Y = (int) event.getY(event.getActionIndex());
				point0_up_X = (int) event.getX(event.getActionIndex());
				// Log.d("Archer","放開PointerId"+event.getPointerId((event.getActionIndex()))+"Ｙ坐標："+event.getY());
				
				//用direction來判斷是否走的是有效步
				switch (direction) {
				case 0:
					//方向是往上才加一步
					if ((point0_up_Y - point0_down_Y) >= 55) {

						// 如果時間間隔沒有超過一定時間的話（這邊是25），代表他用兩隻手指頭一起完，就不加一步了
						if (date.getTime() - 25 > touchtime) {
							Log.d("Archer", "走了一步");
							steps++;
						}

					}
					break;
				case 1:
					//方向是往下才加一步
					if ((point0_down_Y - point0_up_Y ) >= 55) {

						// 如果時間間隔沒有超過一定時間的話（這邊是25），代表他用兩隻手指頭一起完，就不加一步了
						if (date.getTime() - 25 > touchtime) {
							Log.d("Archer", "走了一步");
							steps++;
						}

					}
					break;
				
				case 2:
					//方向是往左才加一步
					if ((point0_up_X - point0_down_X) >= 55) {

						// 如果時間間隔沒有超過一定時間的話（這邊是25），代表他用兩隻手指頭一起完，就不加一步了
						if (date.getTime() - 25 > touchtime) {
							Log.d("Archer", "走了一步");
							steps++;
						}

					}
					break;
				
				case 3:
					//方向是往右才加一步
					if ((point0_down_X - point0_up_X ) >= 55) {

						// 如果時間間隔沒有超過一定時間的話（這邊是25），代表他用兩隻手指頭一起完，就不加一步了
						if (date.getTime() - 25 > touchtime) {
							Log.d("Archer", "走了一步");
							steps++;
						}

					}
					break;

				default:
					break;
				}
				

				break;
			case 1:
				point1_up_Y = (int) event.getY(event.getActionIndex());
				point1_up_X = (int) event.getX(event.getActionIndex());
				// Log.d("Archer","放開PointerId"+event.getPointerId((event.getActionIndex()))+"Ｙ坐標："+point1_up_Y);
								
				//用direction來判斷是否走的是有效步
				switch (direction) {
				case 0:
					//方向是往上才加一步
					if ((point1_up_Y - point1_down_Y) >= 55) {

						// 如果時間間隔沒有超過一定時間的話（這邊是25），代表他用兩隻手指頭一起完，就不加一步了
						if (date.getTime() - 25 > touchtime) {
							Log.d("Archer", "走了一步");
							steps++;
						}

					}
					break;
				case 1:
					//方向是往下才加一步
					if ((point1_down_Y - point1_up_Y ) >= 55) {

						// 如果時間間隔沒有超過一定時間的話（這邊是25），代表他用兩隻手指頭一起完，就不加一步了
						if (date.getTime() - 25 > touchtime) {
							Log.d("Archer", "走了一步");
							steps++;
						}

					}
					break;
				
				case 2:
					//方向是往左才加一步
					if ((point1_up_X - point1_down_X) >= 55) {

						// 如果時間間隔沒有超過一定時間的話（這邊是25），代表他用兩隻手指頭一起完，就不加一步了
						if (date.getTime() - 25 > touchtime) {
							Log.d("Archer", "走了一步");
							steps++;
						}

					}
					break;
				
				case 3:
					//方向是往右才加一步
					if ((point1_down_X - point1_up_X ) >= 55) {

						// 如果時間間隔沒有超過一定時間的話（這邊是25），代表他用兩隻手指頭一起完，就不加一步了
						if (date.getTime() - 25 > touchtime) {
							Log.d("Archer", "走了一步");
							steps++;
						}

					}
					break;

				default:
					break;
				}
				
				
				break;

			default:
				break;
			}

			break;
		case MotionEvent.ACTION_POINTER_UP:
			// 把時間記下來和ACTION_UP去比較，如果太近的話，代表他用兩隻手指頭一起完
			Date date_ACTION_POINTER_UP = new Date();
			touchtime = date_ACTION_POINTER_UP.getTime();
			// Log.d("Archer","ACTION_POINTER_UPPointerId"+event.getPointerId((event.getActionIndex()))+"時間"+touchtime);

			switch (event.getPointerId((event.getActionIndex()))) {
			case 0:
				point0_up_Y = (int) event.getY(event.getActionIndex());
				point0_up_X = (int) event.getX(event.getActionIndex());
				// Log.d("Archer","放開PointerId"+event.getPointerId((event.getActionIndex()))+"Ｙ坐標："+event.getY());
				
				//用direction來判斷是否走的是有效步
				switch (direction) {
				case 0:
					//方向是往上才加一步
					if ((point0_up_Y - point0_down_Y) >= 55) {
						Log.d("Archer", "走了一步");
						steps++;
					}
					break;
				case 1:
					//方向是往下才加一步
					if ((point0_down_Y - point0_up_Y ) >= 55) {
						Log.d("Archer", "走了一步");
						steps++;
					}
					break;
				
				case 2:
					//方向是往左才加一步
					if ((point0_up_X - point0_down_X) >= 55) {
						Log.d("Archer", "走了一步");
						steps++;
					}
					break;
				
				case 3:
					//方向是往右才加一步
					if ((point0_down_X - point0_up_X ) >= 55) {
						Log.d("Archer", "走了一步");
						steps++;
					}
					break;

				default:
					break;
				}
				
				
				break;
			case 1:
				point1_up_Y = (int) event.getY(event.getActionIndex());
				point1_up_X = (int) event.getX(event.getActionIndex());
				// Log.d("Archer","放開PointerId"+event.getPointerId((event.getActionIndex()))+"Ｙ坐標："+point1_up_Y);
				if ((point1_up_Y - point1_down_Y) >= 55) {

					Log.d("Archer", "走了一步");
					steps++;

				}
				//用direction來判斷是否走的是有效步
				switch (direction) {
				case 0:
					//方向是往上才加一步
					if ((point1_up_Y - point1_down_Y) >= 55) {
						Log.d("Archer", "走了一步");
						steps++;
					}
					break;
				case 1:
					//方向是往下才加一步
					if ((point1_down_Y - point1_up_Y ) >= 55) {
						Log.d("Archer", "走了一步");
						steps++;
					}
					break;
				
				case 2:
					//方向是往左才加一步
					if ((point1_up_X - point1_down_X) >= 55) {
						Log.d("Archer", "走了一步");
						steps++;
					}
					break;
				
				case 3:
					//方向是往右才加一步
					if ((point1_down_X - point1_up_X ) >= 55) {
						Log.d("Archer", "走了一步");
						steps++;
					}
					break;

				default:
					break;
				}
				
				break;

			default:
				break;
			}
			break;

		case MotionEvent.ACTION_DOWN:
			// Log.d("Archer","ACTION_DOWNPointerId"+event.getPointerId((event.getActionIndex())));

			// 如果還在計算一秒內跑幾步的話，就不在觸發一次handler

			if (!sendMessageToHandler) {
				Log.d("Archer", "開始計算跑速\n");
				sendMessageToHandler = true;
				handler.sendEmptyMessageDelayed(0, delayMillis);

			}

			switch (event.getPointerId((event.getActionIndex()))) {
			case 0:
				point0_down_Y = (int) event.getY(event.getActionIndex());
				point0_down_X = (int) event.getX(event.getActionIndex());
				// Log.d("Archer","按下PointerId"+event.getPointerId((event.getActionIndex()))+"Ｙ坐標："+event.getY());
				break;
			case 1:
				point1_down_Y = (int) event.getY(event.getActionIndex());
				point1_down_X = (int) event.getX(event.getActionIndex());
				// Log.d("Archer","按下PointerId"+event.getPointerId((event.getActionIndex()))+"Ｙ坐標："+event.getY());
				break;

			default:
				break;
			}

			break;

		case MotionEvent.ACTION_POINTER_DOWN:
			// Log.d("Archer","按下PointerId"+event.getPointerId((event.getActionIndex())));
			if (!sendMessageToHandler) {
				Log.d("Archer", "開始計算跑速\n");
				sendMessageToHandler = true;
				handler.sendEmptyMessageDelayed(0, delayMillis);

			}
			// Log.d("Archer","按下PointerId"+event.getPointerId((event.getActionIndex())));
			switch (event.getPointerId((event.getActionIndex()))) {
			case 0:
				point0_down_Y = (int) event.getY(event.getActionIndex());
				point0_down_X = (int) event.getX(event.getActionIndex());
				// Log.d("Archer","按下PointerId"+event.getPointerId((event.getActionIndex()))+"Ｙ坐標："+event.getY());
				break;
			case 1:
				point1_down_Y = (int) event.getY(event.getActionIndex());
				point1_down_X = (int) event.getX(event.getActionIndex());
				// Log.d("Archer","按下PointerId"+event.getPointerId((event.getActionIndex()))+"Ｙ坐標："+event.getY());
				break;

			default:
				break;
			}

			break;

		default:
			break;
		}

		return super.onTouchEvent(event);
	}

}