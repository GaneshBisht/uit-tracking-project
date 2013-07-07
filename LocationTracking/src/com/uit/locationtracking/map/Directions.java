package com.uit.locationtracking.map;

import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.uit.locationtracking.models.Route;

public class Directions {

	private static StringBuilder urlString = new StringBuilder();
	public static String timeTotal = "";
	public static String distanceTotal = "";
	public static String sAddress = "";
	public static String eAddress = "";
	public static String paths = "";

	public static ArrayList<Route> route = new ArrayList<Route>();

	public static ArrayList<Route> parseDirection(Double latStart,
			Double longStart, Double latEnd, Double longEnd, int type) {
		String KEY_STEP = "steps";
		String KEY_DURATION = "duration";
		String KEY_HTML = "html_instructions";
		String KEY_TEXT = "text";
		String KEY_DISTANCE = "distance";
		String KEY_LAT = "lat";
		String KEY_LNG = "lng";
		paths = "";
		route = new ArrayList<Route>();

		urlString = new StringBuilder();
		urlString
				.append("http://maps.googleapis.com/maps/api/directions/json?origin=");
		urlString.append(latStart);
		urlString.append(",");
		urlString.append(longStart);
		urlString.append("&destination=");
		urlString.append(latEnd);
		urlString.append(",");
		urlString.append(longEnd);
		urlString.append("&sensor=false");

		if (type == 1)
			urlString.append("&mode=driving");
		else
			urlString.append("&mode=walking");

		String doc = "";
		try {
			URL url = new URL(urlString.toString());
			doc = HttpRequest.Request(url);
		} catch (Exception e) {
		}
		Log.i("aaa", urlString.toString());
		Log.i("aaa", doc);

		try {
			JSONObject j = new JSONObject(doc);
			JSONArray jRoutes = j.getJSONArray("routes");
			JSONArray jLegs = jRoutes.getJSONObject(0).getJSONArray("legs");
			timeTotal = jLegs.getJSONObject(0).getJSONObject(KEY_DURATION)
					.getString(KEY_TEXT);
			distanceTotal = jLegs.getJSONObject(0).getJSONObject(KEY_DISTANCE)
					.getString(KEY_TEXT);
			sAddress = jLegs.getJSONObject(0).getString("start_address");
			eAddress = jLegs.getJSONObject(0).getString("end_address");

			JSONArray jSteps = jLegs.getJSONObject(0).getJSONArray(KEY_STEP);
			for (int i = 0; i < jSteps.length(); i++) {
				Route r = new Route();
				JSONObject detailStep = jSteps.getJSONObject(i);

				r.setInstruction(detailStep.getString(KEY_HTML));
				r.setDistance(detailStep.getJSONObject(KEY_DISTANCE).getString(
						KEY_TEXT));
				r.setDuration(detailStep.getJSONObject(KEY_DURATION).getString(
						KEY_TEXT));

				route.add(r);
				if (i < jSteps.length() - 1) {
					paths += detailStep.getJSONObject("start_location")
							.getString(KEY_LAT);
					paths += ","
							+ detailStep.getJSONObject("start_location")
									.getString(KEY_LNG) + " ";
				} else {
					paths += detailStep.getJSONObject("start_location")
							.getString(KEY_LAT);
					paths += ","
							+ detailStep.getJSONObject("start_location")
									.getString(KEY_LNG) + " ";

					paths += detailStep.getJSONObject("end_location")
							.getString(KEY_LAT);
					paths += ","
							+ detailStep.getJSONObject("end_location")
									.getString(KEY_LNG);
				}
			}

		} catch (JSONException e) {
			Log.i("zzz", e.getMessage());
		}

		return route;
	}
}
