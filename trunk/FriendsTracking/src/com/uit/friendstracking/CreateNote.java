package com.uit.friendstracking;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.uit.friendstracking.camera.CameraPreview;
import com.uit.friendstracking.listeners.GPSListener;
import com.uit.friendstracking.models.KPhoto;
import com.uit.friendstracking.models.KUserInfo;
import com.uit.friendstracking.tasks.GetMyUserAsyncTask;
import com.uit.friendstracking.tasks.SetNoteAsyncTask;
import com.uit.friendstracking.webservices.ToServer;

public class CreateNote extends Activity implements OnClickListener {

	private Camera camera;
	private CameraPreview preview;
	private Button buttonTakePhoto, buttonSave, buttonCancel, buttonAgain;
	private byte[] currentPhoto = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.camera);

		preview = new CameraPreview(this);
		((FrameLayout) findViewById(R.id.camera_preview)).addView(preview);

		// Fit the GUI elements to variables.
		buttonTakePhoto = (Button) findViewById(R.id.camera_bTakePhoto);
		buttonTakePhoto.setOnClickListener(this);
		buttonAgain = (Button) findViewById(R.id.camera_bTryAgain);
		buttonAgain.setOnClickListener(this);
		buttonAgain.setEnabled(false);
		buttonAgain.setVisibility(4);
		buttonSave = (Button) findViewById(R.id.camera_bSave);
		buttonSave.setOnClickListener(this);
		buttonCancel = (Button) findViewById(R.id.camera_bCancel);
		buttonCancel.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {

		// Know what is the press button
		Button sourceButton = (Button) v;

		// If the user press the take photo button.
		if (sourceButton == this.buttonTakePhoto) {

			// Take the picture
			preview.getCamera().takePicture(null, null, jpegCallback);

			// Enable the button again
			buttonAgain.setEnabled(true);
			buttonAgain.setVisibility(0);

			// Hide the button take photo
			buttonTakePhoto.setEnabled(false);
			buttonTakePhoto.setVisibility(4);
		}

		// If the user press the button save
		else if (sourceButton == this.buttonSave) {
			// If the user took a photo
			if (currentPhoto != null) {
				// Gets the comment
				EditText comment = (EditText) findViewById(R.id.camera_tComment);
				String com = comment.getText().toString();
				if (com == null)
					com = "";
				try {
					KPhoto photo = new KPhoto();
					photo.setPhoto(currentPhoto);
					KUserInfo user = new GetMyUserAsyncTask().execute().get();
					boolean success;

					// Activate the gps
					LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
					GPSListener locationListener = new GPSListener(this);
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

					// Store the photo in the database
					// If the GPS has detected new position
					if (locationListener.hasPosition()) {
						success = new SetNoteAsyncTask(this, locationListener.getCurrentLatitude(), locationListener.getCurrentLongitude(), com, photo)
								.execute().get();

					}
					// If the GPS does not detected new position we use the last user's postion.
					else {
						if (user.getPosition() != null) {
							success = new SetNoteAsyncTask(this, user.getPosition().getLatitudeFloat(), user.getPosition().getLongitudeFloat(), com, photo).execute().get();
						} else
							success = false;
					}
					// If the photo has been stored succesfully
					if (success) {
						Toast.makeText(getApplicationContext(), "The photo has been saved sucesfully.", Toast.LENGTH_LONG).show();
					}

					// If there was a problem saving the picture.
					else {
						Toast.makeText(getApplicationContext(), "Sorry, but the photo was not saved correctly.", Toast.LENGTH_LONG).show();
					}

				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), "Error: The photo was not saved correctly.\n" + e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		}

		// If the user press the button cancel
		else if (sourceButton == this.buttonCancel) {
			// finish this activity
			this.finish();
		}

		// If the user press the button again
		else if (sourceButton == this.buttonAgain) {

			// Set disabled the button again
			buttonAgain.setEnabled(false);
			buttonAgain.setVisibility(4);

			// Set enabled the button take picture
			buttonTakePhoto.setEnabled(true);
			buttonTakePhoto.setVisibility(0);

			// Start new preview
			preview.getCamera().startPreview();

		}
	}

	PictureCallback jpegCallback = new PictureCallback() {

		// Store the photo
		public void onPictureTaken(byte[] data, Camera camera) {
			currentPhoto = data;
		}
	};

}
