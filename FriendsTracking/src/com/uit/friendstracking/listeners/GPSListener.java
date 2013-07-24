package com.uit.friendstracking.listeners;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.uit.friendstracking.tasks.LogPositionAsyncTask;

public class GPSListener implements LocationListener {

	private static Location m_currentLocation = null;
	private int m_mode;
	private long m_lastUpdateMine = 0;
	private final static int DELTA_TIME = 10000;

	public GPSListener(int mode) {
		this.m_mode = mode;
	}

	public void setCurrentLocation(Location location) {
		m_currentLocation = location;
	}

	public Location getCurrentLocation() {
		return m_currentLocation;

	}

	@Override
	public void onLocationChanged(Location location) {
		if (m_mode == 1) {
			long currentTime = System.currentTimeMillis();
			long deltaTime = currentTime - m_lastUpdateMine;
			if (deltaTime >= DELTA_TIME) {
				m_lastUpdateMine = currentTime;
				double curLat = location.getLatitude();
				double curLng = location.getLongitude();
				new LogPositionAsyncTask(Float.parseFloat("" + curLng), Float.parseFloat("" + curLat)).execute();
			}
		} else if (m_mode == 2) {
			if (location != null) {
				setCurrentLocation(location);
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
