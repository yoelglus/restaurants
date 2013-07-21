package com.yoelglus.restaurants;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.Menu;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.yoelglus.restaurantes.R;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener, 
																GooglePlayServicesClient.ConnectionCallbacks,
																GooglePlayServicesClient.OnConnectionFailedListener,
																LoaderCallbacks<List<Restaurant>> {
	
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
	private LocationClient mLocationClient;
	
	private RestaurantsListFragment mListFragment;
	
	// The pager adapter that will return each screen fragment.
	private ScreensPagerAdapter mScreensPagerAdapter;

	// Will host the two screens.
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the two screens
        // of the app: list and map.
        mScreensPagerAdapter = new ScreensPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the screens adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mScreensPagerAdapter);

        // When swiping between different screens, select the corresponding screen
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the screens in the app, add a tab to the action bar.
        for (int i = 0; i < mScreensPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the screen title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mScreensPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
        
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
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding screen in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the screens.
     */
    public class ScreensPagerAdapter extends FragmentPagerAdapter {
    	
    	private static final int LIST_SCREEN_POSITION = 0;
    	private static final int MAP_SCREEN_POSITION = 1;
    	
    	private static final int SCREENS_COUNT = 2;
    	
        public ScreensPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
        	switch (position) {
			case LIST_SCREEN_POSITION:
	        	mListFragment = new RestaurantsListFragment();
	        	return mListFragment;
			case MAP_SCREEN_POSITION:
				return new RestaurantsListFragment();
			default:
				return null;
			}
        }

        @Override
        public int getCount() {
            return SCREENS_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case LIST_SCREEN_POSITION:
                    return getString(R.string.title_list_screen);
                case MAP_SCREEN_POSITION:
                    return getString(R.string.title_map_screen);
            }
            return null;
        }
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
		Location location = mLocationClient.getLastLocation();
		if (location != null) {
			args.putDouble("Lat", location.getLatitude());
			args.putDouble("Lng", location.getLongitude());
		}
		getSupportLoaderManager().initLoader(0, args, this);
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Loader<List<Restaurant>> onCreateLoader(int id, Bundle args) {
		Loader<List<Restaurant>> loader = new RestaurantsLoader(this, args.getDouble("Lat"), args.getDouble("Lng"));
		loader.forceLoad();
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<List<Restaurant>> loader,
			List<Restaurant> data) {
		if (mListFragment != null) {
			mListFragment.setRestaurantsList(data);
		}
		
	}

	@Override
	public void onLoaderReset(Loader<List<Restaurant>> arg0) {
		// TODO Auto-generated method stub
		
	}
	

}
