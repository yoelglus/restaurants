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
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yoelglus.restaurants.R.id;
import com.yoelglus.restaurants.R.layout;
import com.yoelglus.restaurants.R.string;

/**
 * The main activity of the app. Contains two tabs with two fragments:
 * ListFragment and MapsFragment. There is no ViewPager in this case since
 * panning the map might cause unwanted swiping between screens.
 * 
 * @author Yoel Gluschnaider
 * 
 */
public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener, GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		LoaderCallbacks<List<Restaurant>>, LocationListener {

	// Loader initialization arguments (the current location of the device).
	private static final String LOADER_ARG_LONGITUDE = "Lng";
	private static final String LOADER_ARG_LATITUDE = "Lat";

	// Location request parameters
	private static final int INTERVAL_CEILING_MILISEC = 1000;
	private static final int UPDATE_INTERVAL_MILISEC = 5000;

	// The ID of the request for resolving issues when trying to connect to the
	// Google Services.
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	// Location client used to communicate with Google Services.
	private LocationClient mLocationClient;

	// Pointers to the two screens of the app.
	private RestaurantsListFragment mListFragment;
	private SupportMapFragment mMapFragment;

	// position of the two screens
	private static final int LIST_SCREEN_POSITION = 0;
	private static final int MAP_SCREEN_POSITION = 1;

	// The last known location.
	private Location mLocation;

	// The list of restaurants
	private List<Restaurant> mRestaurantsList;

	// A request to connect to Location Services
	private LocationRequest mLocationRequest;
	
	private MenuItem mRefreshMenuItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create a new global location parameters object
		mLocationRequest = LocationRequest.create();

		// Set the update interval
		mLocationRequest.setInterval(UPDATE_INTERVAL_MILISEC);

		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		// Set the interval ceiling to one second
		mLocationRequest.setFastestInterval(INTERVAL_CEILING_MILISEC);

		// add the tabs
		actionBar.addTab(actionBar.newTab()
				.setText(getString(R.string.title_list_screen))
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab()
				.setText(getString(R.string.title_map_screen))
				.setTabListener(this));

		// create the location client to get the device location
		mLocationClient = new LocationClient(this, this, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		mRefreshMenuItem = menu.findItem(id.action_refresh);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			android.app.FragmentTransaction fragmentTransaction) {
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		switch (tab.getPosition()) {
		case LIST_SCREEN_POSITION:
			// if the list fragment was not created, create it.
			// Otherwise attach it back.
			if (mListFragment == null) {
				mListFragment = new RestaurantsListFragment();
				transaction.add(android.R.id.content, mListFragment,
						"ListFragment");
			} else {
				transaction.attach(mListFragment);
			}
			break;
		case MAP_SCREEN_POSITION:
			// if the maps fragment was not created, create it.
			// Otherwise attach it back.
			if (mMapFragment == null) {
				mMapFragment = SupportMapFragment.newInstance();
				transaction.add(android.R.id.content, mMapFragment,
						"MapFragment");
				transaction.commit();
				// Do the transaction immediately so that we can add markers.
				// This is to avoid null map object retreived from the map
				// fragment.
				getSupportFragmentManager().executePendingTransactions();
				// Set the map markers, position and zoom level.
				setMapData();
				// Return to avoid double commit of the transaction.
				return;
			} else {
				transaction.attach(mMapFragment);
			}
			break;
		}
		transaction.commit();
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			android.app.FragmentTransaction fragmentTransaction) {
		// Detach the unselected tab. Use the support fragment manager.
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
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
	public void onTabReselected(ActionBar.Tab tab,
			android.app.FragmentTransaction fragmentTransaction) {
		// Do nothing.
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case CONNECTION_FAILURE_RESOLUTION_REQUEST:
			// If we resolved the connection failure to the Google Services, try
			// to reconnect.
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
		// connect to the Location Services.
		mLocationClient.connect();
	}

	@Override
	protected void onStop() {
		// If the client is connected, send stop update request to Location
		// Services
		if (mLocationClient.isConnected()) {
			mLocationClient.removeLocationUpdates(this);
		}
		// disconnect from the Location Services.
		mLocationClient.disconnect();
		super.onStop();
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// If the Google Play Services can resolve this issue, send an Intent to
		// show a resolution activity.
		if (result.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				result.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
				// Thrown if Google Play services canceled the original
				// PendingIntent
			} catch (IntentSender.SendIntentException e) {
				// No connection to google play services. Can't get location.
			}
		} else {
			// No connection to google play services. Can't get location.
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {

		// Sends an update request to Location Services
		mLocationClient.requestLocationUpdates(mLocationRequest, this);

		// get the current location and initialize the loader
		Bundle args = new Bundle();
		mLocation = mLocationClient.getLastLocation();
		if (mLocation != null) {
			args.putDouble(LOADER_ARG_LATITUDE, mLocation.getLatitude());
			args.putDouble(LOADER_ARG_LONGITUDE, mLocation.getLongitude());
		}
		getSupportLoaderManager().initLoader(0, args, this);
	}

	@Override
	public void onDisconnected() {
		// Do nothing
	}

	@Override
	public Loader<List<Restaurant>> onCreateLoader(int id, Bundle args) {
		Loader<List<Restaurant>> loader = new RestaurantsLoader(this,
				args.getDouble(LOADER_ARG_LATITUDE), args.getDouble(LOADER_ARG_LONGITUDE));
		// force the loading.
		loader.forceLoad();
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<List<Restaurant>> loader,
			List<Restaurant> data) {
		mRestaurantsList = data;
		// remove the loading indicator
		mRefreshMenuItem.setActionView(null);
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
			// first clear all current markers
			map.clear();
			for (Restaurant restaurant : mRestaurantsList) {
				map.addMarker(new MarkerOptions()
						.position(
								new LatLng(restaurant.getLatitude(), restaurant
										.getLongitude()))
						.title(restaurant.getName())
						.snippet(restaurant.getVicinity()));
			}
			
			LatLng curLocation = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
			
			// add the current location marker
			map.addMarker(new MarkerOptions()
						.position(curLocation)
						.title(getString(string.my_location_marker_title))
						.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
					curLocation, calculateZoomLevel());
			map.animateCamera(cameraUpdate);
		}
	}

	@Override
	public void onLoaderReset(Loader<List<Restaurant>> loader) {

	}

	/**
	 * Calculate the zoom level according to the required width of the screen (2 miles).
	 * @return The zoom level to show the map.
	 */
	private int calculateZoomLevel() {
		DisplayMetrics dispalayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dispalayMetrics);
		double equatorLength = 6378140; // in meters
		// get the shorter size (in landscape it is the height).
		double widthInPixels = Math.min(dispalayMetrics.widthPixels, dispalayMetrics.heightPixels);
		double metersPerPixel = equatorLength / 256;
		int zoomLevel = 1;
		int targetWidthInMeters = 2 * Constants.SEARCH_RADIUS_METERS;
		while ((metersPerPixel * widthInPixels) > targetWidthInMeters) {
			metersPerPixel /= 2;
			++zoomLevel;
		}
		return zoomLevel;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == id.action_refresh) {
			// aquire the new location and restart the loader to get the data from the Google Places API.
			Bundle args = new Bundle();
			mLocation = mLocationClient.getLastLocation();
			if (mLocation != null) {
				args.putDouble(LOADER_ARG_LATITUDE, mLocation.getLatitude());
				args.putDouble(LOADER_ARG_LONGITUDE, mLocation.getLongitude());
			}
			// show loading indicator
			mRefreshMenuItem.setActionView(layout.loading_indicator);
			getSupportLoaderManager().restartLoader(0, args, this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onLocationChanged(Location location) {
		// do nothing, we get the location in each refresh operation.
	}

}
