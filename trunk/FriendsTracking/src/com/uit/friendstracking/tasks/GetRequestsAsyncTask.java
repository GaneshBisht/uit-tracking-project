package com.uit.friendstracking.tasks;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.uit.friendstracking.models.KUserInfo;
import com.uit.friendstracking.webservices.ToServer;

public class GetRequestsAsyncTask extends
		AsyncTask<Void, Void, List<KUserInfo>> {

	private Context m_context;
	private ProgressDialog m_progressDialog;

	public GetRequestsAsyncTask(Context context) {
		m_context = context;
	}

	@Override
	protected void onPostExecute(List<KUserInfo> result) {
		m_progressDialog.dismiss();
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		m_progressDialog = ProgressDialog.show(m_context, "Get Requests",
				"System is getting your requests ...");
		super.onPreExecute();
	}

	@Override
	protected List<KUserInfo> doInBackground(Void... params) {
		try {
			return ToServer.getRequests();
		} catch (Exception e) {
			return new ArrayList<KUserInfo>();
		}
	}
}