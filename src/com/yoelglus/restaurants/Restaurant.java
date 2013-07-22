package com.yoelglus.restaurants;

/**
 * A class representing a restaurant location.
 * @author Yoel Gluschnaider
 *
 */
public class Restaurant {
	// The restaurant name
	private String mName;
	
	// The restaurant address (called vicinity in places)
	private String mVicinity;
	
	// The location of the restaurant.
	private double mLatitude;
	private double mLongitude;
	
	public Restaurant(String name, String vicinity, double latitude, double longitude) {
		mName = name;
		mVicinity = vicinity;
		mLatitude = latitude;
		mLongitude = longitude;
	}
	
	public String getName() {
		return mName;
	}
	
	public String getVicinity() {
		return mVicinity;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public double getLongitude() {
		return mLongitude;
	}
	
}
