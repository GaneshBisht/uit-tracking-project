package com.uit.friendstracking;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.uit.friendstracking.models.KPosition;
import com.uit.friendstracking.models.KUserInfo;
import com.uit.friendstracking.webservices.ToServer;

public class Map extends FragmentActivity implements LocationListener, OnTimeChangedListener {
	private static IntentFilter m_intentFilter;
	private GoogleMap m_map;
	protected LocationManager m_locationManager;
	private String m_provider;
	private Marker m_curMarker;
	private long m_lastUpdate = 0;

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
				long deltaTime = currentTime - m_lastUpdate;
				if (deltaTime >= 60000) {
					Location location = m_locationManager
							.getLastKnownLocation(m_provider);
					double curLat = location.getLatitude();
					double curLng = location.getLongitude();
					m_lastUpdate = currentTime;
					new LogPositionAsyncTask(Float.parseFloat("" + curLng),
							Float.parseFloat("" + curLat)).execute();
					try {
						List<KUserInfo> users = new GetFriendsAsyncTask()
								.execute().get();
						for (KUserInfo user : users) {
							List<KPosition> positions = new GetPositionAsyncTask(
									user.getId()).execute().get();
							if (positions != null && positions.size() != 0) {
								Marker marker = m_map
										.addMarker(new MarkerOptions()
												.position(
														new LatLng(
																positions
																		.get(0)
																		.getLatitudeFloat(),
																positions
																		.get(0)
																		.getLongitudeFloat()))
												.title(user.getNick())
												.snippet(user.getName())
												.icon(BitmapDescriptorFactory
														.fromResource(R.drawable.ic_launcher)));
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
		Criteria crit = new Criteria();
		crit.setAccuracy(Criteria.ACCURACY_FINE);
		m_provider = m_locationManager.getBestProvider(crit, true);
		registerReceiver(m_timeChangedReceiver, m_intentFilter);

		
		//Criteria criteria = new Criteria();
		//m_provider = "gps";// m_locationManager.getBestProvider(criteria,
							// false);
		setUpMapIfNeeded();
		showCurrentLocation();
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
		m_locationManager.requestLocationUpdates(m_provider, 400, 1, this);
	}

	private void setUpMapIfNeeded() {
		if (m_map == null) {
			m_map = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			if (m_map != null) {
				setUpMap();
				m_map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				m_map.setTrafficEnabled(true);
				m_map.setMyLocationEnabled(true);
				m_map.getUiSettings().setMyLocationButtonEnabled(true);
				setUpCamera();
			}
		}
		
	}

	protected void setUpCamera() {
		Location location = m_locationManager.getLastKnownLocation(m_provider);
		if (location != null) {
			double curLat = location.getLatitude();
			double curLng = location.getLongitude();
			LatLng curCoordinate = new LatLng(curLat, curLng);
			m_map.moveCamera(CameraUpdateFactory.newLatLngZoom(curCoordinate,
					14));
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {

		// If the user wants to create a comment
		case R.id.create_comment:
			/*
			 * newIntent = new Intent(this, CreateComment.class);
			 * this.startActivityForResult(newIntent, 0);
			 */

			return true;

			// If the user wants to take a photo
		case R.id.take_photo:
			/*
			 * newIntent = new Intent(this, CameraAndroid.class);
			 * this.startActivityForResult(newIntent, 0);
			 */

			return true;

			// If the user wants to find friends
		case R.id.find_friend:
			Intent newIntent = new Intent(this, FindFriend.class);
			this.startActivityForResult(newIntent, 0);

			return true;

			// If the user wants to see his friends requests
		case R.id.friend_requests:

			try {
				// If the user has friend's requests.
				if (new GetRequestsAsyncTask().execute().get().size() > 0) {

					newIntent = new Intent(this, FriendRequest.class);
					this.startActivityForResult(newIntent, 0);
				} else {
					Toast.makeText(getApplicationContext(),
							"You do not have any friend's request.",
							Toast.LENGTH_LONG).show();
				}
			} catch (Exception e) {
				Toast.makeText(
						getApplicationContext(),
						"Fail while trying to access to friend's request: "
								+ e.getMessage(), Toast.LENGTH_LONG).show();

			}

			return true;

			// If the user wants to modify his personal data
		case R.id.modify_personal:

			/*
			 * newIntent = new Intent(this, ModifyUser.class);
			 * this.startActivityForResult(newIntent, 0);
			 */
			return true;

			// If the user wants to exit
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

	}

	protected void showCurrentLocation() {

		/*
		 * try { Location location = m_locationManager
		 * .getLastKnownLocation(m_provider);
		 * 
		 * if (location != null) {
		 * 
		 * double curLat = location.getLatitude(); double curLng =
		 * location.getLongitude(); LatLng curCoordinate = new LatLng(curLat,
		 * curLng); if (m_curMarker != null) { m_curMarker.remove(); }
		 * m_curMarker = m_map.addMarker(new MarkerOptions()
		 * .position(curCoordinate) .title("Current")
		 * .snippet("This is my location") .icon(BitmapDescriptorFactory
		 * .fromResource(R.drawable.marker)));
		 * 
		 * // get place try {
		 * 
		 * Bundle bundle = getIntent().getExtras(); double toLat =
		 * bundle.getDouble("lat"); double toLng = bundle.getDouble("lng");
		 * 
		 * if (toLat != 0.0 && toLng != 0.0) { try { m_map.addPolyline(new
		 * NetworkAsyncTask(toLat, toLng, curCoordinate).execute().get());
		 * 
		 * } catch (Exception e) {
		 * 
		 * } }
		 * 
		 * } catch (Exception ex) {
		 * 
		 * }
		 * 
		 * } else Toast.makeText(this, "Can not get current location !",
		 * Toast.LENGTH_LONG).show(); } catch (Exception e) {
		 * 
		 * }
		 */

	}

	@Override
	public void onLocationChanged(Location location) {
		/*
		 * double lat = location.getLatitude(); double lng =
		 * location.getLongitude(); LatLng coordinate = new LatLng(lat, lng); if
		 * (m_curMarker != null) { m_curMarker.remove(); } m_curMarker =
		 * m_map.addMarker(new MarkerOptions().position(coordinate)
		 * .title("Current").snippet("This is my location")
		 * .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
		 */

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
	}

	private class NetworkAsyncTask extends
			AsyncTask<Void, Void, PolylineOptions> {

		ProgressDialog mProgressDialog;
		double m_toLat;
		double m_toLng;
		LatLng m_start;

		public NetworkAsyncTask(double toLat, double toLng, LatLng start) {
			this.m_toLat = toLat;
			this.m_toLng = toLng;
			this.m_start = start;
		}

		@Override
		protected void onPostExecute(PolylineOptions result) {
			mProgressDialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			mProgressDialog = ProgressDialog.show(Map.this, "Loading...",
					"Data is Loading...");
		}

		@Override
		protected PolylineOptions doInBackground(Void... params) {
			/*
			 * LatLng toCoordinate = new LatLng(m_toLat, m_toLng);
			 * GMapV2Direction md = new GMapV2Direction();
			 * 
			 * Document doc = md.getDocument(m_start, toCoordinate,
			 * GMapV2Direction.MODE_DRIVING); ArrayList<LatLng> directionPoint =
			 * md.getDirection(doc);
			 */
			PolylineOptions rectLine = new PolylineOptions().width(3).color(
					Color.RED);

			/*
			 * for (int i = 0; i < directionPoint.size(); i++) {
			 * rectLine.add(directionPoint.get(i)); }
			 */

			return rectLine;
		}
	}

	private class GetRequestsAsyncTask extends
			AsyncTask<Void, Void, List<KUserInfo>> {

		private ProgressDialog m_progressDialog;

		public GetRequestsAsyncTask() {
		}

		@Override
		protected void onPostExecute(List<KUserInfo> result) {
			m_progressDialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			m_progressDialog = ProgressDialog.show(Map.this, "Searching...",
					"System is Searching...");
		}

		@Override
		protected List<KUserInfo> doInBackground(Void... params) {
			try {
				return ToServer.getRequests();
			} catch (Exception e) {
				return new ArrayList<KUserInfo>();
			}
		}
	}

	private class LogPositionAsyncTask extends AsyncTask<Void, Void, Boolean> {

		private ProgressDialog m_progressDialog;
		private float m_longitude;
		private float m_latitude;

		public LogPositionAsyncTask(float longitude, float latitude) {
			m_longitude = longitude;
			m_latitude = latitude;
		}

		@Override
		protected void onPostExecute(Boolean result) {
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				return ToServer.logPosition(m_longitude, m_latitude);
			} catch (Exception e) {
				return false;
			}
		}
	}

	private class GetPositionAsyncTask extends
			AsyncTask<Void, Void, List<KPosition>> {

		private int m_id;

		public GetPositionAsyncTask(int id) {
			m_id = id;
		}

		@Override
		protected void onPostExecute(List<KPosition> result) {
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected List<KPosition> doInBackground(Void... params) {
			try {
				return ToServer.getPositions(m_id, 1);
			} catch (Exception e) {
				return new ArrayList<KPosition>();
			}
		}
	}

	private class GetFriendsAsyncTask extends
			AsyncTask<Void, Void, List<KUserInfo>> {

		public GetFriendsAsyncTask() {
		}

		@Override
		protected void onPostExecute(List<KUserInfo> result) {
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected List<KUserInfo> doInBackground(Void... params) {
			try {
				return ToServer.getFriends();
			} catch (Exception e) {
				return new ArrayList<KUserInfo>();
			}
		}
	}

	@Override
	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
		long currentTime = System.currentTimeMillis();
		long deltaTime = currentTime - m_lastUpdate;
		if (deltaTime >= 60000) {
			Location location = m_locationManager
					.getLastKnownLocation(m_provider);
			double curLat = location.getLatitude();
			double curLng = location.getLongitude();
			m_lastUpdate = currentTime;
			new LogPositionAsyncTask(Float.parseFloat("" + curLng),
					Float.parseFloat("" + curLat)).execute();
			try {
				List<KUserInfo> users = new GetFriendsAsyncTask()
						.execute().get();
				for (KUserInfo user : users) {
					List<KPosition> positions = new GetPositionAsyncTask(
							user.getId()).execute().get();
					if (positions != null && positions.size() != 0) {
						Marker marker = m_map
								.addMarker(new MarkerOptions()
										.position(
												new LatLng(
														positions
																.get(0)
																.getLatitudeFloat(),
														positions
																.get(0)
																.getLongitudeFloat()))
										.title(user.getNick())
										.snippet(user.getName())
										.icon(BitmapDescriptorFactory
												.fromResource(R.drawable.ic_launcher)));
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
