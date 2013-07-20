package com.yoelglus.restaurants;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public class RestaurantsLoader extends AsyncTaskLoader<List<Restaurant>>{
	
	public RestaurantsLoader(Context context) {
		super(context);
	}

	// The google places API key.
	private static final String PLACES_API_KEY = "AIzaSyCnEBO1GoJH_7znZUHBS11JPqAW1H-y_40";
	
	// The search radius in meters (one mile).
	private static final int SEARCH_RADIUS_METERS = 1609;

	@Override
	public List<Restaurant> loadInBackground() {
		
		// get current location
		// retreive the data from the 
		
		List<Restaurant> dummyRestaurants = new ArrayList<Restaurant>();
		for (int i = 0; i < 10; i++) {
			dummyRestaurants.add(new Restaurant("Restaurant " + i, "vicinity " + i));
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dummyRestaurants;
	}

}
