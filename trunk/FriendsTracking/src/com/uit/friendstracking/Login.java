package com.uit.friendstracking;

import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.uit.friendstracking.tasks.LoginAsyncTask;
import com.uit.friendstracking.webservices.ToServer;

public class Login extends Activity implements OnClickListener {

	private Button m_btRegister;
	private Button m_btLogin;
	private EditText m_username;
	private EditText m_password;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		m_btRegister = (Button) findViewById(R.id.bRegister);
		m_btRegister.setOnClickListener(this);
		m_btLogin = (Button) findViewById(R.id.bLogin);
		m_btLogin.setOnClickListener(this);

		String ip = getString(R.string.IP);
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

		if (sourceButton == this.m_btRegister) {
			Intent registerIntent = new Intent(v.getContext(), Register.class);
			this.startActivityForResult(registerIntent, 0);
		}
		if (sourceButton == this.m_btLogin) {
			m_username = (EditText) findViewById(R.id.tNick);
			m_password = (EditText) findViewById(R.id.tPassword);

			String username;
			String password;

			username = m_username.getText().toString();

			if (username.length() == 0)
				Toast.makeText(getApplicationContext(), "You must enter a nick", Toast.LENGTH_LONG).show();
			else {
				password = m_password.getText().toString();

				if (password.length() == 0) {
					Toast.makeText(getApplicationContext(), "You must enter a password", Toast.LENGTH_LONG).show();
				} else {
					try {
						LoginAsyncTask loginAsyncTask = new LoginAsyncTask(this, username, password);

						if (loginAsyncTask.execute().get()) {
							if (ToServer.logged() == true) {

								Intent intent = new Intent(v.getContext(), Map.class);
								this.startActivityForResult(intent, 0);

							} else {
								Toast.makeText(getApplicationContext(), "The user has not been identified correctly. Please re-enter your nick/password.",
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
}
