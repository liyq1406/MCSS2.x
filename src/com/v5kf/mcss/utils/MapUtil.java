package com.v5kf.mcss.utils;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.widget.TextView;

import com.tencent.mapsdk.raster.model.Marker;
import com.v5kf.client.lib.V5HttpUtil;
import com.v5kf.client.lib.callback.HttpResponseHandler;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.utils.cache.URLCache;

public class MapUtil {

	public static void getLocationTitle(final double x, final double y, final TextView titleTv, final Marker marker) {
		final String url = String.format(Locale.getDefault(), Config.MAP_WS_API_FORMAT, x, y);
		
		// 1.先获取对应的url缓存
		final URLCache urlCache = new URLCache();
		String responseString = urlCache.get(url);
		if (null != responseString) {
			if (titleTv != null) {
				titleTv.setText(responseString);
			}
			if (marker != null) {
				marker.setTitle(responseString);
			}
			return;
		}
		
		// 2.无缓存则发起GET请求
		V5HttpUtil.get(url, new HttpResponseHandler(titleTv.getContext()) {
			
			@Override
			public void onSuccess(int statusCode, String responseString) {
				JSONObject json;
				try {
					json = new JSONObject(responseString);
					final String desc = json.getJSONObject("result").getString("address");
					urlCache.put(url, desc);
					titleTv.post(new Runnable() {
						
						@Override
						public void run() {
							if (titleTv != null) {
								titleTv.setText(desc);
							}
							
							if (marker != null) {
								marker.setTitle(desc);
							}
						}
					});
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFailure(int statusCode, String responseString) {
				Logger.e("MapUtil", "[getLocationTitle] -> HttpFailure("+statusCode+"): " + responseString);
			}
		});
	}
	
//	public static void getLocationTitle(final double x, final double y, final TextView titleTv, final Marker marker) {
//		final URLCache urlCache = new URLCache();
//		String responseString = urlCache.get(GeocoderHttpClient.getUrl(x, y));
//		if (null != responseString) {
//			if (titleTv != null) {
//				titleTv.setText(responseString);
//			}
//			if (marker != null) {
//				marker.setTitle(responseString);
//			}
//			return;
//		}
//		GeocoderHttpClient.get(x, y, null, new TextHttpResponseHandler() {
//			
//			@Override
//			public void onSuccess(int statusCode, Header[] headers,
//					String responseString) {
//				// Auto-generated method stub
//				JSONObject json;
//				try {
//					json = new JSONObject(responseString);
//					String desc = json.getJSONObject("result").getString("address");
//					urlCache.put(GeocoderHttpClient.getUrl(x, y), desc);
//					if (titleTv != null) {
//						titleTv.setText(desc);
//					}
//					
//					if (marker != null) {
//						marker.setTitle(desc);
//					}
//				} catch (JSONException e) {
//					// Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			
//			@Override
//			public void onFailure(int statusCode, Header[] headers,
//					String responseString, Throwable throwable) {
//				// Auto-generated method stub					
//			}
//		});		
//	}
	
	public static void getLocationTitle(double x, double y, final TextView titleTv) {
		getLocationTitle(x, y, titleTv, null);
	}
}
