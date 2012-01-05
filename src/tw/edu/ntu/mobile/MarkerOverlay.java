package tw.edu.ntu.mobile;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MarkerOverlay extends Overlay {
	private final Context context;
	private final GeoPoint geoPoint;
	private final int drawable;

	public MarkerOverlay(Context context, GeoPoint geoPoint, int drawable) {
		this.context = context;
		this.geoPoint = geoPoint;
		this.drawable = drawable;
	}

	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
			long when) {
		// TODO Auto-generated method stub
		super.draw(canvas, mapView, shadow);

		// Convert geo coordinates to screen pixels
		Point screenPoint = new Point();
		mapView.getProjection().toPixels(geoPoint, screenPoint);

		// Read the image
		BitmapFactory.Options opt = new BitmapFactory.Options();
		//opt.inSampleSize = 5;
		Bitmap markerImage = BitmapFactory.decodeResource(
				context.getResources(), drawable, opt);

		// Draw it, centered around the given coordinates
		canvas.drawBitmap(markerImage, screenPoint.x - markerImage.getWidth()
				/ 2, screenPoint.y - markerImage.getHeight() / 2, null);
		return true;
	}

}
