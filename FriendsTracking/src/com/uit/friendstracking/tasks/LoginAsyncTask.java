package com.uit.friendstracking.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.uit.friendstracking.webservices.ToServer;

public class LoginAsyncTask extends AsyncTask<Void, Void, Boolean> {

	private Context m_context;
	private ProgressDialog m_progressDialog = null;
	private String m_userName;
	private String m_passWord;

	public LoginAsyncTask(Context context, String username, String password) {
		m_context = context;
		m_userName = username;
		m_passWord = password;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (m_context != null) {
			m_progressDialog.dismiss();
		}
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		if (m_context != null) {
			m_progressDialog = ProgressDialog.show(m_context, "Login", "System is logging to server ...");
		}
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		try {
			return ToServer.login(m_userName, m_passWord);
		} catch (Exception e) {
			return false;
		}
	}
}