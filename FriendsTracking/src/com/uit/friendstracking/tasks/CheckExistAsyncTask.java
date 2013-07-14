package com.uit.friendstracking.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.uit.friendstracking.webservices.ToServer;

public class CheckExistAsyncTask extends AsyncTask<Void, Void, Boolean> {

	private Context m_context;
	private ProgressDialog m_progressDialog;
	private String m_userName;

	public CheckExistAsyncTask(Context context, String userName) {
		m_context = context;
		m_userName = userName;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		m_progressDialog.dismiss();
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		m_progressDialog = ProgressDialog.show(m_context, "Check Exist",
				"System is checking exist ...");
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		try {
			return ToServer.exists(m_userName);
		} catch (Exception e) {
			return false;
		}
	}
}