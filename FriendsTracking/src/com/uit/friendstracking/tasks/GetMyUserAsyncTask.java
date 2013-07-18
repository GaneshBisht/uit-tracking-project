package com.uit.friendstracking.tasks;

import android.os.AsyncTask;

import com.uit.friendstracking.models.KUserInfo;
import com.uit.friendstracking.webservices.ToServer;

public class GetMyUserAsyncTask extends AsyncTask<Void, Void, KUserInfo> {

	public GetMyUserAsyncTask() {

	}

	@Override
	protected void onPostExecute(KUserInfo result) {
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected KUserInfo doInBackground(Void... params) {
		try {
			return ToServer.myUser();
		} catch (Exception e) {
			return null;
		}
	}
}