package com.uit.friendstracking.tasks;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

import com.uit.friendstracking.models.KNote;
import com.uit.friendstracking.webservices.ToServer;

public class GetNotesAsyncTask extends AsyncTask<Void, Void, List<KNote>> {

	private int m_id;

	public GetNotesAsyncTask(int id) {
		m_id = id;
	}

	@Override
	protected void onPostExecute(List<KNote> result) {
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected List<KNote> doInBackground(Void... params) {
		try {
			return ToServer.getNotes(m_id, 200, false);
		} catch (Exception e) {
			return new ArrayList<KNote>(0);
		}
	}
}
