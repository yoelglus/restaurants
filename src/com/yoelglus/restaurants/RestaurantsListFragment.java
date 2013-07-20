package com.yoelglus.restaurants;

import java.util.ArrayList;
import java.util.List;

import com.yoelglus.restaurantes.R;
import com.yoelglus.restaurantes.R.id;
import com.yoelglus.restaurantes.R.layout;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RestaurantsListFragment extends ListFragment implements LoaderCallbacks<List<Restaurant>>{
	
	private ArrayAdapter<Restaurant> mAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(layout.fragment_list, container, false);
		return root;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new RestaurantsAdapter(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, new ArrayList<Restaurant>());
		setListAdapter(mAdapter);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<List<Restaurant>> onCreateLoader(int id, Bundle args) {
		Loader<List<Restaurant>> loader = new RestaurantsLoader(getActivity());
		loader.forceLoad();
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<List<Restaurant>> loader, List<Restaurant> data) {
		mAdapter.clear();
		mAdapter.addAll(data);
	}

	@Override
	public void onLoaderReset(Loader<List<Restaurant>> loader) {
		mAdapter.clear();
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
	
	
	
	
}
