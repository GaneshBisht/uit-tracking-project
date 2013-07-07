package com.uit.locationtracking.details;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.uit.locationtracking.R;
import com.uit.locationtracking.category.ImageAdapter;
import com.uit.locationtracking.general.Setting;
import com.uit.locationtracking.models.Place;

public class SearchResultActivity extends Activity {

	int distance;// = 1000;
	double latitude;
	double longitude;
	// double latitude = 10.806050;
	// double longitude = 106.690550;
	// double latitude = -33.8670522;
	// double longitude = 151.1957362;

	String name, type;

	public final String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";
	public String API_KEY = "AIzaSyDU47OPm64uft4oMLi_PVDh_IOYhSakjhk";
	List<Place> placeList;

	TextView text;
	ResultAdapter adapter;
	ListView listView;
	ProgressDialog progressDialog;

	final static int SHOW_MAP = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_result);

		text = (TextView) findViewById(R.id.searchResultText);
		listView = (ListView) findViewById(R.id.searchList);
		listView.setSelector(R.drawable.result_style);

		// get passing data
		Bundle bundle = getIntent().getExtras();
		name = bundle.getString("categoryName");
		type = "";
		try {
			type = bundle.getString("categoryType");
		} catch (Exception e) {
			e.getMessage();
		}

		// Get LOCATION
		LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// ********************
		String provider = Setting.provider;
		// ********************

		MyLocationListener locationListener = new MyLocationListener();
		mlocManager.requestLocationUpdates(provider, 0, 0, locationListener);

		this.distance = Setting.distance;
		Setting.latitude = mlocManager.getLastKnownLocation(provider)
				.getLatitude();// Setting.latitude;
		Setting.longitude = mlocManager.getLastKnownLocation(provider)
				.getLongitude();// Setting.longitude;

		this.latitude = Setting.latitude;
		this.longitude = Setting.longitude;
		// text.setText(latitude + " // " + longitude);

		// Start Searching
		new SearchAsyncTask(this).execute();

	}

	public String getResult(double latitude, double longitude, String name,
			String type) throws IOException {
		// making query string
		String query = PLACES_SEARCH_URL + "location=" + latitude + ","
				+ longitude + "&radius=" + distance;
		if (type.equalsIgnoreCase(""))
			query += "&name=" + name;
		else
			query += "&types=" + type;
		query += "&sensor=false&language=vi&key=" + API_KEY;

		// sending query to google
		URL url = new URL(query);
		URLConnection tc = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				tc.getInputStream()));

		String line;
		StringBuffer sb = new StringBuffer();
		while ((line = in.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}

	// parse
	public void parsePlace(String rawJSON) {
		placeList = new ArrayList<Place>();
		try {
			JSONObject j = new JSONObject(rawJSON);
			JSONArray places = j.getJSONArray("results");
			for (int i = 0; i < places.length(); i++) {
				Place place = new Place();
				place.setName(places.getJSONObject(i).getString("name"));
				place.setAddress(places.getJSONObject(i).getString("vicinity"));
				place.setReference(places.getJSONObject(i).getString(
						"reference"));
				try {
					place.setRating(Float.parseFloat(places.getJSONObject(i)
							.getString("rating")));
				} catch (Exception e) {
					e.getMessage();
				}

				place.setLat(places.getJSONObject(i).getJSONObject("geometry")
						.getJSONObject("location").getDouble("lat"));
				place.setLng(places.getJSONObject(i).getJSONObject("geometry")
						.getJSONObject("location").getDouble("lng"));
				placeList.add(place);
			}
		} catch (JSONException e) {
			e.getMessage();
		}
	}

	// ASYNC TASK
	public class SearchAsyncTask extends AsyncTask<Void, Integer, Void> {

		Context context;

		public SearchAsyncTask(Context context) {
			this.context = context;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(context, getResources()
					.getString(R.string.loading_title), getResources()
					.getString(R.string.loading_message));
		}

		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
			if (placeList.isEmpty()) {
				AlertDialog alertDialog = new AlertDialog.Builder(context)
						.create();
				alertDialog.setTitle("Warning");
				alertDialog.setMessage("No places found");
				alertDialog.setIcon(ImageAdapter.resizeImage(context,
						R.drawable.warning, 24, 24));
				alertDialog.setButton("Back",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								((SearchResultActivity) context).finish();
							}
						});
				alertDialog.show();
			}
			adapter = new ResultAdapter(context, R.layout.search_result,
					placeList);
			listView.setAdapter(adapter);
		}

		@Override
		protected Void doInBackground(Void... params) {
			// query for result from google
			try {
				String result = getResult(latitude, longitude, name, type);
				if (result != "") {
					parsePlace(result);
				}
			} catch (IOException e) {
				e.getMessage();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {

		}
	}

	// LOCATION UPDATE
	public class MyLocationListener implements LocationListener {

		public void onLocationChanged(Location location) {
			Setting.latitude = location.getLatitude();
			Setting.longitude = location.getLongitude();
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

}
