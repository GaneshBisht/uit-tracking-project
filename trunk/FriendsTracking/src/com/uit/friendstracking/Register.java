package com.uit.friendstracking;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.uit.friendstracking.models.KUserInfo;
import com.uit.friendstracking.webservices.ToServer;

public class Register extends Activity implements OnClickListener {

	private Button m_btSave;
	private Button m_btCancel;

	private EditText m_etName;
	private EditText m_etSurname;
	private EditText m_etAddress;
	private EditText m_etEmail;
	private EditText m_etCountry;
	private EditText m_etUserName;
	private EditText m_etPassWord;
	private EditText m_etPhone;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);

		// Fit the interface's buttons and EditTexts to local variables
		m_btSave = (Button) findViewById(R.id.register_bSave);
		m_btSave.setOnClickListener(this);

		m_btCancel = (Button) findViewById(R.id.register_bCancel);
		m_btCancel.setOnClickListener(this);

		m_etName = (EditText) findViewById(R.id.register_tName);
		m_etSurname = (EditText) findViewById(R.id.register_tSurname);
		m_etAddress = (EditText) findViewById(R.id.register_tAddress);
		m_etCountry = (EditText) findViewById(R.id.register_tCountry);
		m_etEmail = (EditText) findViewById(R.id.register_tEmail);
		m_etUserName = (EditText) findViewById(R.id.register_tNick);
		m_etPassWord = (EditText) findViewById(R.id.register_tPassword);
		m_etPhone = (EditText) findViewById(R.id.register_tPhone);

	}

	@Override
	public void onClick(View v) {
		Button sourceButton = (Button) v;

		if (sourceButton == this.m_btSave) {
			String name, surname, address, email, country, nick, password, phone;

			name = m_etName.getText().toString();
			surname = m_etSurname.getText().toString();
			address = m_etAddress.getText().toString();
			country = m_etCountry.getText().toString();
			email = m_etEmail.getText().toString();
			nick = m_etUserName.getText().toString();
			password = m_etPassWord.getText().toString();
			phone = m_etPhone.getText().toString();

			if (name.length() == 0)
				Toast.makeText(getApplicationContext(),
						"You must enter the name.", Toast.LENGTH_LONG).show();
			else if (surname.length() == 0)
				Toast.makeText(getApplicationContext(),
						"You must enter the surname .", Toast.LENGTH_LONG)
						.show();
			else if (address.length() == 0)
				Toast.makeText(getApplicationContext(),
						"You must enter the address.", Toast.LENGTH_LONG)
						.show();
			else if (country.length() == 0)
				Toast.makeText(getApplicationContext(),
						"You must enter the country .", Toast.LENGTH_LONG)
						.show();
			else if (email.length() == 0)
				Toast.makeText(getApplicationContext(),
						"You must enter the email.", Toast.LENGTH_LONG).show();
			else if (nick.length() == 0 || nick.length() < 3)
				Toast.makeText(getApplicationContext(),
						"You must enter the nick greater than 3 characters .",
						Toast.LENGTH_LONG).show();
			else if (password.length() == 0 || password.length() < 4)
				Toast.makeText(getApplicationContext(),
						"You must enter a password greater than 4 characters.",
						Toast.LENGTH_LONG).show();
			else if (phone.length() == 0)
				Toast.makeText(getApplicationContext(),
						"You must enter the phone number.", Toast.LENGTH_LONG)
						.show();
			else if (phone.length() > 9)
				Toast.makeText(getApplicationContext(),
						"The phone number is incorrect.", Toast.LENGTH_LONG)
						.show();

			else {

				KUserInfo us = new KUserInfo();
				us.setNick(nick);
				us.setName(name);
				us.setSurname(surname);
				us.setEmail(email);
				us.setPhone(Integer.parseInt(phone));
				us.setCountry(country);
				us.setAddress(address);
				us.setAdministrator(false);

				try {
					if (new CheckExistAsyncTask(nick).execute().get()) {
						Toast.makeText(getApplicationContext(),
								"The nick already exist. You have to change.",
								Toast.LENGTH_LONG).show();
					} else {
						// Save the user
						boolean succesfully = new NewUserAsyncTask(us, password).execute().get();

						// If the user has been saved in the database
						if (succesfully) {
							Toast.makeText(getApplicationContext(),
									"The user has been saved.",
									Toast.LENGTH_LONG).show();
							this.finish();
						} else
							Toast.makeText(getApplicationContext(),
									"Error, the user has not been saved.",
									Toast.LENGTH_LONG).show();

					}
				} catch (Exception e) {
					Toast.makeText(
							getApplicationContext(),
							"Failed while trying to save the user."
									+ e.getMessage(), Toast.LENGTH_LONG).show();

					e.printStackTrace();
				}

			}
		}
		// If the user press the cancel button
		else if (sourceButton == this.m_btCancel) {
			this.finish();
		}
	}
	
	private class CheckExistAsyncTask extends AsyncTask<Void, Void, Boolean> {

		private ProgressDialog m_progressDialog;
		private String m_userName;

		public CheckExistAsyncTask(String userName) {
			m_userName = userName;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			m_progressDialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			m_progressDialog = ProgressDialog.show(Register.this, "Loading...",
					"Data is Loading...");
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				return ToServer.exists(m_userName);
			} catch (Exception e) {
				return false;
			}
		}
	}
	
	private class NewUserAsyncTask extends AsyncTask<Void, Void, Boolean> {

		private ProgressDialog m_progressDialog;
		private KUserInfo m_user;
		private String m_passWord;

		public NewUserAsyncTask(KUserInfo user, String passWord) {
			m_user = user;
			m_passWord = passWord;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			m_progressDialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			m_progressDialog = ProgressDialog.show(Register.this, "Adding...",
					"User is Adding...");
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				return ToServer.newUser(m_user, m_passWord);
			} catch (Exception e) {
				return false;
			}
		}
	}
}
