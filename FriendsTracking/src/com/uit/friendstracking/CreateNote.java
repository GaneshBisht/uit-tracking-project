package com.uit.friendstracking;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.uit.friendstracking.listeners.GPSListener;
import com.uit.friendstracking.models.KPhoto;
import com.uit.friendstracking.models.KUserInfo;
import com.uit.friendstracking.tasks.GetMyUserAsyncTask;
import com.uit.friendstracking.tasks.SetNoteAsyncTask;

public class CreateNote extends Activity {

	private static final int CAMERA_REQUEST = 1888;
	private ImageView m_imageView;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);
		m_imageView = (ImageView) this.findViewById(R.id.imageView1);

		Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(cameraIntent, CAMERA_REQUEST);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
			Bitmap photo = (Bitmap) data.getExtras().get("data");
			m_imageView.setImageBitmap(photo);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_create_note, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.take_photo:
			Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(cameraIntent, CAMERA_REQUEST);
			break;
		case R.id.save:
			Bitmap bitmap = ((BitmapDrawable) m_imageView.getDrawable()).getBitmap();
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] bitmapdata = stream.toByteArray();
			if (bitmapdata != null) {
				EditText comment = (EditText) findViewById(R.id.camera_tComment);
				String com = comment.getText().toString();
				if (com == null)
					com = "";
				try {

					KPhoto photo = new KPhoto();
					photo.setPhoto(bitmapdata);
					KUserInfo user = new GetMyUserAsyncTask().execute().get();
					boolean success;

					GPSListener locationListener = new GPSListener(2);

					if (locationListener.hasPosition()) {
						success = new SetNoteAsyncTask(this, locationListener.getCurrentLatitude(), locationListener.getCurrentLongitude(), com, photo)
								.execute().get();

					} else {
						if (user.getPosition() != null) {
							success = new SetNoteAsyncTask(this, user.getPosition().getLatitudeFloat(), user.getPosition().getLongitudeFloat(), com, photo)
									.execute().get();
						} else {
							success = false;
						}
					}
					if (success) {
						Toast.makeText(getApplicationContext(), "The photo has been saved sucesfully.", Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(getApplicationContext(), "Sorry, but the photo was not saved correctly.", Toast.LENGTH_LONG).show();
					}

				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), "Error: The photo was not saved correctly.\n" + e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
			break;
		default:
			break;
		}
		return true;
	}
}
