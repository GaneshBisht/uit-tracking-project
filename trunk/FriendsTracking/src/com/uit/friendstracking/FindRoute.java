package com.uit.friendstracking;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import model.UserInfo;

import com.google.android.gms.internal.s;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.uit.friendstracking.models.KPosition;
import com.uit.friendstracking.models.KUserInfo;
import com.uit.friendstracking.tasks.GetFriendsAsyncTask;
import com.uit.friendstracking.tasks.GetPositionAsyncTask;
import com.uit.friendstracking.tasks.SearchAsyncTask;
import com.uit.friendstracking.webservices.ToServer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FindRoute extends Activity implements OnClickListener {

	Spinner ToFriend;
	Button btnSearch, btOK;
	List<KUserInfo> listUsers;
	String[] listItems;
	int[] listIds;
	ArrayList<Friend> arrFriend;

	private class Friend {
		/*
		 * Normally you would create some getter and setter-methods. But in
		 * Android, using public fields is better for memory proposals.
		 */
		public String name;
		public String locations;

		public Friend(String name, String location) {
			this.name = name;
			this.locations = location;
		}
	}

	private class MyAdapter implements SpinnerAdapter {

		/**
		 * The internal data (the ArrayList with the Objects).
		 */
		ArrayList<Friend> data;

		public MyAdapter(ArrayList<Friend> data) {
			this.data = data;
		}

		/**
		 * Returns the Size of the ArrayList
		 */
		@Override
		public int getCount() {
			return data.size();
		}

		/**
		 * Returns one Element of the ArrayList at the specified position.
		 */
		@Override
		public Object getItem(int position) {
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getItemViewType(int position) {
			return android.R.layout.simple_spinner_dropdown_item;
		}

		/**
		 * Returns the View that is shown when a element was selected.
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView v = new TextView(getApplicationContext());
			v.setTextColor(Color.BLACK);
			v.setText(data.get(position).name);
			return v;
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public void registerDataSetObserver(DataSetObserver observer) {
			// TODO Auto-generated method stub

		}

		@Override
		public void unregisterDataSetObserver(DataSetObserver observer) {
			// TODO Auto-generated method stub

		}

		/**
		 * The Views which are shown in when the arrow is clicked (In this case,
		 * I used the same as for the "getView"-method.
		 */
		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			return this.getView(position, convertView, parent);
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.find_route);
		
		ToFriend = (Spinner) findViewById(R.id.spTo);

		btnSearch = (Button) findViewById(R.id.find_btnSearch);
		btnSearch.setOnClickListener(this);

		btOK = (Button) findViewById(R.id.btnOk);
		btOK.setOnClickListener(this);

		arrFriend = new ArrayList<FindRoute.Friend>();
		getListFriend();
		MyAdapter adapter = new MyAdapter(arrFriend);
		// apply the Adapter:
		ToFriend.setAdapter(adapter);
	}

	public void getListFriend() {
		try {
			List<KUserInfo> lstKUserInfo = new GetFriendsAsyncTask().execute()
					.get();
			for (KUserInfo user : lstKUserInfo) {
				List<KPosition> positions = new GetPositionAsyncTask(
						user.getId()).execute().get();
				if (positions != null && positions.size() != 0) {
					String tempName = user.getNick();
					String tempLocation = positions.get(0).getLatitude()
							.toString()
							+ "," + positions.get(0).getLongitude().toString();

					Friend tempFriend = new Friend(tempName, tempLocation);
					arrFriend.add(tempFriend);
				}

			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		LocationManager locationManager;
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);

		String provider = locationManager.getBestProvider(criteria, true);

		Location location = locationManager.getLastKnownLocation(provider);
		// TODO Auto-generated method stub
		Button sourceButton = (Button) v;

		if (sourceButton == btOK) {
			int indexSelected = ToFriend.getSelectedItemPosition();
			final Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://maps.google.com/maps?" + "saddr="
							+ location.getLatitude() + "," + location.getLongitude() + "&daddr="
							+ arrFriend.get(indexSelected).locations));
			intent.setClassName("com.google.android.apps.maps",
					"com.google.android.maps.MapsActivity");
			startActivity(intent);
		}
	}

}
