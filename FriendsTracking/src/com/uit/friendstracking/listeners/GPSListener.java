package com.uit.friendstracking.listeners;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

import com.uit.friendstracking.webservices.ToServer;

public class GPSListener implements LocationListener {

	private static Location m_currentLocation = null;
	private Context m_context;

	public GPSListener(Context ctx) {
		this.m_context = ctx;
	}

	public void setCurrentLocation(Location location) {
		m_currentLocation = location;
	}

	public Location getCurrentLocation() {
		return m_currentLocation;

	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			setCurrentLocation(location);
			try {
				boolean succesfully = ToServer.logPosition((float) (location.getLongitude()), (float) (location.getLatitude()));
				if (succesfully) {
					Toast.makeText(m_context, "The position has been updated succesfully.", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(m_context, "Failed to update the position.", Toast.LENGTH_LONG).show();
				}
			} catch (Exception e) {
				Toast.makeText(m_context, "Failed to update the position: " + e.getMessage(), Toast.LENGTH_LONG).show();

			}
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

	public int getCurrentLatitude1E6() {
		return ((int) (m_currentLocation.getLatitude() * 1E6));

	}

	public int getCurrentLongitude1E6() {
		return ((int) (m_currentLocation.getLongitude() * 1E6));

	}

	public int getCurrentLatitude() {
		return ((int) (m_currentLocation.getLatitude()));

	}

	public int getCurrentLongitude() {
		return ((int) (m_currentLocation.getLongitude()));

	}

	public boolean hasPosition() {
		if (m_currentLocation == null)
			return false;
		return true;
	}

}
