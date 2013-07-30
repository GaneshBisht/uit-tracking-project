package com.uit.friendstracking;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.uit.friendstracking.models.KPhoto;
import com.uit.friendstracking.models.KUserInfo;
import com.uit.friendstracking.tasks.CheckExistAsyncTask;
import com.uit.friendstracking.tasks.LoadAvatarAsyncTask;
import com.uit.friendstracking.tasks.LoginAsyncTask;
import com.uit.friendstracking.tasks.NewUserAsyncTask;
import com.uit.friendstracking.webservices.ToServer;

public class Login extends Activity implements OnClickListener {

	private Button m_btRegister;
	private Button m_btLogin;
	private EditText m_username;
	private EditText m_password;
	private boolean isResumed = false;
	private UiLifecycleHelper m_uiHelper;
	private Session.StatusCallback m_callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		m_uiHelper = new UiLifecycleHelper(this, m_callback);
		m_uiHelper.onCreate(savedInstanceState);
		m_btRegister = (Button) findViewById(R.id.bRegister);
		m_btRegister.setOnClickListener(this);
		m_btLogin = (Button) findViewById(R.id.bLogin);
		m_btLogin.setOnClickListener(this);
		LoginButton authButton = (LoginButton) findViewById(R.id.login_button);
		authButton.setReadPermissions(Arrays.asList("basic_info", "email"));
		authButton.setLoginBehavior(SessionLoginBehavior.SUPPRESS_SSO);

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

	@Override
	protected void onResume() {
		super.onResume();
		m_uiHelper.onResume();
		isResumed = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		m_uiHelper.onPause();
		isResumed = false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		m_uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		m_uiHelper.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		m_uiHelper.onSaveInstanceState(outState);
	}

	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		if (isResumed) {
			if (state.equals(SessionState.OPENED)) {
				Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

					@Override
					public void onCompleted(GraphUser user, Response response) {
						if (user != null) {
							try {
								String userName = user.asMap().get("email").toString();
								boolean exist = new CheckExistAsyncTask(null, userName).execute().get();
								if (!exist) {
									KUserInfo userInfo = new KUserInfo();
									userInfo.setNick(userName);
									userInfo.setName(user.getName());
									userInfo.setSurname(user.getUsername());
									userInfo.setEmail(userName);
									userInfo.setPhone(123);
									userInfo.setCountry(user.getLocation().getCountry());
									userInfo.setAddress(user.getLocation().getStreet());
									userInfo.setAdministrator(false);
									
									KPhoto photo = new KPhoto();
									Bitmap bitmap = new LoadAvatarAsyncTask(null, "http://graph.facebook.com/" + user.getUsername() + "/picture").execute().get();
									ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
									bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayBitmapStream);
									byte[] currentPhoto = byteArrayBitmapStream.toByteArray();
									photo.setPhoto(currentPhoto);
									
									new NewUserAsyncTask(null, userInfo, "isFacebook", photo).execute();
								}
								LoginAsyncTask loginAsyncTask = new LoginAsyncTask(null, userName, "isFacebook");

								if (loginAsyncTask.execute().get()) {
									if (ToServer.logged() == true) {
										Intent intent = new Intent(getApplicationContext(), Map.class);
										startActivityForResult(intent, 0);
									} else {
										Toast.makeText(getApplicationContext(),
												"The user has not been identified correctly. Please re-enter your nick/password.", Toast.LENGTH_LONG).show();
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
				});
			} else if (state.isClosed()) {
				Toast.makeText(getApplicationContext(), "close", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
}
