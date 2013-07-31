package com.uit.friendstracking;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.model.GraphLocation;
import com.uit.friendstracking.models.KPhoto;
import com.uit.friendstracking.tasks.SetNoteAsyncTask;

public class CreateNote extends Activity implements OnClickListener {

	private static final int CAMERA_REQUEST = 1888;
	private static final int PLACE_PICKER = 2727;
	private List<ImageView> m_listImageView = new ArrayList<ImageView>();
	private CheckBox m_cbPost;
	Facebook authenticatedFacebook = new Facebook("141712532702607");
	private static final String[] PERMISSIONS = new String[] { "publish_stream", "read_stream", "offline_access" };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);

		m_cbPost = (CheckBox) findViewById(R.id.cbPostToWall);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
			Bitmap photo = (Bitmap) data.getExtras().get("data");
			LinearLayout linearLayout1 = (LinearLayout) findViewById(R.id.layoutImg);
			ImageView image = new ImageView(CreateNote.this);
			image.setImageBitmap(photo);
			m_listImageView.add(image);
			linearLayout1.addView(image);
		} else if (requestCode == PLACE_PICKER && resultCode == RESULT_OK) {
			FriendsTrackingApplication app = (FriendsTrackingApplication) getApplication();
			LinearLayout linearLayout1 = (LinearLayout) findViewById(R.id.layoutText);
			TextView textview = new TextView(CreateNote.this);
			textview.setText(app.getSelectedPlace().getName());
			linearLayout1.addView(textview);
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
		case R.id.takePhoto:
			Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(cameraIntent, CAMERA_REQUEST);
			break;
		case R.id.pickPlace:
			startPickerActivity(PickerActivity.PLACE_PICKER, PLACE_PICKER);
			break;
		case R.id.save:
			Bitmap bitmap = ((BitmapDrawable) m_listImageView.get(0).getDrawable()).getBitmap();
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] bitmapdata = stream.toByteArray();
			if (bitmapdata != null) {
				EditText comment = (EditText) findViewById(R.id.camera_tComment);
				String com = comment.getText().toString();
				if (com == null)
					com = "";
				if (m_cbPost.isChecked()) {
					authenticatedFacebook.authorize(CreateNote.this, PERMISSIONS, new FaceBookWallPostListener());
				}
				try {

					KPhoto photo = new KPhoto();
					photo.setPhoto(bitmapdata);
					FriendsTrackingApplication app = (FriendsTrackingApplication) getApplication();
					GraphLocation location = app.getSelectedPlace().getLocation();
					Double lat = Double.valueOf(location.getLatitude());
					Double lng = Double.valueOf(location.getLongitude());
					new SetNoteAsyncTask(this, lat.floatValue(), lng.floatValue(), com, photo).execute();

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

	@Override
	public void onClick(View view) {

	}

	private void startPickerActivity(Uri data, int requestCode) {
		Intent intent = new Intent(this, PickerActivity.class);
		intent.setData(data);
		startActivityForResult(intent, requestCode);
	}

	public class FaceBookWallPostListener implements DialogListener {

		public void onComplete(Bundle values) {
			new FacebookWallPost().execute();
		}

		public void onCancel() {
		}

		public void onError(DialogError e) {
			e.printStackTrace();
		}

		public void onFacebookError(FacebookError e) {
			e.printStackTrace();
		}
	}

	private class FacebookWallPost extends AsyncTask<String, Void, String> {
		ProgressDialog pDialog;

		@Override
		protected String doInBackground(String... params) {
			try {
				Bundle parameters = new Bundle();
				EditText comment = (EditText) findViewById(R.id.camera_tComment);
				String com = comment.getText().toString();
				parameters.putString("message", com);
				try {
					Bitmap bitmap = ((BitmapDrawable) m_listImageView.get(0).getDrawable()).getBitmap();
					byte[] data = null;
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
					data = stream.toByteArray();
					if (data != null) {
						parameters.putByteArray("picture", data);
					}
					parameters.putString("access_token", authenticatedFacebook.getAccessToken());
					authenticatedFacebook.request("me");
					authenticatedFacebook.request("me/photos", parameters, "POST");
				} catch (Exception e) {
					return e.getMessage();
				}

				return "success";
			} catch (Exception e) {
				return e.getMessage();
			}
		}

		@Override
		protected void onPostExecute(String result) {
			pDialog.dismiss();
			if (result.equals("success")) {
				Toast.makeText(CreateNote.this, "WallPost Successfully Done", Toast.LENGTH_SHORT).show();
				try {
					new File(Environment.getExternalStorageDirectory().toString() + "/Diegodeals", "diegodeals.jpg").delete();
				} catch (Exception e) {
				}

			} else {
				Toast.makeText(CreateNote.this, "Failed to post \n " + result, Toast.LENGTH_SHORT).show();
			}

		}

		@Override
		protected void onPreExecute() {
			pDialog = new ProgressDialog(CreateNote.this);
			pDialog.setMessage("Posting Picture & Message on Facebook...");
			pDialog.show();
		}

	}
}
