package com.uit.friendstracking.tasks;

import android.os.AsyncTask;

import com.uit.friendstracking.models.KNote;
import com.uit.friendstracking.webservices.ToServer;

public class GetNoteAsyncTask extends AsyncTask<Void, Void, KNote> {

	private int m_noteId;

	public GetNoteAsyncTask(int id) {
		m_noteId = id;
	}

	@Override
	protected void onPostExecute(KNote result) {
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected KNote doInBackground(Void... params) {
		try {
			return ToServer.getNote(m_noteId);
		} catch (Exception e) {
			return null;
		}
	}
}
