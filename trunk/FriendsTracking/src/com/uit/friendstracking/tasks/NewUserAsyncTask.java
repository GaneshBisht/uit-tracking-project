package com.uit.friendstracking.tasks;

import model.Photo;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.uit.friendstracking.models.KUserInfo;
import com.uit.friendstracking.webservices.ToServer;

public class NewUserAsyncTask extends AsyncTask<Void, Void, Boolean> {

	private Context m_context;
	private ProgressDialog m_progressDialog;
	private KUserInfo m_user;
	private String m_passWord;
	private Photo m_photo;

	public NewUserAsyncTask(Context context, KUserInfo user, String passWord, Photo photo) {
		m_context = context;
		m_user = user;
		m_passWord = passWord;
		m_photo = photo;
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
			m_progressDialog = ProgressDialog.show(m_context, "New User", "System is adding user ...");
		}
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		try {
			return ToServer.newUser1(m_user, m_passWord, m_photo);
		} catch (Exception e) {
			return false;
		}
	}
}