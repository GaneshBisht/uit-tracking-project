package com.uit.friendstracking;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.uit.friendstracking.models.KUserInfo;
import com.uit.friendstracking.webservices.ToServer;

public class FriendRequest extends ListActivity implements OnClickListener {

	private List<KUserInfo> m_listUsers;
	private Button m_btAddFriend;
	private Button m_btCancel;
	private List<Boolean> m_listToAdd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_request);

		try {
			// Get the friends requests.
			m_listUsers = new GetRequestsAsyncTask().execute().get();
			String[] listItems = new String[m_listUsers.size()];
			m_listToAdd = new ArrayList<Boolean>();

			for (int i = 0; i < m_listUsers.size(); i++) {
				listItems[i] = m_listUsers.get(i).getNick();
				m_listToAdd.add(false);
			}
			this.setListAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_checked, listItems));

		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),
					"Failed looking for friend requests: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
			this.finish();
		}

		// Fit the interface's buttons to a local variables
		m_btAddFriend = (Button) findViewById(R.id.listrequest_bAdd);
		m_btAddFriend.setOnClickListener(this);

		m_btCancel = (Button) findViewById(R.id.listrequest_bCancel);
		m_btCancel.setOnClickListener(this);

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// Set checked/unchecked the selected item
		CheckedTextView textView = (CheckedTextView) v;
		textView.setChecked(!textView.isChecked());
		m_listToAdd.set(position, textView.isChecked());

		super.onListItemClick(l, v, position, id);
	}

	@Override
	public void onClick(View v) {
		Button sourceButton = (Button) v;
		boolean fail = false;

		// If the user press the button add friends
		if (sourceButton == m_btAddFriend) {
			// For all the items
			for (int i = 0; i < this.getListView().getCount(); i++) {
				// If the item is checked
				if (m_listToAdd.get(i) == true) {
					try {
						// Ask for friend
						if (!new AskFriendAsyncTask(m_listUsers.get(i).getId()).execute().get())
							fail = true;

					} catch (Exception e) {
						fail = true;
					}
				}
			}

			if (!fail) {
				Toast.makeText(getApplicationContext(),
						"All the friends have been added sucesfully.",
						Toast.LENGTH_LONG).show();
			} else
				Toast.makeText(getApplicationContext(),
						"Failed while trying to save friends ",
						Toast.LENGTH_LONG).show();

			// Finish
			this.finish();

		}
		// If the user press the button finish
		else if (sourceButton == m_btCancel) {
			this.finish();
		}

	}

	private class GetRequestsAsyncTask extends
			AsyncTask<Void, Void, List<KUserInfo>> {

		private ProgressDialog m_progressDialog;

		public GetRequestsAsyncTask() {
		}

		@Override
		protected void onPostExecute(List<KUserInfo> result) {
			m_progressDialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			m_progressDialog = ProgressDialog.show(FriendRequest.this,
					"Searching...", "System is Searching...");
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

	private class AskFriendAsyncTask extends AsyncTask<Void, Void, Boolean> {

		private ProgressDialog m_progressDialog;
		private int m_to;

		public AskFriendAsyncTask(int to) {
			m_to = to;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			m_progressDialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			m_progressDialog = ProgressDialog.show(FriendRequest.this,
					"Searching...", "System is Searching...");
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
}
