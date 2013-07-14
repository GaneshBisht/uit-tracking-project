package com.uit.friendstracking.tasks;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

import com.uit.friendstracking.models.KPosition;
import com.uit.friendstracking.webservices.ToServer;

public class GetPositionAsyncTask extends
		AsyncTask<Void, Void, List<KPosition>> {

	private int m_id;

	public GetPositionAsyncTask(int id) {
		m_id = id;
	}

	@Override
	protected void onPostExecute(List<KPosition> result) {
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected List<KPosition> doInBackground(Void... params) {
		try {
			return ToServer.getPositions(m_id, 1);
		} catch (Exception e) {
			return new ArrayList<KPosition>();
		}
	}
}