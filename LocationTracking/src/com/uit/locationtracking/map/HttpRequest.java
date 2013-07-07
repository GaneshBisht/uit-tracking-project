package com.uit.locationtracking.map;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class HttpRequest {

	public static URL url;

	public HttpRequest(URL url) {
		HttpRequest.url = url;
	}

	public static String Request(URL url) throws IOException {
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

	public static JSONArray ReadJSON(String sFile, String sTag)
			throws JSONException {
		JSONObject jFile = new JSONObject(sFile);
		return new JSONArray(jFile.getString(sTag));

	}

	public static GeoPoint Reverse_GeoCoding(String name, Context context) {
		Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
		GeoPoint p = null;
		Log.i("toa do", name);

		try {
			List<Address> addresses = geoCoder.getFromLocationName(name, 5);
			if (addresses.size() > 0) {
				p = new GeoPoint((int) (addresses.get(0).getLatitude() * 1E6),
						(int) (addresses.get(0).getLongitude() * 1E6));
			}
			Log.i("toa do", "get");
		}

		catch (IOException e) {
			e.printStackTrace();
			Log.i("toa do", e.getMessage());
		}
		geoCoder = new Geocoder(context);
		return p;

	}

	public static String Geocoding(double lat, double lng, Context context)
			throws IOException {
		String add = "";
		Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
		List<Address> addresses = geoCoder.getFromLocation(lat, lng, 1);

		if (addresses.size() > 0) {
			for (int i = 0; i < addresses.get(0).getMaxAddressLineIndex(); i++)
				add += addresses.get(0).getAddressLine(i) + "\n";
		}
		return add;
	}
}
