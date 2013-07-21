package com.yoelglus.restaurants;

public class Restaurant {
	private String mName;
	private String mVicinity;
	private long mLatitude;
	private long mLongitude;
	
	public Restaurant(String name, String vicinity, long latitude, long longitude) {
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

	public long getLatitude() {
		return mLatitude;
	}

	public long getLongitude() {
		return mLongitude;
	}
	
}
