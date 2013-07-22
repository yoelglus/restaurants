package com.yoelglus.restaurants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v4.content.AsyncTaskLoader;

public class RestaurantsLoader extends AsyncTaskLoader<List<Restaurant>> {

	private double mLatitude;
	private double mLongitude;

	private String mApiKey;
	
	public RestaurantsLoader(Context context, double lat, double lng) {
		super(context);
		mLatitude = lat;
		mLongitude = lng;
		// extract the API key from the app's meta data.
		ApplicationInfo appInfo;
		try {
			appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			mApiKey = appInfo.metaData.getString("com.yoelglus.restaurants.places.API_KEY");
		} catch (NameNotFoundException e) {
			// do nothing.
		}
	}

	// The search radius in meters (one mile).
	private static final int SEARCH_RADIUS_METERS = 1609;

	@Override
	public List<Restaurant> loadInBackground() {

		// Retrieve the data from the places API
		List<Restaurant> resturantsList = null;

		// get the list from the places API
		HttpClient httpClient = new DefaultHttpClient();

		String url = String
				.format(Locale.getDefault(),
						"https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=%d&types=bus_station&sensor=false&key=%s",
						mLatitude, 
						mLongitude, 
						SEARCH_RADIUS_METERS,
						mApiKey);

		HttpGet httpGet = new HttpGet(url);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String response = null;
		try {
			response = httpClient.execute(httpGet, responseHandler);
		} catch (ClientProtocolException e1) {
		} catch (IOException e1) {
		}

		// Create a JSONObject from the body of the response
		JSONObject responseJson = null;
		if (response != null) {
			try {
				responseJson = new JSONObject(response);
			} catch (JSONException e1) {
			}
		}

		SharedPreferences sharedPreferences = getContext()
				.getSharedPreferences("restaurants", Context.MODE_PRIVATE);

		// Validate the result
		if (responseJson != null
				&& "OK".equals(responseJson.optString("status"))) {
			// if succeeded, save the data to the shared preferences.
			Editor editor = sharedPreferences.edit();
			editor.putString("response", response);
			editor.commit();
		}
		// if failed, try to get the latest data from the share preferences.
		else {
			response = sharedPreferences.getString("response", null);
			if (response != null) {
				try {
					responseJson = new JSONObject(response);
				} catch (JSONException e1) {
				}
			}
		}
		resturantsList = parseResponse(responseJson);

		return resturantsList;
	}

	private List<Restaurant> parseResponse(JSONObject responseJson) {
		if (responseJson == null) {
			return null;
		}
		List<Restaurant> resturantsList;
		// Parse the list
		resturantsList = new ArrayList<Restaurant>();
		JSONArray results = responseJson.optJSONArray("results");
		int resultsLen = results.length();
		JSONObject result = null;
		for (int i = 0; i < resultsLen; i++) {
			result = results.optJSONObject(i);
			if (result != null) {
				String name = result.optString("name");
				String vicinity = result.optString("vicinity");
				double latitude = 0;
				double longitude = 0;

				// parse the location
				JSONObject geometry = result.optJSONObject("geometry");
				if (geometry != null) {
					JSONObject location = geometry.optJSONObject("location");
					if (location != null) {
						latitude = location.optDouble("lat", 0);
						longitude = location.optDouble("lng", 0);
					}
				}
				// add the restaurant to the list.
				resturantsList.add(new Restaurant(name, vicinity, latitude,
						longitude));
			}
		}
		return resturantsList;
	}

}
