package tw.edu.ntu.mobile;

import java.text.MessageFormat;

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


import android.util.Log;

public class RouteInfo {
	public static String LatLngToAddressName(double nowLat_y,double nowlng_x){
	String mapAPI = "http://maps.google.com/maps/api/geocode/json?"+"latlng={0},{1}&sensor=true&language=zh-TW";
	
	double _Lat = nowLat_y;
	double _Lng = nowlng_x;
	
	String StreeName="找不到路名";

	String url = MessageFormat.format(mapAPI, _Lat, _Lng);

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
						JSONArray addressArray = jsonObject.getJSONArray("results");
						
						for(int i=0;i<addressArray.length();i++){
							for(int j=0 ;j<addressArray.getJSONObject(i).getJSONArray("types").length();j++){
								if(addressArray.getJSONObject(i).getJSONArray("types").getString(j).equals("street_address")||addressArray.getJSONObject(i).getJSONArray("types").getString(j).equals("route")){
									for(int o=0;o<addressArray.getJSONObject(i).getJSONArray("address_components").length();o++){
										for(int c=0;c<addressArray.getJSONObject(i).getJSONArray("address_components").getJSONObject(o).getJSONArray("types").length();c++){
											if(addressArray.getJSONObject(i).getJSONArray("address_components").getJSONObject(o).getJSONArray("types").getString(c).equals("route")){
												return addressArray.getJSONObject(i).getJSONArray("address_components").getJSONObject(o).getString("long_name");
											}
										}
										
										
											
										}
									}
									
									
								}
							}
									
									
									
						}
						
						
						
				} catch (Exception e) {
					Log.e("map", e.toString());
				}
	
				return StreeName;
	}
}
