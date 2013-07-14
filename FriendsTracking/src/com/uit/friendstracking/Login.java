package com.uit.friendstracking;

import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.uit.friendstracking.webservices.ToServer;

public class Login extends Activity implements OnClickListener {

	private Button bRegister;
	private Button bLogin;
	private EditText nick;
	private EditText password;
	ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		bRegister = (Button) findViewById(R.id.bRegister);
		bRegister.setOnClickListener(this);
		bLogin = (Button) findViewById(R.id.bLogin);
		bLogin.setOnClickListener(this);

		String ip = getString(R.string.IP);
		// Send the ip to the server
		ToServer.setIP(ip);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		Button sourceButton = (Button) v;

		// If the user click the register button.
		if (sourceButton == this.bRegister) {
			// Start the register activity

			Intent nuevoIntent = new Intent(v.getContext(), Register.class);
			this.startActivityForResult(nuevoIntent, 0);

		}
		// If the user click the login button
		if (sourceButton == this.bLogin) {
			// Get the nick and password
			nick = (EditText) findViewById(R.id.tNick);
			password = (EditText) findViewById(R.id.tPassword);

			String nk, pw;

			nk = nick.getText().toString();

			// If the user has not entered the nick.
			if (nk.length() == 0)
				Toast.makeText(getApplicationContext(),
						"You must enter a nick", Toast.LENGTH_LONG).show();
			else {
				pw = password.getText().toString();

				// If the user has not entered the password
				if (pw.length() == 0) {
					Toast.makeText(getApplicationContext(),
							"You must enter a password", Toast.LENGTH_LONG)
							.show();
				} else {
					try {
						if (new LoginAsyncTask(nk, pw).execute().get()) {
							// If the User/Password are correct
							if (ToServer.logged() == true) {

								Intent intent = new Intent(v.getContext(),
										Map.class);
								this.startActivityForResult(intent, 0);

							} else {
								Toast.makeText(
										getApplicationContext(),
										"The user has not been identified correctly. Please re-enter your nick/password.",
										Toast.LENGTH_LONG).show();
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}

				}

			}

		}
	}

	private class LoginAsyncTask extends AsyncTask<Void, Void, Boolean> {

		private ProgressDialog m_progressDialog;
		private String m_userName;
		private String m_passWord;

		public LoginAsyncTask(String userName, String passWord) {
			m_userName = userName;
			m_passWord = passWord;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			m_progressDialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			m_progressDialog = ProgressDialog.show(Login.this, "Loading...",
					"Data is Loading...");
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
}
