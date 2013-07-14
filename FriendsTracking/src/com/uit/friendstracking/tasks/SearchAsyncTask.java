package com.uit.friendstracking.tasks;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.uit.friendstracking.models.KUserInfo;
import com.uit.friendstracking.webservices.ToServer;

public class SearchAsyncTask extends AsyncTask<Void, Void, List<KUserInfo>> {

	private Context m_context;
	private ProgressDialog m_progressDialog;
	private String m_name;
	private String m_surname;
	private String m_nick;
	private String m_country;

	public SearchAsyncTask(Context context, String name, String surname,
			String nick, String country) {
		m_context = context;
		m_name = name;
		m_surname = surname;
		m_nick = nick;
		m_country = country;
	}

	@Override
	protected void onPostExecute(List<KUserInfo> result) {
		m_progressDialog.dismiss();
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		m_progressDialog = ProgressDialog.show(m_context, "Search Friends",
				"System is searching friends ...");
		super.onPreExecute();
	}

	@Override
	protected List<KUserInfo> doInBackground(Void... params) {
		try {
			return ToServer.searchFriend(m_nick, m_name, m_surname, m_country);
		} catch (Exception e) {
			return new ArrayList<KUserInfo>();
		}
	}
}