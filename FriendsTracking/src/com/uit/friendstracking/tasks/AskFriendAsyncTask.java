package com.uit.friendstracking.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.uit.friendstracking.webservices.ToServer;

public class AskFriendAsyncTask extends AsyncTask<Void, Void, Boolean> {

	private Context m_context;
	private ProgressDialog m_progressDialog;
	private int m_to;

	public AskFriendAsyncTask(Context context, int to) {
		m_context = context;
		m_to = to;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		m_progressDialog.dismiss();
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		m_progressDialog = ProgressDialog.show(m_context, "Ask Friend",
				"System is asking friend ...");
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		try {
			return ToServer.askFriend(m_to);
		} catch (Exception e) {
			return false;
		}
	}
}