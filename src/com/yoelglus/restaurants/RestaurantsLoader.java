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
import android.support.v4.content.AsyncTaskLoader;

public class RestaurantsLoader extends AsyncTaskLoader<List<Restaurant>> {
	
	private double mLatitude;
	private double mLongitude;
	
	public RestaurantsLoader(Context context, double lat, double lng) {
		super(context);
		mLatitude = lat;
		mLongitude = lng;
	}

	// The google places API key.
	private static final String PLACES_API_KEY = "AIzaSyCnEBO1GoJH_7znZUHBS11JPqAW1H-y_40";
	
	// The search radius in meters (one mile).
	private static final int SEARCH_RADIUS_METERS = 1609;
	

	@Override
	public List<Restaurant> loadInBackground() {
		
		// Get current location
		// Retrieve the data from the places API
		List<Restaurant> resturantsList = getListFromPlaces(mLatitude,mLongitude);
		
		// if succeeded, save the data to the shared preferences.
		// if failed, try to get the latest data from the share preferences.

		return resturantsList;
	}

	private List<Restaurant> getListFromPlaces(double curLocationLat, double curLocationLng) {
		
		// get the list from the places API
		HttpClient httpClient = new DefaultHttpClient();
		
		String url = String.format(
				Locale.getDefault(),
				"https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=%d&types=food&sensor=false&key=%s", 
				curLocationLat, 
				curLocationLng, 
				SEARCH_RADIUS_METERS, 
				PLACES_API_KEY);
		
		HttpGet httpGet = new HttpGet(url);
		// httpGet.addHeader("Content-type","application/json");
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String response = null;
		try {
			 response = httpClient.execute(httpGet, responseHandler);
		} catch (ClientProtocolException e1) {
			return null;
		} catch (IOException e1) {
			return null;
		}
		
		// Create a JSONObject from the body of the response
		JSONObject responseJson = null;
		try {
			responseJson = new JSONObject(response);
		} catch (JSONException e1) {
			return null;
		}
		
		// Validate the result
		if ("OK".equals(responseJson.optString("status"))) {
			// Parse the list
			List<Restaurant> restaurants = new ArrayList<Restaurant>();
			JSONArray results = responseJson.optJSONArray("results");
			int resultsLen = results.length();
			JSONObject result = null;
			for (int i = 0; i < resultsLen; i++) {
				result = results.optJSONObject(i);
				if (result != null) {
					String name = result.optString("name");
					String vicinity = result.optString("vicinity");
					long latitude = 0;
					long longitude = 0;
					
					// parse the location
					JSONObject geometry = result.optJSONObject("geometry");
					if (geometry != null) {
						JSONObject location = geometry.optJSONObject("location");
						if (location != null) {
							latitude = location.optLong("lat", 0);
							longitude = location.optLong("lng", 0);
						}
					}
					// add the restaurant to the list.
					restaurants.add(new Restaurant(name, vicinity, latitude, longitude));
				}
			}
			return restaurants;
		}
		
		return null;
	}

}
