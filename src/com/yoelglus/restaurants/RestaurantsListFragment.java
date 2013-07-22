package com.yoelglus.restaurants;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.yoelglus.restaurants.R.id;
import com.yoelglus.restaurants.R.layout;

/**
 * The restaurants list (first screen of the app).
 * @author Yoel Gluschnaider
 *
 */
public class RestaurantsListFragment extends ListFragment {
	
	// The restaurants adapter.
	private ArrayAdapter<Restaurant> mAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// create and set the adapter.
		mAdapter = new RestaurantsAdapter(getActivity(), android.R.id.text1);
		setListAdapter(mAdapter);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(layout.fragment_list, container, false);
	}
	
	private class RestaurantsAdapter extends ArrayAdapter<Restaurant> {

		public RestaurantsAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Use the ViewHolder pattern.
			RestaurantViewHolder viewHolder;
			if (convertView == null) {
				// create a new item view.
				LayoutInflater inflater = LayoutInflater.from(getActivity());
				convertView = inflater.inflate(android.R.layout.two_line_list_item, parent, false);
				viewHolder = new RestaurantViewHolder();
				viewHolder.text1 = (TextView) convertView.findViewById(android.R.id.text1);
				viewHolder.text2 = (TextView) convertView.findViewById(android.R.id.text2);
				convertView.setTag(id.action_refresh, viewHolder);
			}
			// reuse existing view.
			else {
				viewHolder = (RestaurantViewHolder) convertView.getTag(id.action_refresh);
			}
			
			// set the name and vicinity.
			Restaurant restaurant = getItem(position);
			viewHolder.text1.setText(restaurant.getName());
			viewHolder.text2.setText(restaurant.getVicinity());
			
			return convertView;
		}
		

	}
	
	// The view holder pattern.
	private static class RestaurantViewHolder {
		private TextView text1;
		private TextView text2;
	}
	
	/**
	 * Set the restaurants list the be the new one.
	 * @param restaurantsList
	 */
	public void setRestaurantsList(List<Restaurant> restaurantsList) {
		// first clear the list and then if the list is not null, add it to the adapter.
		mAdapter.clear();
		if (restaurantsList != null) {
			mAdapter.addAll(restaurantsList);
			mAdapter.notifyDataSetChanged();
		}
	}
}
