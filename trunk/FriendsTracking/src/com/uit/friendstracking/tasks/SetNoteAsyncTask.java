package com.uit.friendstracking.tasks;

import model.Photo;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.uit.friendstracking.webservices.ToServer;

public class SetNoteAsyncTask extends AsyncTask<Void, Void, Boolean> {

	private Context m_context;
	private ProgressDialog m_progressDialog;
	private float m_latitude;
	private float m_longitude;
	private String m_note;
	private Photo m_photo;

	public SetNoteAsyncTask(Context context, float latitude, float longitude, String note, Photo photo) {
		m_context = context;
		m_latitude = latitude;
		m_longitude = longitude;
		m_note = note;
		m_photo = photo;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		m_progressDialog.dismiss();
		if (result) {
			Toast.makeText(m_context, "The photo has been saved sucesfully.", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(m_context, "Sorry, but the photo was not saved correctly.", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		m_progressDialog = ProgressDialog.show(m_context, "Set Note", "System is setting note ...");
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		try {
			return ToServer.setNote(m_longitude, m_latitude, m_note, m_photo);
		} catch (Exception e) {
			return false;
		}
	}
}