package com.yoelglus.restaurants;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.yoelglus.restaurantes.R.id;
import com.yoelglus.restaurantes.R.layout;

public class RestaurantsListFragment extends ListFragment {
	
	private ArrayAdapter<Restaurant> mAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new RestaurantsAdapter(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, new ArrayList<Restaurant>());
		setListAdapter(mAdapter);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(layout.fragment_list, container, false);
		return root;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	private class RestaurantsAdapter extends ArrayAdapter<Restaurant> {

		public RestaurantsAdapter(Context context, int resource,
				int textViewResourceId, List<Restaurant> objects) {
			super(context, resource, textViewResourceId, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			RestaurantViewHolder viewHolder;
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(getActivity());
				convertView = inflater.inflate(android.R.layout.two_line_list_item, parent, false);
				viewHolder = new RestaurantViewHolder();
				viewHolder.text1 = (TextView) convertView.findViewById(android.R.id.text1);
				viewHolder.text2 = (TextView) convertView.findViewById(android.R.id.text2);
				convertView.setTag(id.action_refresh, viewHolder);
			}
			else {
				viewHolder = (RestaurantViewHolder) convertView.getTag(id.action_refresh);
			}
			
			Restaurant restaurant = getItem(position);
			viewHolder.text1.setText(restaurant.getName());
			viewHolder.text2.setText(restaurant.getVicinity());
			
			return convertView;
		}
		

	}
	
	private static class RestaurantViewHolder {
		private TextView text1;
		private TextView text2;
	}
	
	public void setRestaurantsList(List<Restaurant> restaurantsList) {
		mAdapter.clear();
		if (restaurantsList != null) {
			mAdapter.addAll(restaurantsList);
			mAdapter.notifyDataSetChanged();
		}
	}
}
