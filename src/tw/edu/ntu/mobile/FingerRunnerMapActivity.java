package tw.edu.ntu.mobile;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.json.JSONObject;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

import android.R.integer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;

public class FingerRunnerMapActivity extends MapActivity {
	private MapView mapView;
	private MapController mapController;
	private MyLocationOverlay mylayer;
	private List<GeoPoint> pathPoints;
	private Handler mHandler;
	private Handler centerHandler;

	
    private VelocityTracker m_tracker;
    private int steps=0;//用來計算走了幾步
    int point0_down_Y=0;
    int point0_up_Y=0;
    int point1_down_Y=0;
    int point1_up_Y=0;
    boolean sendMessageToHandler=false;
    
    private long touchtime = 0;//用來判斷使用者是否同時用兩隻手在玩
    private float totalLength = 0;
    private int delayMillis = 500;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mHandler = new mainHandler();
		centerHandler = new centerHandler();
		// map 羅盤, built in zoom in/out init
		mapInit();
		// draw the first route path
		new RoutePath().execute("台灣大學", "台灣科技大學");

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
	}

	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			Log.d("Archer", "現在速度為：" + steps + " steps/s");
			totalLength += steps*5;
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
			// Log.d("map",startPoint.toString());
			super.handleMessage(msg);
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
				// Log.d("Archer","放開PointerId"+event.getPointerId((event.getActionIndex()))+"Ｙ坐標："+event.getY());
				if ((point0_up_Y - point0_down_Y) >= 65) {

					// 如果時間間隔沒有超過一定時間的話（這邊是25），代表他用兩隻手指頭一起完，就不加一步了
					if (date.getTime() - 25 > touchtime) {
						Log.d("Archer", "走了一步");
						steps++;
					}

				}

				break;
			case 1:
				point1_up_Y = (int) event.getY(event.getActionIndex());
				// Log.d("Archer","放開PointerId"+event.getPointerId((event.getActionIndex()))+"Ｙ坐標："+point1_up_Y);
				if ((point1_up_Y - point1_down_Y) >= 65) {

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
		case MotionEvent.ACTION_POINTER_UP:
			// 把時間記下來和ACTION_UP去比較，如果太近的話，代表他用兩隻手指頭一起完
			Date date_ACTION_POINTER_UP = new Date();
			touchtime = date_ACTION_POINTER_UP.getTime();
			// Log.d("Archer","ACTION_POINTER_UPPointerId"+event.getPointerId((event.getActionIndex()))+"時間"+touchtime);

			switch (event.getPointerId((event.getActionIndex()))) {
			case 0:
				point0_up_Y = (int) event.getY(event.getActionIndex());
				// Log.d("Archer","放開PointerId"+event.getPointerId((event.getActionIndex()))+"Ｙ坐標："+event.getY());
				if ((point0_up_Y - point0_down_Y) >= 65) {

					Log.d("Archer", "走了一步");
					steps++;

				}
				break;
			case 1:
				point1_up_Y = (int) event.getY(event.getActionIndex());
				// Log.d("Archer","放開PointerId"+event.getPointerId((event.getActionIndex()))+"Ｙ坐標："+point1_up_Y);
				if ((point1_up_Y - point1_down_Y) >= 65) {

					Log.d("Archer", "走了一步");
					steps++;

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
				// Log.d("Archer","按下PointerId"+event.getPointerId((event.getActionIndex()))+"Ｙ坐標："+event.getY());
				break;
			case 1:
				point1_down_Y = (int) event.getY(event.getActionIndex());
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

				// Log.d("Archer","按下PointerId"+event.getPointerId((event.getActionIndex()))+"Ｙ坐標："+event.getY());
				break;
			case 1:
				point1_down_Y = (int) event.getY(event.getActionIndex());
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