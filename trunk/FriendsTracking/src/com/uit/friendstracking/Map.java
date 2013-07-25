package com.uit.friendstracking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
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
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.uit.friendstracking.listeners.GPSListener;
import com.uit.friendstracking.models.Group;
import com.uit.friendstracking.models.Groups;
import com.uit.friendstracking.models.KNote;
import com.uit.friendstracking.models.KPhoto;
import com.uit.friendstracking.models.KPosition;
import com.uit.friendstracking.models.KUserInfo;
import com.uit.friendstracking.tasks.GetFriendsAsyncTask;
import com.uit.friendstracking.tasks.GetNoteAsyncTask;
import com.uit.friendstracking.tasks.GetNotesAsyncTask;
import com.uit.friendstracking.tasks.GetPositionAsyncTask;
import com.uit.friendstracking.tasks.GetRequestsAsyncTask;
import com.uit.friendstracking.webservices.ToServer;

public class Map extends FragmentActivity implements OnMarkerClickListener {

	private static IntentFilter m_intentFilter;
	private GoogleMap m_map;
	protected LocationManager m_locationManager;
	private String m_provider;
	private java.util.Map<Marker, KUserInfo> m_mapUserMarker = new HashMap<Marker, KUserInfo>();
	private java.util.Map<Marker, Group> m_mapNoteMarker = new HashMap<Marker, Group>();
	private long m_lastUpdateFriends = 0;
	private final static int DELTA_TIME = 10000;
	private Groups groups;
	private RelativeLayout m_container;
	private Bitmap iconGroup, photoIcon, noteIcon;
	private GPSListener m_gpsListener;

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
					drawFriendsMarker(currentTime);
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		tvDistanceDuration = (TextView) findViewById(R.id.tv_distance_time);
		rbDriving = (RadioButton) findViewById(R.id.rb_driving);
		rbBiCycling = (RadioButton) findViewById(R.id.rb_bicycling);
		rbWalking = (RadioButton) findViewById(R.id.rb_walking);
		rgModes = (RadioGroup) findViewById(R.id.rg_modes);
		Button btnDraw = (Button) findViewById(R.id.btn_draw);

		m_locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		m_provider = m_locationManager.getBestProvider(criteria, true);
		m_container = (RelativeLayout) findViewById(R.id.my_container);

		noteIcon = BitmapFactory.decodeResource(getResources(), R.drawable.note);
		iconGroup = BitmapFactory.decodeResource(getResources(), R.drawable.photosicon);
		photoIcon = BitmapFactory.decodeResource(getResources(), R.drawable.photoicon);

		m_gpsListener = new GPSListener(1);

		setUpMapIfNeeded();

		drawFriendsMarker(System.currentTimeMillis());

