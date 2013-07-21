package com.uit.friendstracking;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.uit.friendstracking.camera.CameraPreview;
import com.uit.friendstracking.models.KPhoto;
import com.uit.friendstracking.models.KUserInfo;
import com.uit.friendstracking.tasks.CheckExistAsyncTask;
import com.uit.friendstracking.tasks.NewUserAsyncTask;

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
	private ImageView mImageView;
	private CameraPreview preview;
	private byte[] currentPhoto = null;
	private Button m_button1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		
		preview = new CameraPreview(this);
		((FrameLayout) findViewById(R.id.camera_preview1)).addView(preview);

		m_btSave = (Button) findViewById(R.id.register_bSave);
		m_btSave.setOnClickListener(this);
		
		m_button1 = (Button) findViewById(R.id.bt1);
		m_button1.setOnClickListener(this);

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
		mImageView = (ImageView)findViewById(R.id.register_Image);

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

				KUserInfo userInfo = new KUserInfo();
				userInfo.setNick(nick);
				userInfo.setName(name);
				userInfo.setSurname(surname);
				userInfo.setEmail(email);
				userInfo.setPhone(Integer.parseInt(phone));
				userInfo.setCountry(country);
				userInfo.setAddress(address);
				userInfo.setAdministrator(false);
				
				/*BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();
				Bitmap bitmap = drawable.getBitmap();
				ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayBitmapStream);
				byte[] data = byteArrayBitmapStream.toByteArray();*/
				KPhoto photo = new KPhoto();
				photo.setPhoto(currentPhoto);
				//userInfo.setPhoto(photo);
				

				try {
					if (new CheckExistAsyncTask(this, nick).execute().get()) {
						Toast.makeText(getApplicationContext(),
								"The nick already exist. You have to change.",
								Toast.LENGTH_LONG).show();
					} else {
						boolean succesfully = new NewUserAsyncTask(this, userInfo,
								password, photo).execute().get();

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
		else if (sourceButton == this.m_btCancel) {
			this.finish();
		}
		else if(sourceButton == this.m_button1){
			preview.getCamera().takePicture(null, null, jpegCallback);
		}
	}
	
	PictureCallback jpegCallback = new PictureCallback() {

		// Store the photo
		public void onPictureTaken(byte[] data, Camera camera) {
			currentPhoto = data;
		}
	};
}
