package com.uit.friendstracking;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.uit.friendstracking.models.KPosition;
import com.uit.friendstracking.models.KUserInfo;
import com.uit.friendstracking.tasks.GetFriendsAsyncTask;
import com.uit.friendstracking.tasks.GetPositionAsyncTask;
import com.uit.friendstracking.tasks.GetRequestsAsyncTask;
import com.uit.friendstracking.tasks.LogPositionAsyncTask;
import com.uit.friendstracking.webservices.ToServer;

public class Map extends FragmentActivity implements LocationListener {
	private static IntentFilter m_intentFilter;
	private GoogleMap m_map;
	protected LocationManager m_locationManager;
	private String m_provider;
	private List<Marker> m_listMarker = new ArrayList<Marker>();
	private long m_lastUpdateFriends = 0;
	private long m_lastUpdateMine = 0;
	private final static int DELTA_TIME = 10000;

	static {
		m_intentFilter = new IntentFilter();
		m_intentFilter.addAction(Intent.ACTION_TIME_TICK);
		m_intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		m_intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
	}

	private final BroadcastReceiver m_timeChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action.equals(Intent.ACTION_TIME_TICK)) {
				long currentTime = System.currentTimeMillis();
				long deltaTime = currentTime - m_lastUpdateFriends;
				if (deltaTime >= DELTA_TIME) {
					m_lastUpdateFriends = currentTime;
					for (Marker marker : m_listMarker) {
						marker.remove();
					}
					try {
						List<KUserInfo> users = new GetFriendsAsyncTask().execute().get();
						for (KUserInfo user : users) {
							List<KPosition> positions = new GetPositionAsyncTask(user.getId()).execute().get();
							if (positions != null && positions.size() != 0) {
								Marker marker = m_map.addMarker(new MarkerOptions()
										.position(new LatLng(positions.get(0).getLatitudeFloat(), positions.get(0).getLongitudeFloat())).title(user.getNick())
										.snippet(user.getPosition().toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher)));
								m_listMarker.add(marker);
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		m_locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		m_provider = m_locationManager.getBestProvider(criteria, true);
		//registerReceiver(m_timeChangedReceiver, m_intentFilter);

		setUpMapIfNeeded();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
		m_locationManager.requestLocationUpdates(m_provider, 500, 1, this);
		
		IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction(Intent.ACTION_TIME_TICK);
	    registerReceiver(m_timeChangedReceiver, intentFilter); 
	}

	private void setUpMapIfNeeded() {
		if (m_map == null) {
			m_map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
			if (m_map != null) {
				setUpMap();
			}
		}
	}

	protected void setUpCamera() {
		Location location = m_locationManager.getLastKnownLocation(m_provider);
		if (location != null) {
			double curLat = location.getLatitude();
			double curLng = location.getLongitude();
			LatLng curCoordinate = new LatLng(curLat, curLng);
			m_map.moveCamera(CameraUpdateFactory.newLatLngZoom(curCoordinate, 14));
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {

		case R.id.create_comment:
			return true;

		case R.id.take_photo:
			return true;

		case R.id.find_friend:
			Intent newIntent = new Intent(this, FindFriend.class);
			this.startActivityForResult(newIntent, 0);
			return true;

		case R.id.friend_requests:

			try {
				if (new GetRequestsAsyncTask(this).execute().get().size() > 0) {
					newIntent = new Intent(this, FriendRequest.class);
					this.startActivityForResult(newIntent, 0);
				} else {
					Toast.makeText(getApplicationContext(), "You do not have any friend's request.", Toast.LENGTH_LONG).show();
				}
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), "Fail while trying to access to friend's request: " + e.getMessage(), Toast.LENGTH_LONG).show();

			}

			return true;

		case R.id.modify_personal:
			return true;

		case R.id.exit:
			ToServer.logout();
			this.finish();
			return true;

		default:
			break;
		}

		return true;
	}

	private void setUpMap() {
		m_map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		m_map.setTrafficEnabled(true);
		m_map.setMyLocationEnabled(true);
		m_map.getUiSettings().setMyLocationButtonEnabled(true);
		setUpCamera();
	}

	@Override
	public void onLocationChanged(Location location) {
		long currentTime = System.currentTimeMillis();
		long deltaTime = currentTime - m_lastUpdateMine;
		if (deltaTime >= DELTA_TIME) {
			m_lastUpdateMine = currentTime;
			double curLat = location.getLatitude();
			double curLng = location.getLongitude();
			new LogPositionAsyncTask(Float.parseFloat("" + curLng), Float.parseFloat("" + curLat)).execute();
		}
	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	protected void onPause() {
		super.onPause();
		m_locationManager.removeUpdates(this);
		unregisterReceiver(m_timeChangedReceiver);
	}

}
