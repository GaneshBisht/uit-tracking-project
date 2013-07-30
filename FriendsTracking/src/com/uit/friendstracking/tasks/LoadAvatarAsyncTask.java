package com.uit.friendstracking.tasks;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class LoadAvatarAsyncTask extends AsyncTask<Void, Void, Bitmap> {
	private Context m_context;
	private ProgressDialog m_progressDialog;
	private String m_url;

	public LoadAvatarAsyncTask(Context context, String url) {
		m_context = context;
		m_url = url;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		if (m_context != null) {
			m_progressDialog.dismiss();
		}
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		if (m_context != null) {
			m_progressDialog = ProgressDialog.show(m_context, "Load Avatar", "System is Loading Avatar ...");
		}
		super.onPreExecute();
	}

	@Override
	protected Bitmap doInBackground(Void... params) {
		try {
			return loadBitmapFromURL(m_url);
		} catch (Exception e) {
			return null;
		}
	}

	private Bitmap loadBitmapFromURL(String url) {
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
}
