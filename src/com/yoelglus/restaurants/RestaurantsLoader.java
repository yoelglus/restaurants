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

/**
 * Loads the restaurants list from the places API in a background thread.
 * Use the shared preferences as the persistant memory for the offline mode.
 * @author Yoel Gluschnaider
 *
 */
public class RestaurantsLoader extends AsyncTaskLoader<List<Restaurant>> {

	// The status value of a valid response from the places API.
	private static final String STATUS_OK = "OK";

	// name of the restaurants list shared preferences.
	private static final String SHARED_PREF_NAME = "restaurants";

	// The response from places API key in the shared preferences
	private static final String RESPONSE_SP_KEY = "response";

	// The key of the places API key meta data element.
	private static final String META_DATA_KEY = "com.yoelglus.restaurants.places.API_KEY";	
	
	// JSON response from Google Places properties keys
	private static final String STATUS_KEY = "status";
	private static final String RESTAURANTS_LIST_KEY = "results";
	private static final String LONGITUDE_KEY = "lng";
	private static final String LATITUDE_KEY = "lat";
	private static final String LOCATION_KEY = "location";
	private static final String GEOMETRY_KEY = "geometry";
	private static final String VICINITY_KEY = "vicinity";
	private static final String NAME_KEY = "name";
	
	// the type of places you want to use
	private static final String PLACES_TYPE = "restaurant";
	
	// The current location (used to set the location in the request from the places API).
	private double mLatitude;
	private double mLongitude;

	// Read the API key from the app's manifest.
	private String mApiKey;


	public RestaurantsLoader(Context context, double lat, double lng) {
		super(context);
		mLatitude = lat;
		mLongitude = lng;
		// extract the API key from the app's meta data.
		ApplicationInfo appInfo;
		try {
			appInfo = context.getPackageManager().getApplicationInfo(
					context.getPackageName(), PackageManager.GET_META_DATA);
			mApiKey = appInfo.metaData.getString(META_DATA_KEY);
		} catch (NameNotFoundException e) {
			// do nothing.
		}
	}

	@Override
	public List<Restaurant> loadInBackground() {

		// Retrieve the data from the places API
		List<Restaurant> resturantsList = null;

		// get the list from the places API
		HttpClient httpClient = new DefaultHttpClient();

		// Build the URL for the request from the places API.
		String url = String
				.format(Locale.getDefault(),
						"https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=%d&types=%s&sensor=false&key=%s",
						mLatitude, 
						mLongitude, 
						Constants.SEARCH_RADIUS_METERS,
						PLACES_TYPE,
						mApiKey);

		// Request the data from the places API.
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
				.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

		// Validate the result
		if (responseJson != null
				&& STATUS_OK.equals(responseJson.optString(STATUS_KEY))) {
			// if succeeded, save the data to the shared preferences.
			Editor editor = sharedPreferences.edit();
			editor.putString(RESPONSE_SP_KEY, response);
			editor.commit();
		}
		// if failed, try to get the latest data from the share preferences.
		else {
			response = sharedPreferences.getString(RESPONSE_SP_KEY, null);
			if (response != null) {
				try {
					responseJson = new JSONObject(response);
				} catch (JSONException e1) {
				}
			}
		}
		
		// parse the list.
		resturantsList = parseResponse(responseJson);

		return resturantsList;
	}

	/**
	 * Parses the JSON response from Google Places and returns the restaurants list
	 * Returns null if list is invalid
	 * @param responseJson - the JSON from the google places API.
	 * @return the restaurants list or null if the list is invalid. 
	 */
	private List<Restaurant> parseResponse(JSONObject responseJson) {
		if (responseJson == null) {
			return null;
		}
		List<Restaurant> resturantsList;
		// Parse the list
		resturantsList = new ArrayList<Restaurant>();
		// Get the JSON list as an array.
		JSONArray results = responseJson.optJSONArray(RESTAURANTS_LIST_KEY);
		int resultsLen = results.length();
		JSONObject result = null;
		for (int i = 0; i < resultsLen; i++) {
			result = results.optJSONObject(i);
			if (result != null) {
				String name = result.optString(NAME_KEY);
				String vicinity = result.optString(VICINITY_KEY);
				double latitude = 0;
				double longitude = 0;

				// parse the location
				JSONObject geometry = result.optJSONObject(GEOMETRY_KEY);
				if (geometry != null) {
					JSONObject location = geometry.optJSONObject(LOCATION_KEY);
					if (location != null) {
						latitude = location.optDouble(LATITUDE_KEY, 0);
						longitude = location.optDouble(LONGITUDE_KEY, 0);
					}
				}
				// add the restaurant to the list.
				resturantsList.add(new Restaurant(name, vicinity, latitude, longitude));
			}
		}
		return resturantsList;
	}

}
