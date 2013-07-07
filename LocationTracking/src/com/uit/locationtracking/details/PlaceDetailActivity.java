package com.uit.locationtracking.details;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TableLayout;
import android.widget.TextView;

import com.uit.locationtracking.MainActivity;
import com.uit.locationtracking.R;
import com.uit.locationtracking.general.Setting;
import com.uit.locationtracking.map.DirectionAdapter;
import com.uit.locationtracking.map.Directions;
import com.uit.locationtracking.models.Place;
import com.uit.locationtracking.models.Route;

public class PlaceDetailActivity extends Activity {

	String reference;
	public final String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/details/json?";
	public String API_KEY = "AIzaSyDU47OPm64uft4oMLi_PVDh_IOYhSakjhk";
	Place place;
	ArrayList<Route> routes;

	TextView name, address, phone;
	Button call, map, web;
	RatingBar ratingBar;
	TableLayout tableRow;
	ProgressDialog progressDialog;
	ListView routeList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.place_detail);

			name = (TextView) findViewById(R.id.placeDetailName);
			address = (TextView) findViewById(R.id.placeDetailAddress);
			phone = (TextView) findViewById(R.id.placeDetailPhone);
			call = (Button) findViewById(R.id.detail_call);
			map = (Button) findViewById(R.id.detail_map);
			web = (Button) findViewById(R.id.detail_web);
			ratingBar = (RatingBar) findViewById(R.id.detail_rating);
			tableRow = (TableLayout) findViewById(R.id.detail_table);

			routeList = (ListView) findViewById(R.id.place_detail_route);

			Bundle bundle = getIntent().getExtras();
			reference = bundle.getString("reference");

			new DetailAsyncTask(this).execute();

		} catch (Exception ex) {
			Log.d("PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP", ex.getMessage());
		}
	}

	public String getResult() throws IOException {
		// making query string
		String query = PLACES_SEARCH_URL + "reference=" + reference;
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
		try {
			JSONObject j = new JSONObject(rawJSON).getJSONObject("result");
			place = new Place();
			place.setName(j.getString("name"));
			place.setLat(j.getJSONObject("geometry").getJSONObject("location")
					.getDouble("lat"));
			place.setLng(j.getJSONObject("geometry").getJSONObject("location")
					.getDouble("lng"));
			place.setAddress(j.getString("formatted_address"));
			try {
				place.setPhone(j.getString("formatted_phone_number"));
				place.setUrl(j.getString("website"));
				place.setRating(Float.parseFloat(j.getString("rating")));
			} catch (Exception e) {
				e.getMessage();
			}
		} catch (JSONException e) {
			Log.d("PARSEEEEEEEEEEEEEEEE", e.getMessage());
		}
	}

	// ASYNC TASK
	public class DetailAsyncTask extends AsyncTask<Void, Integer, Void> {

		Context context;

		public DetailAsyncTask(Context context) {
			this.context = context;
		}

		@Override
		protected void onPreExecute() {
			tableRow.setVisibility(View.INVISIBLE);
			ratingBar.setVisibility(View.INVISIBLE);
			progressDialog = ProgressDialog.show(context, getResources()
					.getString(R.string.loading_title), getResources()
					.getString(R.string.loading_message));

		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
			tableRow.setVisibility(View.VISIBLE);
			ratingBar.setVisibility(View.VISIBLE);

			name.setText(place.getName());
			address.setText(place.getAddress());
			phone.setText(place.getPhone());
			ratingBar.setRating(place.getRating());

			map.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(getApplicationContext(),
							MainActivity.class);
					Bundle bundle = getIntent().getExtras();
					;
					bundle.putDouble("lng", place.getLng());
					bundle.putDouble("lat", place.getLat());
					intent.putExtras(bundle);
					startActivity(intent);
				}
			});

			final String link = place.getUrl();
			if (!link.equalsIgnoreCase("") && link != null) {
				web.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri
								.parse(link));
						startActivity(intent);
					}
				});
			}

			final String phoneCall = place.getPhone();
			if (!phoneCall.equalsIgnoreCase("") && phoneCall != null) {
				call.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {
						Intent intent = new Intent(Intent.ACTION_CALL);
						intent.setData(Uri.parse("tel:" + phoneCall));
						startActivity(intent);
					}
				});
			}

			DirectionAdapter adapter = new DirectionAdapter(context,
					R.layout.place_detail, routes);
			routeList.setAdapter(adapter);
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				parsePlace(getResult());
				routes = Directions.parseDirection(Setting.latitude,
						Setting.longitude, place.getLat(), place.getLng(), 1);
			} catch (IOException e) {
				Log.d("QUERYYYYYYYYYY", e.getMessage());
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {

		}
	}
}
