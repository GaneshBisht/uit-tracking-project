package com.uit.friendstracking.tasks;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

import com.uit.friendstracking.models.KUserInfo;
import com.uit.friendstracking.webservices.ToServer;

public class GetFriendsAsyncTask extends AsyncTask<Void, Void, List<KUserInfo>> {

	public GetFriendsAsyncTask() {
	}

	@Override
	protected void onPostExecute(List<KUserInfo> result) {
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected List<KUserInfo> doInBackground(Void... params) {
		try {
			return ToServer.getFriends();
		} catch (Exception e) {
			return new ArrayList<KUserInfo>();
		}
	}
}