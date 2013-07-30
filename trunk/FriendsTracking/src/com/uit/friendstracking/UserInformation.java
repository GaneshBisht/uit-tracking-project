package com.uit.friendstracking;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.ksoap2.serialization.MarshalBase64;

import com.uit.friendstracking.imagesupport.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.uit.friendstracking.models.KPhoto;
import com.uit.friendstracking.models.KUserInfo;
import com.uit.friendstracking.tasks.ChangeUserAsyncTask;
import com.uit.friendstracking.tasks.CheckExistAsyncTask;
import com.uit.friendstracking.tasks.GetMyUserAsyncTask;
import com.uit.friendstracking.tasks.NewUserAsyncTask;
import com.uit.friendstracking.webservices.ToServer;

public class UserInformation extends Activity implements OnClickListener {

	private Uri mImageCaptureUri;
	private ImageView mImageView;

	protected static final int PHOTO_PICKED = 0;
	private static final String TEMP_PHOTO_FILE = "tempPhoto.jpg";
	private Button mBtn;
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

	private static final int PICK_FROM_CAMERA = 1;
	private static final int PICK_FROM_FILE = 3;
	private static final int CROP_FROM_CAMERA = 2;

	private Button btpickImage;
	private Button bSave;
	private Button bCancel;
	private KUserInfo user;

	private EditText et_name, et_surname, et_address, et_email, et_country,
			et_password, et_phone;

	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.modifyuser);
		bSave = (Button) findViewById(R.id.btupdate);
		bSave.setOnClickListener(this);
		bCancel = (Button) findViewById(R.id.btCancel);
		bCancel.setOnClickListener(this);

		et_name = (EditText) findViewById(R.id.tbName);
		et_surname = (EditText) findViewById(R.id.tbSurName);
		et_address = (EditText) findViewById(R.id.tbAddress);
		et_country = (EditText) findViewById(R.id.tbCountry);
		et_email = (EditText) findViewById(R.id.tbEmail);
		et_password = (EditText) findViewById(R.id.tbPass);
		et_phone = (EditText) findViewById(R.id.tbPhone);
		mImageView = (ImageView) findViewById(R.id.iv_photo);
		try {
			// Read the old values
			user = new GetMyUserAsyncTask().execute().get();
			et_name.setText(user.getName());
			et_surname.setText(user.getSurname());
			et_address.setText(user.getAddress());
			et_country.setText(user.getCountry());
			et_email.setText(user.getEmail());
			et_phone.setText(String.valueOf(user.getPhone()));
			byte[] b = user.getPhoto().getPhoto();
			Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
			mImageView.setImageDrawable(getDrwableFromBytes(b));

		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),
					"Failed to try to load user's information.",
					Toast.LENGTH_LONG).show();

		}

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

		Button button = (Button) findViewById(R.id.btpickimage);

		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.show();
			}
		});

	};

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

	@SuppressWarnings("deprecation")
	public Drawable getDrwableFromBytes(byte[] imageBytes) {

		if (imageBytes != null)
			return new BitmapDrawable(BitmapFactory.decodeByteArray(imageBytes,
					0, imageBytes.length));
		else
			return null;
	}

	public Bitmap loadBitmap(String url) {
		Bitmap bm = null;
		InputStream is = null;
		BufferedInputStream bis = null;
		try {
			URLConnection conn = new URL(url).openConnection();
			conn.connect();
			is = conn.getInputStream();
			bis = new BufferedInputStream(is, 8192);
			bm = BitmapFactory.decodeStream(bis);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bm;
	}

	@Override
	public void onClick(View v) {

		Button sourceButton = (Button) v;

		// If the user click the button save
		if (sourceButton == this.bSave) {
			String name, surname, address, email, country, password, phone;

			// Read the values
			name = et_name.getText().toString();
			surname = et_surname.getText().toString();
			address = et_address.getText().toString();
			country = et_country.getText().toString();
			email = et_email.getText().toString();
			password = et_password.getText().toString();
			phone = et_phone.getText().toString();

			// Check if all the attributes has a value
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
				try {
					user = new GetMyUserAsyncTask().execute().get();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ExecutionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				user.setName(name);
				user.setSurname(surname);
				user.setEmail(email);
				user.setPhone(Integer.parseInt(phone));
				user.setCountry(country);
				user.setAddress(address);
				user.setAdministrator(false);

				BitmapDrawable drawable = (BitmapDrawable) mImageView
						.getDrawable();
				Bitmap bitmap = drawable.getBitmap();
				ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.PNG, 100,
						byteArrayBitmapStream);
				byte[] currentPhoto = byteArrayBitmapStream.toByteArray();
				KPhoto photo = new KPhoto();
				photo.setPhoto(currentPhoto);
				// userInfo.setPhoto(photo);

				try {

					boolean succesfully = new ChangeUserAsyncTask(this, user,
							password, photo).execute().get();
					if (succesfully) {
						Toast.makeText(getApplicationContext(),
								"The user has been saved.", Toast.LENGTH_LONG)
								.show();
						this.finish();
					} else
						Toast.makeText(getApplicationContext(),
								"Error, the user has not been saved.",
								Toast.LENGTH_LONG).show();

				} catch (Exception e) {
					Toast.makeText(
							getApplicationContext(),
							"Failed while trying to save the user."
									+ e.getMessage(), Toast.LENGTH_LONG).show();

					e.printStackTrace();
				}

			}
		}
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

	private class GetUserAsyncTask extends AsyncTask<Void, Void, KUserInfo> {

		private ProgressDialog m_progressDialog;

		@Override
		protected void onPostExecute(KUserInfo result) {
			m_progressDialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			m_progressDialog = ProgressDialog.show(UserInformation.this,
					"Loading...", "Data is Loading...");
		}

		@Override
		protected KUserInfo doInBackground(Void... params) {
			try {
				return ToServer.myUser();
			} catch (Exception e) {
				return null;
			}
		}
	}

}
