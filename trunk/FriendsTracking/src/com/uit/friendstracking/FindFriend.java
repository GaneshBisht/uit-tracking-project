package com.uit.friendstracking;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.uit.friendstracking.models.KUserInfo;
import com.uit.friendstracking.tasks.SearchAsyncTask;

public class FindFriend extends Activity implements OnClickListener {

	private Button m_btSearch;
	private Button m_btCancel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.find);

		m_btSearch = (Button) findViewById(R.id.find_bSearch);
		m_btSearch.setOnClickListener(this);

		m_btCancel = (Button) findViewById(R.id.find_bCancel);
		m_btCancel.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		Button sourceButton = (Button) v;

		if (sourceButton == m_btSearch) {
			EditText textName = (EditText) findViewById(R.id.find_tName);
			EditText textCity = (EditText) findViewById(R.id.find_tCity);
			EditText textNick = (EditText) findViewById(R.id.find_tNick);
			EditText textSurname = (EditText) findViewById(R.id.find_tSurname);

			String name = textName.getText().toString();
			String city = textCity.getText().toString();
			String nick = textNick.getText().toString();
			String surname = textSurname.getText().toString();

			try {
				List<KUserInfo> listUsers = new SearchAsyncTask(this, name,
						surname, nick, city).execute().get();

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
		else if (sourceButton == m_btCancel) {
			this.finish();
		}
	}
}
