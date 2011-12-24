package tw.edu.ntu.mobile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class LineItemizedOverlay extends Overlay {
	private List<GeoPoint> mOverlays = new ArrayList<GeoPoint>();
	private static final int ALPHA = 120;
	private static final float STROKE = 10;
	private final Path previous_path;
	private final Path path;
	private final Point p;
	private Point previous_p;
	private final Paint paint;
	private final float finalSubLength;
	private final Handler mainHandler;

	public LineItemizedOverlay(List<GeoPoint> mOverlays, float subLength, Handler mainHandler) {
		this.mOverlays = mOverlays;
		path = new Path();
		previous_path = new Path();
		p = new Point();
		paint = new Paint();
		this.finalSubLength = subLength;
		this.mainHandler = mainHandler;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);

		// 線的樣式
		paint.setColor(Color.argb(120, 150, 0, 0));
		paint.setAlpha(ALPHA);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(STROKE);
		paint.setStyle(Paint.Style.STROKE);

		Projection prj = mapView.getProjection();
		/*
		 * Reverse the geoPointList first
		 */
		// Collections.reverse(mOverlays);

		Iterator<GeoPoint> it = mOverlays.iterator();
		prj.toPixels(it.next(), p);
		previous_p = new Point(p);

		boolean findStartPoint = false;
		float previousPathLength;
		float subLength = finalSubLength;
		while (it.hasNext()) {
			prj.toPixels(it.next(), p);
			if (!findStartPoint) { // 尚未設定初始點
				previous_path.rewind();
				previous_path.moveTo(previous_p.x, previous_p.y);
				previous_path.lineTo(p.x, p.y);
				
				previousPathLength = new PathMeasure(previous_path, false)
						.getLength();
				if (previousPathLength > subLength) {
					/*
					 * 表示在path中間
					 */
					// set start point
					Point startPoint = new Point(
							previous_p.x + (int) ((float)( p.x - previous_p.x ) * ( subLength / previousPathLength )),
							previous_p.y + (int) ((float)( p.y - previous_p.y ) * ( subLength / previousPathLength )));
					path.rewind();
					path.moveTo(startPoint.x, startPoint.y);

					path.lineTo(p.x, p.y);
					Message m = new Message();
					m.obj = prj.fromPixels(startPoint.x, startPoint.y);
					mainHandler.sendMessage(m);
					findStartPoint = true;
				} else if (previousPathLength == subLength) {
					path.moveTo(p.x, p.y);
					findStartPoint = true;
				}
				subLength -= previousPathLength;
				previous_p = new Point(p);
			} else {
				path.lineTo(p.x, p.y);
			}

		}
		path.setLastPoint(p.x, p.y);
		canvas.drawPath(path, paint);
	}
	
}
