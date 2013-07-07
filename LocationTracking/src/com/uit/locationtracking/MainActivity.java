package com.uit.locationtracking;

import java.util.ArrayList;

import org.w3c.dom.Document;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.uit.locationtracking.category.CategoryActivity;
import com.uit.locationtracking.map.GMapV2Direction;

public class MainActivity extends FragmentActivity implements LocationListener {
	private GoogleMap m_map;
	protected LocationManager m_locationManager;
	private String m_provider;
	private Marker m_curMarker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		m_locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		m_provider = m_locationManager.getBestProvider(criteria, false);
		setUpMapIfNeeded();
		showCurrentLocation();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
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
			}
		}
		m_map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		m_map.setTrafficEnabled(true);
		m_map.setMyLocationEnabled(true);
		m_map.getUiSettings().setMyLocationButtonEnabled(false);
		setUpCamera();
	}

	protected void setUpCamera() {
		Location location = m_locationManager.getLastKnownLocation(m_provider);
		double curLat = location.getLatitude();
		double curLng = location.getLongitude();
		LatLng curCoordinate = new LatLng(curLat, curLng);
		m_map.moveCamera(CameraUpdateFactory.newLatLngZoom(curCoordinate, 14));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {

		case R.id.mnuPlaces:
			startActivity(new Intent(this, CategoryActivity.class));
			this.finish();
			break;

		default:
			break;
		}

		return true;
	}

	private void setUpMap() {

	}

	protected void showCurrentLocation() {

		try {
			Location location = m_locationManager
					.getLastKnownLocation(m_provider);

			if (location != null) {

				double curLat = location.getLatitude();
				double curLng = location.getLongitude();
				LatLng curCoordinate = new LatLng(curLat, curLng);
				if (m_curMarker != null) {
					m_curMarker.remove();
				}
				m_curMarker = m_map.addMarker(new MarkerOptions()
						.position(curCoordinate)
						.title("Current")
						.snippet("This is my location")
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.marker)));

				// get place
				try {

					Bundle bundle = getIntent().getExtras();
					double toLat = bundle.getDouble("lat");
					double toLng = bundle.getDouble("lng");

					if (toLat != 0.0 && toLng != 0.0) {
						try {
							m_map.addPolyline(new NetworkAsyncTask(toLat,
									toLng, curCoordinate).execute().get());

						} catch (Exception e) {

						}
					}

				} catch (Exception ex) {

				}

			} else
				Toast.makeText(this, "Can not get current location !",
						Toast.LENGTH_LONG).show();
		} catch (Exception e) {

		}

	}

	@Override
	public void onLocationChanged(Location location) {
		double lat = location.getLatitude();
		double lng = location.getLongitude();
		LatLng coordinate = new LatLng(lat, lng);
		if (m_curMarker != null) {
			m_curMarker.remove();
		}
		m_curMarker = m_map.addMarker(new MarkerOptions().position(coordinate)
				.title("Current").snippet("This is my location")
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));

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
			mProgressDialog = ProgressDialog.show(MainActivity.this,
					"Loading...", "Data is Loading...");
		}

		@Override
		protected PolylineOptions doInBackground(Void... params) {
			LatLng toCoordinate = new LatLng(m_toLat, m_toLng);
			GMapV2Direction md = new GMapV2Direction();

			Document doc = md.getDocument(m_start, toCoordinate,
					GMapV2Direction.MODE_DRIVING);
			ArrayList<LatLng> directionPoint = md.getDirection(doc);
			PolylineOptions rectLine = new PolylineOptions().width(3).color(
					Color.RED);

			for (int i = 0; i < directionPoint.size(); i++) {
				rectLine.add(directionPoint.get(i));
			}

			return rectLine;
		}
	}

}