		rgModes.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (markerPoints.size() >= 2) {
					LatLng origin = markerPoints.get(0);
					LatLng dest = markerPoints.get(1);
					String url = getDirectionsUrl(origin, dest);
					DownloadTask downloadTask = new DownloadTask();
					downloadTask.execute(url);
				}
			}
		});

		markerPoints = new ArrayList<LatLng>();

		if (m_map != null) {

			m_map.setOnMapClickListener(new OnMapClickListener() {

				@Override
				public void onMapClick(LatLng point) {
					if (markerPoints.size() > 1) {
						markerPoints.clear();
						m_map.clear();
					}
					markerPoints.add(point);
					drawStartStopMarkers();
					MarkerOptions options = new MarkerOptions();
					options.position(point);

					if (markerPoints.size() == 1) {
						options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
					} else if (markerPoints.size() == 2) {
						options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
					} else {
						options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
					}

					m_map.addMarker(options);

					if (markerPoints.size() >= 2) {
						LatLng origin = markerPoints.get(0);
						LatLng dest = markerPoints.get(1);
						String url = getDirectionsUrl(origin, dest);
						DownloadTask downloadTask = new DownloadTask();
						downloadTask.execute(url);
					}

				}
			});

			btnDraw.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (markerPoints.size() >= 2) {
						LatLng origin = markerPoints.get(0);
						LatLng dest = markerPoints.get(1);
						final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?" + "saddr=" + origin.latitude + ","
								+ origin.longitude + "&daddr=" + dest.latitude + "," + dest.longitude));
						intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
						startActivity(intent);
					}

				}
			});

			m_map.setOnMapLongClickListener(new OnMapLongClickListener() {

				@Override
				public void onMapLongClick(LatLng point) {
					m_map.clear();
					markerPoints.clear();

				}
			});
		}
	}

	private void drawFriendsMarker(long currentTime) {
		m_lastUpdateFriends = currentTime;
		for (Marker marker : m_mapUserMarker.keySet()) {
			marker.remove();
		}
		try {
			List<KUserInfo> users = new GetFriendsAsyncTask().execute().get();
			for (KUserInfo user : users) {
				List<KPosition> positions = new GetPositionAsyncTask(user.getId()).execute().get();
				if (positions != null && positions.size() != 0) {
					Marker marker = m_map.addMarker(new MarkerOptions()
							.position(new LatLng(positions.get(0).getLatitudeFloat(), positions.get(0).getLongitudeFloat())).title(user.getNick())
							.snippet(user.getPosition().toString()).icon(BitmapDescriptorFactory.defaultMarker()));
					m_mapUserMarker.put(marker, user);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
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
		m_locationManager.requestLocationUpdates(m_provider, 500, 1, m_gpsListener);

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

		case R.id.take_photo:
			Intent noteIntent = new Intent(this, CreateNote.class);
			this.startActivityForResult(noteIntent, 0);
			return true;

		case R.id.find_friend:
			Intent newIntent = new Intent(this, FindFriend.class);
			this.startActivityForResult(newIntent, 0);
			return true;

		case R.id.update_userinfo:
			Intent intentUpdateUser = new Intent(this, UserInformation.class);
			this.startActivityForResult(intentUpdateUser, 0);
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
		m_map.setOnMarkerClickListener(this);
		m_map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		m_map.setTrafficEnabled(true);
		m_map.setMyLocationEnabled(true);
		m_map.getUiSettings().setMyLocationButtonEnabled(true);
		setUpCamera();
	}

	@Override
	protected void onPause() {
		super.onPause();
		m_locationManager.removeUpdates(m_gpsListener);
		unregisterReceiver(m_timeChangedReceiver);
	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		if (m_mapUserMarker.containsKey(arg0)) {
			for (Marker marker : m_mapUserMarker.keySet()) {
				marker.remove();
			}
			double gradesPerPixel = getGradesPerPixel();

			// Calculate the grades which the icon on the map.
			float rate = (float) (photoIcon.getHeight() * gradesPerPixel);
			try {
				List<KNote> listNotes = new GetNotesAsyncTask(m_mapUserMarker.get(arg0).getId()).execute().get();
				if (listNotes.size() > 0) {
					clasifyNotes(rate, listNotes);
					drawNotesMarker(arg0, groups);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		} else if (m_mapNoteMarker.containsKey(arg0)) {
			Intent newIntent = new Intent(this, PhotoGallery.class);
			PhotoGallery.selectedGroup = m_mapNoteMarker.get(arg0);
			List<KNote> listNotes = new ArrayList<KNote>();
			try {
				Iterator<KNote> it = PhotoGallery.selectedGroup.iterator();
				while (it.hasNext()) {
					KNote note = (KNote) it.next();
					KNote kNote = new GetNoteAsyncTask(note.getId()).execute().get();
					if (kNote != null) {
						listNotes.add(kNote);
					}

				}
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), "Failed while trying to read photos: " + e.getMessage(), Toast.LENGTH_LONG).show();
			}
			//Bundle bundle = new Bundle();
			//bundle.putSerializable("asd", listNotes);
			PhotoGallery.listNotes = listNotes;
			this.startActivityForResult(newIntent, 0);
		}
		return false;
	}

	private void clasifyNotes(float rate, List<KNote> listNotes) {
		groups = new Groups(rate);
		if (listNotes != null) {
			Iterator<KNote> it = listNotes.iterator();
			while (it.hasNext()) {
				groups.addKNote(it.next());
			}
		}
	}

	private double getGradesPerPixel() {
		LatLng mapCenter = m_map.getCameraPosition().target;
		Point screenCoordsCenter = m_map.getProjection().toScreenLocation(mapCenter);

		int height = m_container.getHeight();

		Point screenCoordsEdge = new Point(screenCoordsCenter.x, screenCoordsCenter.y + height / 2);
		LatLng edgePoint = m_map.getProjection().fromScreenLocation(screenCoordsEdge);

		double difGrades = Math.abs(mapCenter.latitude - edgePoint.latitude);
		difGrades /= 1E6;

		double difPixels = Math.abs(screenCoordsCenter.y - screenCoordsEdge.y);

		return (difGrades / difPixels);
	}

	private void drawNotesMarker(Marker ma, Groups grps) {

		Iterator<Group> it = grps.iterator();

		while (it.hasNext()) {
			Group gr = it.next();
			Marker marker;
			if (!gr.isGroup()) {
				if (gr.hasPhoto()) {
					marker = m_map.addMarker(new MarkerOptions()
							.position(new LatLng(gr.getPosition().getLatitudeFloat(), gr.getPosition().getLongitudeFloat())).title(gr.getName())
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.photoicon)));
				} else {
					marker = m_map.addMarker(new MarkerOptions()
							.position(new LatLng(gr.getPosition().getLatitudeFloat(), gr.getPosition().getLongitudeFloat())).title(gr.getName())
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.note)));
				}
			} else {
				marker = m_map.addMarker(new MarkerOptions().position(new LatLng(gr.getPosition().getLatitudeFloat(), gr.getPosition().getLongitudeFloat()))
						.title(gr.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.photosicon)));
			}
			m_mapNoteMarker.put(marker, gr);
			// drawMessageWindow(gr, canvas, mapView);
		}
	}

	private void drawStartStopMarkers() {

		for (int i = 0; i < markerPoints.size(); i++) {
			MarkerOptions options = new MarkerOptions();
			options.position(markerPoints.get(i));
			if (i == 0) {
				options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
			} else if (i == 1) {
				options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
			}
			m_map.addMarker(options);
		}
	}

	private String getDirectionsUrl(LatLng origin, LatLng dest) {

		String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

		String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

		String sensor = "sensor=false";

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

		String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

		String output = "json";

		String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

		return url;
	}

	private String downloadUrl(String strUrl) throws IOException {
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(strUrl);

			urlConnection = (HttpURLConnection) url.openConnection();

			urlConnection.connect();

			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

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

	private class DownloadTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... url) {

			String data = "";
			try {
				data = downloadUrl(url[0]);
			} catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			ParserTask parserTask = new ParserTask();
			parserTask.execute(result);
		}
	}

	private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

		@Override
		protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

			JSONObject jObject;
			List<List<HashMap<String, String>>> routes = null;

			try {
				jObject = new JSONObject(jsonData[0]);
				DirectionsJSONParser parser = new DirectionsJSONParser();
				routes = parser.parse(jObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return routes;
		}

		@Override
		protected void onPostExecute(List<List<HashMap<String, String>>> result) {
			ArrayList<LatLng> points = null;
			PolylineOptions lineOptions = null;
			MarkerOptions markerOptions = new MarkerOptions();
			String distance = "";
			String duration = "";

			if (result.size() < 1) {
				Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
				return;
			}

			for (int i = 0; i < result.size(); i++) {
				points = new ArrayList<LatLng>();
				lineOptions = new PolylineOptions();

				List<HashMap<String, String>> path = result.get(i);

				for (int j = 0; j < path.size(); j++) {
					HashMap<String, String> point = path.get(j);

					if (j == 0) {
						distance = (String) point.get("distance");
						continue;
					} else if (j == 1) {
						duration = (String) point.get("duration");
						continue;
					}

					double lat = Double.parseDouble(point.get("lat"));
					double lng = Double.parseDouble(point.get("lng"));
					LatLng position = new LatLng(lat, lng);

					points.add(position);
				}

				lineOptions.addAll(points);
				lineOptions.width(2);

				if (mMode == MODE_DRIVING)
					lineOptions.color(Color.RED);
				else if (mMode == MODE_BICYCLING)
					lineOptions.color(Color.GREEN);
				else if (mMode == MODE_WALKING)
					lineOptions.color(Color.BLUE);

			}

			tvDistanceDuration.setText("Distance:" + distance + ", Duration:" + duration);
			m_map.addPolyline(lineOptions);
		}
	}
}
