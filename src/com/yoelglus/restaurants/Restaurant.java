package com.yoelglus.restaurants;

public class Restaurant {
	private String mName;
	private String mVicinity;
	
	public Restaurant(String name, String vicinity) {
		mName = name;
		mVicinity = vicinity;
	}
	
	public String getName() {
		return mName;
	}
	
	public String getVicinity() {
		return mVicinity;
	}
	
}
