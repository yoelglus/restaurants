package com.yoelglus.restaurants;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.view.Menu;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener, 
																GooglePlayServicesClient.ConnectionCallbacks,
																GooglePlayServicesClient.OnConnectionFailedListener,
																LoaderCallbacks<List<Restaurant>> {
	
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
	private LocationClient mLocationClient;
	
	private RestaurantsListFragment mListFragment;
	private SupportMapFragment mMapFragment;
	
	private static final int LIST_SCREEN_POSITION = 0;
	private static final int MAP_SCREEN_POSITION = 1;
	
	private int mSelectedTab = 0;

	private Location mLocation;

	private List<Restaurant> mRestaurantsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.addTab(
                actionBar.newTab()
                        .setText(getString(R.string.title_list_screen))
                        .setTabListener(this));
        actionBar.addTab(
                actionBar.newTab()
                        .setText(getString(R.string.title_map_screen))
                        .setTabListener(this));
        
        // create the location client to get the device location
        mLocationClient = new LocationClient(this, this, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
    	FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    	mSelectedTab = tab.getPosition();
    	switch (mSelectedTab) {
		case LIST_SCREEN_POSITION:
			if (mListFragment == null) {
				mListFragment = new RestaurantsListFragment();
				transaction.add(android.R.id.content, mListFragment, "ListFragment");
			}
			else {
				transaction.attach(mListFragment);
			}
			break;
		case MAP_SCREEN_POSITION:
			if (mMapFragment == null) {
				mMapFragment = SupportMapFragment.newInstance();
				transaction.add(android.R.id.content, mMapFragment, "MapFragment");
				transaction.commit();
				getSupportFragmentManager().executePendingTransactions();
				setMapData();
				return;
			}
			else {
				transaction.attach(mMapFragment);
			}
			break;
    	}
    	transaction.commit();
    }

	@Override
    public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    	switch (tab.getPosition()) {
		case LIST_SCREEN_POSITION:
			if (mListFragment != null) {
				transaction.detach(mListFragment);
			}
			break;
		case MAP_SCREEN_POSITION:
			if (mMapFragment != null) {
				transaction.detach(mMapFragment);
			}
			break;
    	}
    	transaction.commit();
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode) {
		case CONNECTION_FAILURE_RESOLUTION_REQUEST:
			if (resultCode == Activity.RESULT_OK) {
				mLocationClient.connect();
			}
			break;

		default:
	    	super.onActivityResult(requestCode, resultCode, data);
			break;
		}
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	mLocationClient.connect();
    }
    
    @Override
    protected void onStop() {
    	mLocationClient.disconnect();
    	super.onStop();
    }

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		/*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (result.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
            	result.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
            	// No connection to google play services. Can't get location.
            }
        } else {
            // No connection to google play services. Can't get location.
        }
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// get the current location and initialize the loader
		Bundle args = new Bundle();
		mLocation = mLocationClient.getLastLocation();
		if (mLocation != null) {
			args.putDouble("Lat", mLocation.getLatitude());
			args.putDouble("Lng", mLocation.getLongitude());
		}
		getSupportLoaderManager().initLoader(0, args, this);
	}

	@Override
	public void onDisconnected() {}

	@Override
	public Loader<List<Restaurant>> onCreateLoader(int id, Bundle args) {
		Loader<List<Restaurant>> loader = new RestaurantsLoader(this, args.getDouble("Lat"), args.getDouble("Lng"));
		loader.forceLoad();
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<List<Restaurant>> loader,
			List<Restaurant> data) {
		mRestaurantsList = data;
		if (mListFragment != null) {
			mListFragment.setRestaurantsList(mRestaurantsList);
		}
		
		if (mMapFragment != null) {
			setMapData();
		}
		
	}

	private void setMapData() {
		if (mRestaurantsList != null) {
			GoogleMap map = mMapFragment.getMap();
			for (Restaurant restaurant : mRestaurantsList) {
				map.addMarker(
						new MarkerOptions()
							.position(new LatLng(restaurant.getLatitude(), restaurant.getLongitude()))
							.title(restaurant.getName()).snippet(restaurant.getVicinity()));
			}
			
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
					new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), calculateZoomLevel());
			map.animateCamera(cameraUpdate);
		}
	}

	@Override
	public void onLoaderReset(Loader<List<Restaurant>> arg0) {
		
	}
	
	private int calculateZoomLevel() {
		DisplayMetrics dispalayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dispalayMetrics);
	    double equatorLength = 6378140; // in meters
	    double widthInPixels = dispalayMetrics.widthPixels;
	    double metersPerPixel = equatorLength / 256;
	    int zoomLevel = 1;
	    while ((metersPerPixel * widthInPixels) > 3200) {
	        metersPerPixel /= 2;
	        ++zoomLevel;
	    }
	    return zoomLevel;
	}
	

}
