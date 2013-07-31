package com.uit.friendstracking;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.uit.friendstracking.camera.CameraPreview;
import com.uit.friendstracking.imagesupport.CropOption;
import com.uit.friendstracking.imagesupport.CropOptionAdapter;
import com.uit.friendstracking.models.KPhoto;
import com.uit.friendstracking.models.KUserInfo;
import com.uit.friendstracking.tasks.CheckExistAsyncTask;
import com.uit.friendstracking.tasks.LogPositionAsyncTask;
import com.uit.friendstracking.tasks.LoginAsyncTask;
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
	private byte[] currentPhoto = null;
	private Button m_button1;

	protected static final int PHOTO_PICKED = 0;
	private static final String TEMP_PHOTO_FILE = "tempPhoto.jpg";
	protected ImageView photo;
	protected int outputX = 400;
	protected int outputY = 600;
	protected int aspectX = 1;
	protected int aspectY = 1;
	protected boolean return_data = false;
	protected Register thiz;
	protected boolean scale = true;
	protected boolean faceDetection = true;
	protected boolean circleCrop = false;
	private final static String TAG = "MediaStoreTest";

	private Uri mImageCaptureUri;
	private static final int PICK_FROM_CAMERA = 1;
	private static final int PICK_FROM_FILE = 3;
	private static final int CROP_FROM_CAMERA = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);

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
		mImageView = (ImageView) findViewById(R.id.register_Image);

		final String[] items = new String[] { "Take from camera",
				"Select from gallery" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.select_dialog_item, items);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Select Image");
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) { // pick from
																	// camera
				if (item == 0) {
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

					mImageCaptureUri = Uri.fromFile(new File(Environment
							.getExternalStorageDirectory(), "tmp_avatar_"
							+ String.valueOf(System.currentTimeMillis())
							+ ".jpg"));

					intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
							mImageCaptureUri);

					try {
						intent.putExtra("return-data", true);

						startActivityForResult(intent, PICK_FROM_CAMERA);
					} catch (ActivityNotFoundException e) {
						e.printStackTrace();
					}
				} else { // pick from file
					try {
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT,
								null);
						intent.setType("image/*");
						intent.putExtra("crop", "true");
						intent.putExtra("aspectX", aspectX);
						intent.putExtra("aspectY", aspectY);
						intent.putExtra("outputX", outputX);
						intent.putExtra("outputY", outputY);
						intent.putExtra("scale", scale);
						intent.putExtra("return-data", return_data);
						intent.putExtra("output", getTempUri());
						intent.putExtra("outputFormat",
								Bitmap.CompressFormat.JPEG.toString());
						intent.putExtra("noFaceDetection", !faceDetection);
						if (circleCrop) {
							intent.putExtra("circleCrop", true);
						}

						startActivityForResult(intent, PHOTO_PICKED);
					} catch (ActivityNotFoundException e) {
						Toast.makeText(thiz,
								"We did not find a MediaStore Gallery",
								Toast.LENGTH_LONG).show();
					}
				}
			}
		});

		final AlertDialog dialog = builder.create();
		m_button1 = (Button) findViewById(R.id.bt1);
		m_button1.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				dialog.show();
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case PHOTO_PICKED:
			if (resultCode == RESULT_OK) {
				if (data == null) {
					Log.w(TAG, "Null data, but RESULT_OK, from image picker!");
					Toast t = Toast.makeText(this, "no_photo_picked",
							Toast.LENGTH_SHORT);
					t.show();
					return;
				}

				final Bundle extras = data.getExtras();
				if (extras != null) {
					File tempFile = getTempFile();
					mImageView.setImageURI(Uri.parse(tempFile.getPath()));
				}
			}
			break;
		case PICK_FROM_CAMERA:
			doCrop();
			break;
		case CROP_FROM_CAMERA:
			Bundle extras = data.getExtras();

			if (extras != null) {
				Bitmap photo = extras.getParcelable("data");

				mImageView.setImageBitmap(photo);
			}
			try {
				File f = new File(mImageCaptureUri.getPath());

				if (f.exists())
					f.delete();
			} catch (Exception e) {
				// TODO: handle exception
			}
			break;

		}
	}

	private void doCrop() {
		final ArrayList<CropOption> cropOptions = new ArrayList<com.uit.friendstracking.imagesupport.CropOption>();

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setType("image/*");

		List<ResolveInfo> list = getPackageManager().queryIntentActivities(
				intent, 0);

		int size = list.size();

		if (size == 0) {
			Toast.makeText(this, "Can not find image crop app",
					Toast.LENGTH_SHORT).show();

			return;
		} else {
			intent.setData(mImageCaptureUri);

			intent.putExtra("outputX", 32);
			intent.putExtra("outputY", 32);
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("scale", true);
			intent.putExtra("return-data", true);

			if (size == 1) {
				Intent i = new Intent(intent);
				ResolveInfo res = list.get(0);

				i.setComponent(new ComponentName(res.activityInfo.packageName,
						res.activityInfo.name));

				startActivityForResult(i, CROP_FROM_CAMERA);
			}
		}
	}

	private Uri getTempUri() {
		return Uri.fromFile(getTempFile());
	}

	private File getTempFile() {
		if (isSDCARDMounted()) {

			File f = new File(Environment.getExternalStorageDirectory(),
					TEMP_PHOTO_FILE);
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Toast.makeText(thiz,
						"File IO issue, can\'t write temp file on this system",
						Toast.LENGTH_LONG).show();
			}
			return f;
		} else {
			return null;
		}
	}

	private boolean isSDCARDMounted() {
		String status = Environment.getExternalStorageState();

		if (status.equals(Environment.MEDIA_MOUNTED))
			return true;
		return false;
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

				BitmapDrawable drawable = (BitmapDrawable) mImageView
						.getDrawable();
				Bitmap bitmap = drawable.getBitmap();
				ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.PNG, 100,
						byteArrayBitmapStream);
				if (currentPhoto == null)
					currentPhoto = byteArrayBitmapStream.toByteArray();
				KPhoto photo = new KPhoto();
				photo.setPhoto(currentPhoto);
				// userInfo.setPhoto(photo);

				try {
					if (new CheckExistAsyncTask(this, nick).execute().get()) {
						Toast.makeText(getApplicationContext(),
								"The nick already exist. You have to change.",
								Toast.LENGTH_LONG).show();
					} else {
						boolean succesfully = new NewUserAsyncTask(this,
								userInfo, password, photo).execute().get();

						if (succesfully) {
							LocationManager	m_locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
							Criteria criteria = new Criteria();
							criteria.setAccuracy(Criteria.ACCURACY_FINE);
							String m_provider = m_locationManager.getBestProvider(criteria, true);
							Location location = m_locationManager.getLastKnownLocation(m_provider);
							if (location != null) {
								double curLat = location.getLatitude();
								double curLng = location.getLongitude();
								new LoginAsyncTask(this, nick, password).execute();
								new LogPositionAsyncTask(Float.parseFloat("" + curLng), Float.parseFloat("" + curLat)).execute();
							}
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
		} else if (sourceButton == this.m_btCancel) {
			this.finish();
		}
	}

}
