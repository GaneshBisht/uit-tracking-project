package com.uit.friendstracking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONObject;

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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
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

	ArrayList<LatLng> markerPoints;
	TextView tvDistanceDuration;
	RadioButton rbDriving;
	RadioButton rbBiCycling;
	RadioButton rbWalking;
	RadioGroup rgModes;

	int mMode = 0;
	final int MODE_DRIVING = 0;
	final int MODE_BICYCLING = 1;
	final int MODE_WALKING = 2;

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
												.snippet(
														user.getPosition()
																.toString())
												.icon(BitmapDescriptorFactory
														.fromResource(R.drawable.ic_launcher)));
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

		tvDistanceDuration = (TextView) findViewById(R.id.tv_distance_time);

		// Getting reference to rb_driving
		rbDriving = (RadioButton) findViewById(R.id.rb_driving);

		// Getting reference to rb_bicylcing
		rbBiCycling = (RadioButton) findViewById(R.id.rb_bicycling);

		// Getting reference to rb_walking
		rbWalking = (RadioButton) findViewById(R.id.rb_walking);

		// Getting Reference to rg_modes
		rgModes = (RadioGroup) findViewById(R.id.rg_modes);

		// Getting reference to Button
		Button btnDraw = (Button) findViewById(R.id.btn_draw);

		m_locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		m_provider = m_locationManager.getBestProvider(criteria, true);
		// registerReceiver(m_timeChangedReceiver, m_intentFilter);

		setUpMapIfNeeded();
		rgModes.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				// Checks, whether start and end locations are captured
				if (markerPoints.size() >= 2) {
					LatLng origin = markerPoints.get(0);
					LatLng dest = markerPoints.get(1);

					// Getting URL to the Google Directions API
					String url = getDirectionsUrl(origin, dest);

					DownloadTask downloadTask = new DownloadTask();

					// Start downloading json data from Google Directions API
					downloadTask.execute(url);
				}
			}
		});

		// Initializing
		markerPoints = new ArrayList<LatLng>();

		if (m_map != null) {

			// Setting onclick event listener for the map
			m_map.setOnMapClickListener(new OnMapClickListener() {

				@Override
				public void onMapClick(LatLng point) {

					// Already two locations
					if (markerPoints.size() > 1) {
						markerPoints.clear();
						m_map.clear();
						// return;
					}

					// Adding new item to the ArrayList
					markerPoints.add(point);

					// Draws Start and Stop markers on the Google Map
					drawStartStopMarkers();

					// Creating MarkerOptions
					MarkerOptions options = new MarkerOptions();

					// Setting the position of the marker
					options.position(point);

					/**
					 * For the start location, the color of marker is GREEN and
					 * for the end location, the color of marker is RED.
					 */
					if (markerPoints.size() == 1) {
						options.icon(BitmapDescriptorFactory
								.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
					} else if (markerPoints.size() == 2) {
						options.icon(BitmapDescriptorFactory
								.defaultMarker(BitmapDescriptorFactory.HUE_RED));
					} else {
						options.icon(BitmapDescriptorFactory
								.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
					}

					// Add new marker to the Google Map Android API V2
					m_map.addMarker(options);

					// Checks, whether start and end locations are captured
					if (markerPoints.size() >= 2) {
						LatLng origin = markerPoints.get(0);
						LatLng dest = markerPoints.get(1);

						// Getting URL to the Google Directions API
						String url = getDirectionsUrl(origin, dest);

						DownloadTask downloadTask = new DownloadTask();

						// Start downloading json data from Google Directions
						// API
						downloadTask.execute(url);
					}

				}
			});

			// Click event handler for Button btn_draw
			btnDraw.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (markerPoints.size() >= 2) {
						LatLng origin = markerPoints.get(0);
						LatLng dest = markerPoints.get(1);
						final Intent intent = new Intent(Intent.ACTION_VIEW,
						/** Using the web based turn by turn directions url. */
						Uri.parse("http://maps.google.com/maps?" + "saddr="
								+ origin.latitude + "," + origin.longitude
								+ "&daddr=" + dest.latitude + ","
								+ dest.longitude));
						/**
						 * Setting the Class Name that should handle this
						 * intent. We are setting the class name to the class
						 * name of the native maps activity. Android platform
						 * recognizes this and now knows that we want to open up
						 * the Native Maps application to handle the URL. Hence
						 * it does not give the choice of application to the
						 * user and directly opens the Native Google Maps
						 * application.
						 */
						intent.setClassName("com.google.android.apps.maps",
								"com.google.android.maps.MapsActivity");
						startActivity(intent);
						// Checks, whether start and end locations are captured
					}

				}
			});

			// The map will be cleared on long click
			m_map.setOnMapLongClickListener(new OnMapLongClickListener() {

				@Override
				public void onMapLongClick(LatLng point) {
					// Removes all the points from Google Map
					m_map.clear();

					// Removes all the points in the ArrayList
					markerPoints.clear();

				}
			});
		}
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
			m_map = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
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
			Intent noteIntent = new Intent(this, CreateNote.class);
			this.startActivityForResult(noteIntent, 0);
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
		case R.id.update_userinfo:
			Intent intentUpdateUser = new Intent(this, UserInformation.class);
			// intentUpdateUser.putExtra("items", listItems);
			this.startActivityForResult(intentUpdateUser, 0);

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
			new LogPositionAsyncTask(Float.parseFloat("" + curLng),
					Float.parseFloat("" + curLat)).execute();
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
	
	// Drawing Start and Stop locations
		private void drawStartStopMarkers() {

			for (int i = 0; i < markerPoints.size(); i++) {

				// Creating MarkerOptions
				MarkerOptions options = new MarkerOptions();

				// Setting the position of the marker
				options.position(markerPoints.get(i));

				/**
				 * For the start location, the color of marker is GREEN and for the
				 * end location, the color of marker is RED.
				 */
				if (i == 0) {
					options.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
				} else if (i == 1) {
					options.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_RED));
				}

				// Add new marker to the Google Map Android API V2
				m_map.addMarker(options);
			}
		}

		private String getDirectionsUrl(LatLng origin, LatLng dest) {

			// Origin of route
			String str_origin = "origin=" + origin.latitude + ","
					+ origin.longitude;

			// Destination of route
			String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

			// Sensor enabled
			String sensor = "sensor=false";

			// Waypoints
			// String waypoints = "";
			// for (int i = 2; i < markerPoints.size(); i++) {
			// LatLng point = (LatLng) markerPoints.get(i);
			// if (i == 2)
			// waypoints = "waypoints=";
			// waypoints += point.latitude + "," + point.longitude + "|";
			// }

			// Travelling Mode
			String mode = "mode=driving";

			if (rbDriving.isChecked()) {
				mode = "mode=driving";
				mMode = 0;
			} else if (rbBiCycling.isChecked()) {
				mode = "mode=bicycling";
				mMode = 1;
			} else if (rbWalking.isChecked()) {
				mode = "mode=walking";
				mMode = 2;
			}

			// Building the parameters to the web service
			String parameters = str_origin + "&" + str_dest + "&" + sensor + "&"
					+ mode;

			// Output format
			String output = "json";

			// Building the url to the web service
			String url = "https://maps.googleapis.com/maps/api/directions/"
					+ output + "?" + parameters;

			return url;
		}

		/** A method to download json data from url */
		private String downloadUrl(String strUrl) throws IOException {
			String data = "";
			InputStream iStream = null;
			HttpURLConnection urlConnection = null;
			try {
				URL url = new URL(strUrl);

				// Creating an http connection to communicate with url
				urlConnection = (HttpURLConnection) url.openConnection();

				// Connecting to url
				urlConnection.connect();

				// Reading data from url
				iStream = urlConnection.getInputStream();

				BufferedReader br = new BufferedReader(new InputStreamReader(
						iStream));

				StringBuffer sb = new StringBuffer();

				String line = "";
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}

				data = sb.toString();

				br.close();

			} catch (Exception e) {
				Log.d("Exception while downloading url", e.toString());
			} finally {
				iStream.close();
				urlConnection.disconnect();
			}
			return data;
		}

		// Fetches data from url passed
		private class DownloadTask extends AsyncTask<String, Void, String> {

			// Downloading data in non-ui thread
			@Override
			protected String doInBackground(String... url) {

				// For storing data from web service
				String data = "";

				try {
					// Fetching the data from web service
					data = downloadUrl(url[0]);
				} catch (Exception e) {
					Log.d("Background Task", e.toString());
				}
				return data;
			}

			// Executes in UI thread, after the execution of
			// doInBackground()
			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);

				ParserTask parserTask = new ParserTask();

				// Invokes the thread for parsing the JSON data
				parserTask.execute(result);

			}
		}

		/** A class to parse the Google Places in JSON format */
		private class ParserTask extends
				AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

			// Parsing the data in non-ui thread
			@Override
			protected List<List<HashMap<String, String>>> doInBackground(
					String... jsonData) {

				JSONObject jObject;
				List<List<HashMap<String, String>>> routes = null;

				try {
					jObject = new JSONObject(jsonData[0]);
					DirectionsJSONParser parser = new DirectionsJSONParser();

					// Starts parsing data
					routes = parser.parse(jObject);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return routes;
			}

			// Executes in UI thread, after the parsing process
			@Override
			protected void onPostExecute(List<List<HashMap<String, String>>> result) {
				ArrayList<LatLng> points = null;
				PolylineOptions lineOptions = null;
				MarkerOptions markerOptions = new MarkerOptions();
				String distance = "";
				String duration = "";

				if (result.size() < 1) {
					Toast.makeText(getBaseContext(), "No Points",
							Toast.LENGTH_SHORT).show();
					return;
				}

				// Traversing through all the routes
				for (int i = 0; i < result.size(); i++) {
					points = new ArrayList<LatLng>();
					lineOptions = new PolylineOptions();

					// Fetching i-th route
					List<HashMap<String, String>> path = result.get(i);

					// Fetching all the points in i-th route
					for (int j = 0; j < path.size(); j++) {
						HashMap<String, String> point = path.get(j);

						if (j == 0) { // Get distance from the list
							distance = (String) point.get("distance");
							continue;
						} else if (j == 1) { // Get duration from the list
							duration = (String) point.get("duration");
							continue;
						}

						double lat = Double.parseDouble(point.get("lat"));
						double lng = Double.parseDouble(point.get("lng"));
						LatLng position = new LatLng(lat, lng);

						points.add(position);
					}

					// Adding all the points in the route to LineOptions
					lineOptions.addAll(points);
					lineOptions.width(2);

					// Changing the color polyline according to the mode
					if (mMode == MODE_DRIVING)
						lineOptions.color(Color.RED);
					else if (mMode == MODE_BICYCLING)
						lineOptions.color(Color.GREEN);
					else if (mMode == MODE_WALKING)
						lineOptions.color(Color.BLUE);

				}

				tvDistanceDuration.setText("Distance:" + distance + ", Duration:"
						+ duration);

				// Drawing polyline in the Google Map for the i-th route
				m_map.addPolyline(lineOptions);
			}
		}

}
