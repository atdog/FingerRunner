package tw.edu.ntu.mobile;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class OtherRunnerOverlay extends ItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	
	public OtherRunnerOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker) );
		// TODO Auto-generated constructor stub
	}

	@Override
	protected OverlayItem createItem(int arg0) {
		// TODO Auto-generated method stub
		return mOverlays.get(arg0);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return mOverlays.size();
	}
	
	public void addOverlay(OverlayItem overlay){
		mOverlays.add(overlay);
		populate();
	}

	
	
}
