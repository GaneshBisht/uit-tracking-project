package com.uit.friendstracking;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.uit.friendstracking.models.KUserInfo;
import com.uit.friendstracking.webservices.ToServer;

public class FindFriend extends Activity implements OnClickListener {

	private Button bSearch;
	private Button bCancel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.find);

		bSearch = (Button) findViewById(R.id.find_bSearch);
		bSearch.setOnClickListener(this);

		bCancel = (Button) findViewById(R.id.find_bCancel);
		bCancel.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		Button sourceButton = (Button) v;

		if (sourceButton == bSearch) {
			EditText textName = (EditText) findViewById(R.id.find_tName);
			EditText textCity = (EditText) findViewById(R.id.find_tCity);
			EditText textNick = (EditText) findViewById(R.id.find_tNick);
			EditText textSurname = (EditText) findViewById(R.id.find_tSurname);

			String name = textName.getText().toString();
			String city = textCity.getText().toString();
			String nick = textNick.getText().toString();
			String surname = textSurname.getText().toString();

			try {
				List<KUserInfo> listUsers = new SearchAsyncTask(name, surname, nick, city).execute().get();

				// Prepare to send the ids and nicks to ListFriends
				String[] listItems = new String[listUsers.size()];
				int[] listIds = new int[listUsers.size()];

				for (int i = 0; i < listUsers.size(); i++) {
					listItems[i] = listUsers.get(i).getNick();
					listIds[i] = listUsers.get(i).getId();
				}

				if (listUsers.size() > 0) {

					// Start the new activity
					Intent newIntent = new Intent(this, ListFriends.class);
					newIntent.putExtra("items", listItems);
					newIntent.putExtra("ids", listIds);

					this.startActivityForResult(newIntent, 0);
				} else {
					Toast.makeText(getApplicationContext(),
							"No matches found for the search.",
							Toast.LENGTH_LONG).show();

				}
			} catch (Exception e) {

				Toast.makeText(
						getApplicationContext(),
						"Failed while trying to find friends." + e.getMessage(),
						Toast.LENGTH_LONG).show();

			}

		}
		// If the user press the button cancel
		else if (sourceButton == bCancel) {
			this.finish();
		}
	}

	private class SearchAsyncTask extends
			AsyncTask<Void, Void, List<KUserInfo>> {

		private ProgressDialog m_progressDialog;
		private String m_name;
		private String m_surname;
		private String m_nick;
		private String m_country;

		public SearchAsyncTask(String name, String surname, String nick,
				String country) {
			m_name = name;
			m_surname = surname;
			m_nick = nick;
			m_country = country;
		}

		@Override
		protected void onPostExecute(List<KUserInfo> result) {
			m_progressDialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			m_progressDialog = ProgressDialog.show(FindFriend.this,
					"Searching...", "System is Searching...");
		}

		@Override
		protected List<KUserInfo> doInBackground(Void... params) {
			try {
				return ToServer.searchFriend(m_nick, m_name, m_surname,
						m_country);
			} catch (Exception e) {
				return new ArrayList<KUserInfo>();
			}
		}
	}

}
