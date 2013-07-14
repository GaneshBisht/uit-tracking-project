package com.uit.friendstracking.tasks;

import android.os.AsyncTask;

import com.uit.friendstracking.webservices.ToServer;

public class LogPositionAsyncTask extends AsyncTask<Void, Void, Boolean> {

	private float m_longitude;
	private float m_latitude;

	public LogPositionAsyncTask(float longitude, float latitude) {
		m_longitude = longitude;
		m_latitude = latitude;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
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